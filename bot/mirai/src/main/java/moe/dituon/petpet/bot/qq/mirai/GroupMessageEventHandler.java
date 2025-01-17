package moe.dituon.petpet.bot.qq.mirai;

import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.bot.qq.ReplyType;
import moe.dituon.petpet.bot.qq.avatar.QQAvatarRequester;
import moe.dituon.petpet.bot.qq.permission.ContactPermission;
import moe.dituon.petpet.bot.utils.Cooler;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.PetpetModel;
import moe.dituon.petpet.core.element.PetpetTemplateModel;
import moe.dituon.petpet.core.imgres.ImageResourceMap;
import moe.dituon.petpet.script.PetpetJsScriptModel;
import moe.dituon.petpet.script.PetpetScriptModel;
import net.mamoe.mirai.contact.AnonymousMember;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.util.*;

@Slf4j
public class GroupMessageEventHandler extends MiraiMessageEventHandler {
    protected final String command;
    private final MiraiBotService service;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    protected final boolean respondNudgeWhenCooldown;

    public GroupMessageEventHandler(MiraiBotService service) {
        this.service = service;
        this.command = service.miraiConfig.getCommand();
        this.respondNudgeWhenCooldown = "[nudge]".equals(service.miraiConfig.getInCoolDownMessage());
    }

    public void handle(GroupMessageEvent event) {
        var handler = new Handler(event);
        handler.handle();
    }

    protected class Handler {
        protected final GroupMessageEvent event;
        protected final String message;
        protected final ContactPermission permission;
        protected PetpetModel templateModel;
        protected boolean resultDefault = false;

        public Handler(GroupMessageEvent event) {
            this.event = event;
            this.permission = service.getPermission(event.getGroup().getId());

            var messageBuilder = new StringBuilder();
            for (SingleMessage ele : event.getMessage()) {
                if (ele instanceof PlainText) {
                    var text = (PlainText) ele;
                    messageBuilder.append(text.getContent()).append(' ');
                } else if (ele instanceof Image) {
                    int[] sourceIds = event.getMessage().get(MessageSource.Key).getIds();
                    if (sourceIds.length == 0) {
                        continue;
                    }

                    // message id = group id + source id
                    long id = event.getGroup().getId() + sourceIds[0];
                    service.getImageCachePool().put(id, Image.queryUrl((Image) ele));
                }
            }
            this.message = messageBuilder.toString();
        }

        public void handle() {
            if (handleOpCommand()) return;
            handleCommand();
        }

        protected boolean handleOpCommand() {
            if (!hasGroupPermission(this.event.getSender())) return false;

            if (!this.message.startsWith(command)) {
                return false;
            }

            var commandOperation = this.message.substring(command.length());
            String result;
            try {
                result = this.permission.handleEditCommand(commandOperation);
                if (result == null) {
                    return false;
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                result = e.getMessage();
            }
            this.sendMessage(command + ' ' + result);

            return true;
        }

        protected void handleCommand() {
            var commandHead = service.miraiConfig.getCommandHead();
            if (message.startsWith(command)) { // /pet (id?) (param?)
                if ((permission.getCommandPermission() & ContactPermission.COMMAND) == 0) {
                    // no permission
                    return;
                }
                var idAndParam = message.substring(command.length()).trim();
                if (idAndParam.isBlank()) { // /pet
                    boolean isUseDefaultTemplate = service.miraiConfig.getDefaultReplyType() != ReplyType.RANDOM;
                    handleIdAndParam(idAndParam, false, isUseDefaultTemplate);
                    return;
                }
                handleIdAndParam(idAndParam, false, false); // (id?) (param?)
            } else if (commandHead.isEmpty() || message.startsWith(commandHead)) { // #(id?) (param?)
                if ((permission.getCommandPermission() & ContactPermission.COMMAND_HEAD) == 0) {
                    // no permission
                    return;
                }
                var idAndParam = message.substring(commandHead.length()).trim();
                handleIdAndParam(idAndParam, true, false);
            }
        }

        /**
         * @param idAndParam   (id?) (param?)
         * @param idIsRequired if id is undefined, use random id or default template
         */
        protected void handleIdAndParam(String idAndParam, boolean idIsRequired, boolean useDefaultTemplate) {
            var tokens = idAndParam.split(" +"); // [id, ...params]
            String idOrAlias = tokens.length > 0 ? tokens[0].replace("\n", "") : "";
            var model = resolveTemplateModel(idOrAlias, idIsRequired, useDefaultTemplate);

            if (model == null) return;

            if (Cooler.isLocked(event.getGroup().getId()) || Cooler.isLocked(event.getSender().getId())) {
                if (respondNudgeWhenCooldown && !(event.getSender() instanceof AnonymousMember)) {
                    event.getSender().nudge().sendTo(event.getGroup());
                    return;
                }
                if (service.miraiConfig.getInCoolDownMessage().isBlank()) return;
                sendMessage(service.miraiConfig.getInCoolDownMessage());
                return;
            }
            this.templateModel = model;

            // TODO: cache request map
            var context = buildRequestContext(
                    tokens.length > 0 ? Arrays.copyOfRange(tokens, 1, tokens.length) : EMPTY_STRING_ARRAY,
                    tokens.length > 0 ? idAndParam.substring(idOrAlias.length()).trim() : ""
            );
            sendImage(model, context);

            if (!resultDefault) {
                Cooler.lock(event.getGroup().getId(), permission.getCooldownTime());
                Cooler.lock(event.getSender().getId(), service.miraiConfig.getUserCoolDown());
            }
        }

        /**
         * 根据 ID 或别名解析模板模型，如果找不到则根据参数选择默认模板或随机模板。
         */
        private @Nullable PetpetModel resolveTemplateModel(String idOrAlias, boolean idIsRequired, boolean useDefaultTemplate) {
            if (idOrAlias.isEmpty()) {
                if (idIsRequired) return null;
                this.resultDefault = useDefaultTemplate;
                return useDefaultTemplate ? service.getDefaultTemplate() : service.randomTemplate();
            }

            var model = service.getTemplate(idOrAlias);
            if (model == null && !idIsRequired) {
                this.resultDefault = useDefaultTemplate;
                model = useDefaultTemplate ? service.getDefaultTemplate() : service.randomTemplate();
            }
            return model;
        }

        /**
         * @param params [id, ...params]
         */
        protected RequestContext buildRequestContext(String[] params, String rawParams) {
            var textMap = new HashMap<String, String>(params.length + 8);
            textMap.put("raw", rawParams);
            for (int i = 0; i < params.length; i++) {
                // text variable e.g. text${1}
                textMap.put(String.valueOf(i), params[i]);
            }

            boolean ignoreAt = false;
            // default subject is BOT
            Member from = event.getGroup().getBotAsMember();
            // default target is sender
            Member to = event.getSender();
            var customImageList = new ArrayList<String>(event.getMessage().size() - 1);
            for (SingleMessage ele : event.getMessage()) {
                if (ele instanceof At && !ignoreAt) {
                    if ((permission.getCommandPermission() & ContactPermission.AT) == 0) {
                        continue;
                    }
                    At at = (At) ele;
                    // ignored at self
                    if (at.getTarget() == event.getSender().getId()) continue;

                    // form = sender; to = at target
                    from = event.getSender();
                    to = event.getGroup().get(at.getTarget());
                    assert to != null;
                } else if (ele instanceof QuoteReply) {
                    if ((permission.getCommandPermission() & ContactPermission.IMAGE) == 0) {
                        continue;
                    }
                    var reply = (QuoteReply) ele;
                    var replySourceIds = reply.getSource().getIds();
                    if (replySourceIds.length == 0) continue;
                    // message id = group id + source id
                    long msgId = event.getGroup().getId() + replySourceIds[0];

                    var sourceImgSrc = service.getImageCachePool().get(msgId);
                    if (sourceImgSrc == null) continue;
                    customImageList.add(sourceImgSrc);
                    from = event.getSender();
                    ignoreAt = true;
                } else if (ele instanceof Image) {
                    if ((permission.getCommandPermission() & ContactPermission.IMAGE) == 0) {
                        continue;
                    }
                    customImageList.add(Image.queryUrl((Image) ele));
                    from = event.getSender();
                    ignoreAt = true;
                }
            }

            String toName = getNameOrNick(to);
            // get template element expected size to query avatar image
            String toUrl = getAvatarUrl(to.getId(), TO_KEY);
            String fromName = getNameOrNick(from);
            String fromUrl = getAvatarUrl(to.getId(), FROM_KEY);
            List<String> randomImageList = null;

            // customImageList: [to, form, ...random]
            if (!customImageList.isEmpty()) {
                if (customImageList.size() > 1) {
                    // stay from sender name
                    fromUrl = customImageList.get(1);
                    if (customImageList.size() > 2) {
                        // TODO: RandomImageResource
//                        randomImageList = customImageList.subList(2, customImageList.size());
                    }
                }
                toUrl = customImageList.get(0);
                toName = "这个";
            }

            textMap.put(FROM_KEY, fromName);
            textMap.put(TO_KEY, toName);
            textMap.put(GROUP_KEY, event.getGroup().getName());
            textMap.put(BOT_KEY, getNameOrNick(event.getGroup().getBotAsMember()));
            textMap.put(FROM_ID_KEY, String.valueOf(from.getId()));
            textMap.put(TO_ID_KEY, String.valueOf(to.getId()));
            textMap.put(GROUP_ID_KEY, String.valueOf(event.getGroup().getId()));
            textMap.put(BOT_ID_KEY, String.valueOf(event.getBot().getId()));

            try {
                return new RequestContext(
                        new ImageResourceMap(Map.of(
                                FROM_KEY, QQAvatarRequester.getAvatarResource(fromUrl),
                                TO_KEY, QQAvatarRequester.getAvatarResource(toUrl),
                                GROUP_KEY, QQAvatarRequester.getAvatarResource(
                                        getAvatarUrl(event.getGroup().getId(), GROUP_KEY)
                                ),
                                BOT_KEY, QQAvatarRequester.getAvatarResource(
                                        getAvatarUrl(event.getBot().getId(), BOT_KEY)
                                )
                        )),
                        textMap
                );
            } catch (MalformedURLException e) {
                // never
                throw new IllegalStateException(e);
            }
        }

        protected String getAvatarUrl(long qqId, String key) {
            return QQAvatarRequester.getAvatarUrlString(
                    qqId,
                    service.getTemplateExpectedSize(templateModel)
                            .getOrDefault(key, 640)
            );
        }

        protected void sendImage(PetpetModel model, RequestContext context) {
            if (model instanceof PetpetScriptModel) {
                var scriptModel = (PetpetScriptModel) model;
                var basePath = scriptModel instanceof PetpetJsScriptModel ?
                        ((PetpetJsScriptModel) scriptModel).basePath : null;
                var scriptEvent = new ScriptMiraiBotSendEvent(this.event, context, basePath);
                if (scriptModel.getEventManager().has("bot_send")) {
                    scriptModel.getEventManager().trigger("bot_send", scriptEvent);
                }
                this.event.getGroup().sendMessage(new QuoteReply(event.getMessage())
                        .plus(scriptEvent.getResponseMessage())
                );
            } else if (model instanceof PetpetTemplateModel) {
                var img = model.draw(context);
                try {
                    sendMessage(
                            service.uploadImage(img.bytes, event.getGroup())
                    );
                } catch (Exception ex) {
                    sendMessage("上传图像时出错: " + ex.getMessage());
                    log.warn("上传图像时出错", ex);
                }
            } else {
                // never
                throw new IllegalStateException();
            }
        }

        protected void sendMessage(String msg) {
            event.getGroup().sendMessage(new QuoteReply(event.getMessage()).plus(msg));
        }

        protected void sendMessage(Image image) {
            event.getGroup().sendMessage(new QuoteReply(event.getMessage()).plus(image));
        }
    }
}

