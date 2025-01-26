package moe.dituon.petpet.bot.qq.handler;

import moe.dituon.petpet.bot.BotSendEvent;
import moe.dituon.petpet.bot.MessageEventHandler;
import moe.dituon.petpet.bot.qq.QQBotConfig;
import moe.dituon.petpet.bot.qq.QQBotService;
import moe.dituon.petpet.bot.qq.avatar.QQAvatarRequester;
import moe.dituon.petpet.bot.qq.permission.ContactPermission;
import moe.dituon.petpet.bot.utils.Cooler;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.PetpetModel;
import moe.dituon.petpet.core.element.PetpetTemplateModel;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.script.PetpetScriptModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public abstract class QQMessageEventHandler extends MessageEventHandler {
    protected final QQBotService service;
    protected final QQBotConfig config;

    protected QQMessageEventHandler(QQBotService service) {
        this.service = service;
        this.config = service.getConfig();
    }

    protected void handle(MessageContext message) {
        message.handleCommand();
    }

    public abstract class MessageContext {
        protected String messageText;
        protected PetpetModel template;
        // 不包含 指令与 id 的原始文本
        protected String rawMessageText;
        // 文本参数, 不包含指令与 id
        protected String[] messageTokens = null;
        protected ContactPermission permission;
        protected QQMessageChainInterface message;

        protected MessageContext(QQMessageChainInterface message) {
            this.message = message;
            this.messageText = message.getContentText();
            this.permission = service.getPermission(getSubjectId());
        }

        protected MessageContext() {
        }

        /**
         * 管理员命令
         */
        public boolean handleOpCommand() {
            if (!senderHasGroupPermission()) {
                return false;
            }

            var commandOperation = messageText.substring(config.getCommand().length()); // [on|off]
            String result;
            try {
                result = permission.handleEditCommand(commandOperation);
                if (result == null) {
                    return false;
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                result = e.getMessage();
            }
            replyMessage(config.getCommand() + ' ' + result);

            return true;
        }

        public void handleCommand() {
            if (this.messageText.isEmpty()) {
                return;
            }
            if (messageText.startsWith(config.getCommand())) { // pet (id?) (param?)
                var params = messageText.substring(config.getCommand().length()).trim();
                // 没有被 pet off command 禁用
                boolean isEnable = (permission.getCommandPermission() & ContactPermission.COMMAND) != 0;
                if (message.hasTarget() && isEnable) { // pet [@user|IMG] (id?) (param?)
                    if (isInCooldown()) {
                        replyCooldown();
                        return;
                    }
                    // 如果发送 pet 指令且有指定目标, 则使用随机模板
                    template = service.randomTemplate();
                    responseTemplate();
                    lockCooldown();
                } else if (params.isEmpty() && isEnable) { // pet
                    // 如果只发送了 pet 指令, 使用默认模板
                    // 默认模板不会受到 冷却时间 影响
                    template = service.getDefaultTemplate();
                    responseTemplate();
                } else if (handleOpCommand()) { // pet [on|off]
                    // 处理管理员指令, 匹配成功直接返回
                    return;
                } else if (isEnable) { // pet (id?) (param?)
                    if (isInCooldown()) {
                        replyCooldown();
                        return;
                    }
                    // 可能包含模板 id 与文本参数
                    var tokens = params.split(" +");
                    template = service.getTemplate(tokens[0]);
                    if (template == null) { // 匹配模板失败, 使用默认模板
                        // TODO: 可配置使用 随机模板 或 默认模板
                        template = service.getDefaultTemplate();
                        rawMessageText = params;
                    } else {
                        rawMessageText = params.substring(tokens[0].length()).trim();
                    }
                    messageTokens = Arrays.copyOfRange(tokens, 1, tokens.length);
                    responseTemplate();
                    lockCooldown();
                }
            } else if (messageText.startsWith(config.getCommandHead())) {
                if (isInCooldown()) {
                    replyCooldown();
                    return;
                }
                // #(id?) (param?)
                rawMessageText = messageText.substring(config.getCommandHead().length()).trim();
                var tokens = rawMessageText.split(" +");
                template = service.getTemplate(tokens[0]);
                if (template == null) return;
                rawMessageText = rawMessageText.substring(tokens[0].length()).trim();
                messageTokens = Arrays.copyOfRange(tokens, 1, tokens.length);
                responseTemplate();
                lockCooldown();
            }
        }

        protected void responseTemplate() {
            var request = buildRequestContext();
            if (template instanceof PetpetTemplateModel) {
                replyMessage(template.draw(request));
            } else if (template instanceof PetpetScriptModel) {
                var script = (PetpetScriptModel) this.template;
                var event = buildBotSendEvent(script, request);
                script.getEventManager().trigger("bot_send", event);
                replyMessage(event);
            }
        }

        protected RequestContext buildRequestContext() {
            // 构建图像列表
            List<QQMessageElement.ResizeableImageElement> imageList = message.stream()
                    .filter(ele -> ele.getType() == QQMessageElement.MessageType.AT ||
                            ele.getType() == QQMessageElement.MessageType.IMAGE)
                    .map(ele -> (QQMessageElement.ResizeableImageElement) ele)
                    .collect(Collectors.toList());

            if (message.getReplyImage() != null) {
                imageList.add(new QQMessageElement.ImageElement(message.getReplyImage()));
            }

            // 确保 imageList 至少有两个元素
            if (imageList.isEmpty()) {
                imageList.add(QQMessageElement.AtElement.from(getBotId(), getBotName())); // Bot
                imageList.add(QQMessageElement.AtElement.from(getSenderId(), getSenderName())); // Sender
            } else if (imageList.size() == 1) {
                imageList = List.of(
                        QQMessageElement.AtElement.from(getSenderId(), getSenderName()), // Sender
                        imageList.get(0) // Image
                );
            }

            return buildRequestContext(imageList);
        }

        protected RequestContext buildRequestContext(List<QQMessageElement.ResizeableImageElement> imageList) {
            // 构建 imageUrlMap 和 textMap
            Map<String, String> imageUrlMap;
            Map<String, String> textMap;
            if (template instanceof PetpetTemplateModel) {
                var petpetTemplate = (PetpetTemplateModel) template;
                imageUrlMap = buildImageUrlMap(imageList, petpetTemplate.getRequestImageKeys());
                textMap = buildTextMap(imageList, petpetTemplate.getRequestTextKeys());
            } else {
                imageUrlMap = buildImageUrlMap(imageList, ALL_IMAGE_KEYS);
                textMap = buildTextMap(imageList, ALL_TEXT_KEYS);
            }

            return new RequestContext(imageUrlMap, textMap);
        }

        protected Map<String, String> buildMap(Collection<String> keys, UnaryOperator<String> valueMapper) {
            var map = new HashMap<String, String>(keys.size());
            for (var key : keys) {
                var value = valueMapper.apply(key);
                if (value != null) {
                    map.put(key, valueMapper.apply(key));
                }
            }
            return map;
        }

        protected boolean isInCooldown() {
            if (inGroupContext()) {
                return Cooler.isLocked(getSenderId()) || Cooler.isLocked(getSubjectId());
            }
            return Cooler.isLocked(getSubjectId());
        }

        protected void replyCooldown() {
            var msg = config.getInCoolDownMessage();
            if (msg.isEmpty()) {
                return;
            } else if (service.cooldownReplyNudge) {
                replyNudge();
                return;
            }
            replyMessage(config.getInCoolDownMessage());
        }

        protected void lockCooldown() {
            long cooldownTime = permission.getCooldownTime();
            if (inGroupContext()) {
                long userCooldownTime = config.getUserCooldownTime();
                if (userCooldownTime > 0) {
                    Cooler.lock(getSenderId(), userCooldownTime);
                }
            }
            if (cooldownTime > 0) {
                Cooler.lock(getSubjectId(), cooldownTime);
            }
        }

        protected Map<String, String> buildImageUrlMap(
                List<QQMessageElement.ResizeableImageElement> imageList,
                Collection<String> keys
        ) {
            final var finalImageList = imageList;
            return buildMap(keys, key -> {
                switch (key) {
                    case FROM_KEY:
                        return finalImageList.get(finalImageList.size() - 2).getUrl(getAvatarSize(key));
                    case TO_KEY:
                        return finalImageList.get(finalImageList.size() - 1).getUrl(getAvatarSize(key));
                    case GROUP_KEY:
                        // TODO: 分别处理群头像与用户头像
                        return QQAvatarRequester.getAvatarUrlString(getSubjectId(), getAvatarSize(key));
                    case BOT_KEY:
                        return QQAvatarRequester.getAvatarUrlString(getBotId(), getAvatarSize(key));
                    default:
                        return getImageUrlByIndex(finalImageList, key);
                }
            });
        }

        protected Map<String, String> buildTextMap(
                List<QQMessageElement.ResizeableImageElement> imageList,
                Collection<String> keys
        ) {
            final var finalImageList = imageList;
            return buildMap(keys, key -> {
                switch (key) {
                    case FROM_KEY:
                        return finalImageList.get(finalImageList.size() - 2).getName();
                    case TO_KEY:
                        return finalImageList.get(finalImageList.size() - 1).getName();
                    case GROUP_KEY:
                        return getSubjectName();
                    case BOT_KEY:
                        return getBotName();
                    case RAW_TEXT_KEY:
                        return rawMessageText;
                    default:
                        return getMessageTokenByIndex(key);
                }
            });
        }

        protected @Nullable String getImageUrlByIndex(List<QQMessageElement.ResizeableImageElement> imageList, String key) {
            try {
                int i = Integer.parseInt(key);
                return imageList.get(i).getUrl(getAvatarSize(key));
            } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                return null;
            }
        }

        protected @Nullable String getMessageTokenByIndex(String key) {
            try {
                return messageTokens[Integer.parseInt(key)];
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                return null;
            }
        }

        protected int getAvatarSize(String key) {
            return service.getTemplateExpectedSize(template).getOrDefault(key, 0);
        }

        protected boolean inGroupContext() {
            return !getSubjectId().equals(getSenderId());
        }

        /**
         * 判断发送者是否为群聊管理员或拥有 bot 编辑权限
         */
        protected abstract boolean senderHasGroupPermission();

        protected abstract BotSendEvent buildBotSendEvent(PetpetScriptModel script, RequestContext context);

        protected abstract String getBotName();

        protected abstract String getBotId();

        protected abstract String getSenderName();

        protected abstract String getSenderId();

        /**
         * 在群聊中为群名称, 在私聊中为用户名称
         */
        protected abstract String getSubjectName();

        /**
         * 在群聊中为群 id, 在私聊中为用户 id
         */
        protected abstract String getSubjectId();

        protected abstract void replyMessage(String text);

        protected abstract void replyMessage(EncodedImage image);

        protected abstract void replyMessage(BotSendEvent event);

        protected abstract void replyNudge();
    }
}
