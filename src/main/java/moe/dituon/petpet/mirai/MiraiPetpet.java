package moe.dituon.petpet.mirai;

import kotlin.Pair;
import moe.dituon.petpet.plugin.*;
import moe.dituon.petpet.share.BaseConfigFactory;
import moe.dituon.petpet.share.BasePetService;
import moe.dituon.petpet.share.TextExtraData;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent;
import net.mamoe.mirai.event.events.MessagePreSendEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MiraiPetpet extends JavaPlugin {
    public static final MiraiPetpet INSTANCE = new MiraiPetpet();
    private static List<Long> disabledGroup;
    public static MiraiPetService service;
    public static File dataFolder;

    private static MessageSource previousMessage;
    private static NudgeEvent previousNudge;

    private static LinkedHashMap<Long, String> imageCachePool;
    private static Set<String> keyAliaSet = null;
    public static final Random random = new Random();
    private static final Pattern pattern = Pattern.compile("<pet>([\\s\\S]*?)</pet>", Pattern.MULTILINE);

    private MiraiPetpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", String.valueOf(BasePetService.VERSION))
                .name("PetPet")
                .author("Dituon")
                .build());
        service = new MiraiPetService();
    }

    @Override
    public void onEnable() {
        System.setProperty("sun.java2d.opengl", "true");
        try {
            reloadPluginConfig(MiraiPluginConfig.INSTANCE);
            service.readConfigByPluginAutoSave();
        } catch (NoClassDefFoundError ignored) {
            getLogger().error("Mirai 2.11.0 提供了新的 JavaAutoSaveConfig 方法, 请更新Mirai版本至 2.11.0 (不是2.11.0-M1)\n使用旧版本将无法配置config");
        }

        dataFolder = getDataFolder();
        service.readData(dataFolder);

        if (service.headless) System.setProperty("java.awt.headless", "true");
        if (service.autoUpdate) new Thread(() -> {
            DataUpdater updater = new DataUpdater(service.repositoryUrl, getDataFolder());
            updater.autoUpdate();
        }).start();
        disabledGroup = service.disabledGroups;

        getLogger().info("\n\n" +
                "    ██████╗ ███████╗████████╗██████╗ ███████╗████████╗\n" +
                "    ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔════╝╚══██╔══╝\n" +
                "    ██████╔╝█████╗     ██║   ██████╔╝█████╗     ██║   \n" +
                "    ██╔═══╝ ██╔══╝     ██║   ██╔═══╝ ██╔══╝     ██║   \n" +
                "    ██║     ███████╗   ██║   ██║     ███████╗   ██║   \n" +
                "    ╚═╝     ╚══════╝   ╚═╝   ╚═╝     ╚══════╝   ╚═╝     " +
                "v" + BasePetService.VERSION + "\n");

        if (service.probability > 0) GlobalEventChannel.INSTANCE.subscribeAlways(NudgeEvent.class,
                service.messageSynchronized ? this::onNudgeSynchronized : this::onNudge);

        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class,
                service.messageSynchronized ? this::onGroupMessageSynchronized : this::onGroupMessage);

        if (service.respondReply) {
            imageCachePool = new LinkedHashMap<>(service.cachePoolSize, 0.75f, true) {
                @Override
                public boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > service.cachePoolSize;
                }
            };
            GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, this::cacheMessageImage);
            GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessagePostSendEvent.class, this::cacheMessageImage);
        }

        if (service.messageHook)
            GlobalEventChannel.INSTANCE.subscribeAlways(MessagePreSendEvent.class, this::onMessagePreSend);
    }

    private void onNudgeSynchronized(NudgeEvent e) {
        synchronized (this) {
            if (nudgeEventAreEqual(previousNudge, e)) return;
            previousNudge = e;
        }
        responseNudge(e);
    }

    private void onNudge(NudgeEvent e) {
        responseNudge(e);
    }

    private void responseNudge(NudgeEvent e) {
        //Cooldown时间
        if (Cooler.isLocked(e.getFrom().getId())) return;
        // 如果禁用了petpet就返回
        if ((!(e.getSubject() instanceof Group) || isDisabled((Group) e.getSubject()))
                && service.nudgeCanBeDisabled) return;
        try {
            Cooler.lock(e.getFrom().getId(), service.coolDown);
            service.sendImage((Group) e.getSubject(), (Member) e.getFrom(), (Member) e.getTarget(), true);
        } catch (Exception ex) { // 如果无法把被戳的对象转换为Member(只有Bot无法强制转换为Member对象)
            try {
                service.sendImage((Group) e.getSubject(), ((Group) e.getSubject()).getBotAsMember(), (Member) e.getFrom(), true);
            } catch (Exception ignored) { // 如果bot戳了别人
                if (!service.respondSelfNudge) return;
                service.sendImage((Group) e.getSubject(), ((Group) e.getSubject()).getBotAsMember(), (Member) e.getFrom(), true);
            }
        }
    }

    private void onGroupMessageSynchronized(GroupMessageEvent e) {
        synchronized (this) {
            MessageSource thisMessageSource = e.getMessage().get(MessageSource.Key);
            for (Bot bot : Bot.getInstances()) { // 过滤其它bot发出的消息
                if (previousMessage != null && thisMessageSource.getFromId() == bot.getId()) return;
            }
            if (messageSourceAreEqual(previousMessage, thisMessageSource)) return;
            previousMessage = thisMessageSource;
        }
        responseMessage(e);
    }

    private void onGroupMessage(GroupMessageEvent e) {
        responseMessage(e);
    }

    private void responseMessage(GroupMessageEvent e) {
        if (!e.getMessage().contains(PlainText.Key)) return;

        String messageString = e.getMessage().contentToString().trim();

        if (service.devMode && messageString.equals(service.command + " reload")) {
            e.getGroup().sendMessage(service.command + "正在重载...");
            service.readData(dataFolder);
            e.getGroup().sendMessage(service.command + "重载完成!");
            return;
        }

        if (messageString.equals(service.command + " off") &&
                !isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.add(e.getGroup().getId());
            sendReplyMessage(e, "已禁用 " + service.command);
            return;
        }

        if (messageString.equals(service.command + " on") &&
                isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.remove(e.getGroup().getId());
            sendReplyMessage(e, "已启用 " + service.command);
            return;
        }

        if (service.messageCanBeDisabled && isDisabled(e.getGroup())) return;

        if (messageString.equals(service.command)) {
            switch (service.replyFormat) {
                case MESSAGE:
                    e.getGroup().sendMessage("Petpet KeyList: \n" + service.getKeyAliasListString());
                    break;
                case FORWARD:
                    ForwardMessageBuilder builder = new ForwardMessageBuilder(e.getGroup());
                    builder.add(e.getBot().getId(), "petpet!",
                            new PlainText("Petpet KeyList: \n" + service.getKeyAliasListString()));
                    e.getGroup().sendMessage(builder.build());
                    break;
                case IMAGE:
                    if (service.getDataMap().get("key_list") == null) {
                        getLogger().error("未找到PetData/key_list, 无法进行图片构造");
                        e.getGroup().sendMessage("[ERROR]未找到PetData/key_list\n" + service.getKeyAliasListString());
                        return;
                    }
                    List<String> keyList = List.of(service.getKeyAliasListString());
                    service.sendImage(e.getGroup(), "key_list",
                            BaseConfigFactory.getGifAvatarExtraDataFromUrls(null, null, null, null, null),
                            new TextExtraData("", "", "", keyList));
                    break;
            }
            return;
        }

        boolean fuzzyLock = false; //锁住模糊匹配
        boolean hasImage = false; //匹配到多张图片特殊处理

        String fromName = getNameOrNick(e.getGroup().getBotAsMember()),
                fromUrl = e.getBot().getAvatarUrl(),
                toName = e.getSenderName(),
                toUrl = e.getSender().getAvatarUrl(),
                groupName = e.getGroup().getName();

        StringBuilder messageText = new StringBuilder();
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof QuoteReply && service.respondReply) {
                long id = e.getGroup().getId() + ((QuoteReply) singleMessage).getSource().getIds()[0];
                if (imageCachePool.get(id) == null) continue;
                toName = "这个";
                toUrl = imageCachePool.get(id);
                fuzzyLock = true;
                continue;
            }
            if (singleMessage instanceof PlainText) {
                String text = singleMessage.contentToString();
                messageText.append(text).append(' ');
                continue;
            }
            if (singleMessage instanceof At && !fuzzyLock) {
                fuzzyLock = true;

                At at = (At) singleMessage;
                if (at.getTarget() == e.getSender().getId()) continue;

                fromName = getNameOrNick(e.getSender());
                fromUrl = e.getSender().getAvatarUrl();

                Member to = e.getGroup().get(at.getTarget());
                assert to != null;
                toName = getNameOrNick(to);
                toUrl = to.getAvatarUrl();
                continue;
            }
            if (singleMessage instanceof Image) {
                String url = Image.queryUrl((Image) singleMessage);
                if (hasImage) {
                    fromUrl = url;
                    continue;
                }
                fromName = getNameOrNick(e.getSender());
                fromUrl = e.getSender().getAvatarUrl();
                toName = "这个";
                toUrl = url;
                fuzzyLock = true;
                hasImage = true;
            }
        }

        String commandData = messageText.toString().trim();
        ArrayList<String> spanList = new ArrayList<>(Arrays.asList(commandData.split("\\s+")));
        if (spanList.isEmpty()) return;

        String key = null;
        if (service.command.equals(spanList.get(0))) {
            spanList.remove(0); //去掉指令头
            key = service.randomableList.get(random.nextInt(service.randomableList.size())); //随机key
        }

        if (!spanList.isEmpty() && !service.strictCommand) { //匹配非标准格式指令
            if (keyAliaSet == null) { //按需初始化
                keyAliaSet = new HashSet<>(service.getDataMap().keySet());
                keyAliaSet.addAll(service.getAliaMap().keySet());
                keyAliaSet = keyAliaSet.stream()
                        .map(str -> str = service.commandHead + str)
                        .collect(Collectors.toSet());
            }
            for (String k : keyAliaSet) {
                if (!spanList.get(0).startsWith(k)) break;
                String span = spanList.set(0, k);
                if (span.length() != k.length()) {
                    spanList.add(1, span.substring(k.length()));
                }
            }
        }

        if (!spanList.isEmpty()) {
            String firstSpan = spanList.get(0);
            if (firstSpan.startsWith(service.commandHead)) {
                spanList.set(0, firstSpan = firstSpan.substring(service.commandHead.length()));
            } else {
                return;
            }

            if (service.getDataMap().containsKey(firstSpan)) { //key
                key = spanList.remove(0);
            } else if (service.getAliaMap().containsKey(firstSpan)) { //别名
                String[] keys = service.getAliaMap().get(spanList.remove(0));
                key = keys[random.nextInt(keys.length)];
            }
        }

        if (key == null) return;

        if (service.fuzzy && !spanList.isEmpty() && !fuzzyLock) {
            for (Member m : e.getGroup().getMembers()) {
                if (m.getNameCard().toLowerCase().contains(spanList.get(0).toLowerCase())
                        || m.getNick().toLowerCase().contains(spanList.get(0).toLowerCase())) {
                    if (e.getSender().getId() == m.getId()) break;
                    fromName = getNameOrNick(e.getSender());
                    fromUrl = e.getSender().getAvatarUrl();
                    toName = getNameOrNick(m);
                    toUrl = m.getAvatarUrl();
                    break;
                }
            }
        }

        if (Cooler.isLocked(e.getSender().getId()) || Cooler.isLocked(e.getGroup().getId())) {
            if (service.inCoolDownNudge && !(e.getSender() instanceof AnonymousMember)) {
                e.getSender().nudge().sendTo(e.getGroup());
                return;
            }
            if (service.inCoolDownMessage == null) return;
            sendReplyMessage(e, service.inCoolDownMessage);
            return;
        }
        Cooler.lock(e.getSender().getId(), service.coolDown);
        Cooler.lock(e.getGroup().getId(), service.groupCoolDown);

        service.sendImage(e.getGroup(), key,
                BaseConfigFactory.getGifAvatarExtraDataFromUrls(
                        fromUrl, toUrl, e.getGroup().getAvatarUrl(), e.getBot().getAvatarUrl(),
                        e.getGroup().getMembers().stream().map(NormalMember::getAvatarUrl).collect(Collectors.toList())
                ), new TextExtraData(
                        fromName, toName, groupName, spanList
                ));
    }

    private void onMessagePreSend(MessagePreSendEvent e) {
        String messageRaw = e.getMessage().contentToString();
        final Matcher matcher = pattern.matcher(messageRaw);

        boolean flag = false;
        List<Pair<String, Image>> pairs = null;
        while (matcher.find()) {
            flag = true;
            if (pairs == null) pairs = new ArrayList<>(4);
            try {
                Image image = service.inputStreamToImage(
                        new PluginRequestParser(matcher.group(1), e.getTarget()).getImagePair().getFirst(),
                        e.getTarget()
                );
                pairs.add(new Pair<>(matcher.group(0), image));
            } catch (IOException ex) {
                throw new RuntimeException("构造图片失败", ex);
            }
        }
        if (!flag) return;
        MessageChainBuilder messageBuilder = new MessageChainBuilder();
        MessageChain originChain = e.getMessage() instanceof MessageChain ?
                (MessageChain) e.getMessage() : MessageUtils.newChain(e.getMessage());
        short i = 0;
        for (SingleMessage message : originChain) {
            if (!(message instanceof PlainText)) {
                messageBuilder.add(message);
                continue;
            }
            String msgStr = ((PlainText) message).getContent();
            while (i < pairs.size()) {
                Pair<String, Image> pair = pairs.get(i);
                String typeStr = pair.getFirst();
                int typePos = msgStr.indexOf(typeStr);
                if (typePos == -1) break;
                i++;
                messageBuilder.add(msgStr.substring(0, typePos));
                messageBuilder.add(pair.getSecond());
                msgStr = msgStr.substring(typePos + typeStr.length());
            }
            messageBuilder.add(msgStr);
        }
        e.setMessage(messageBuilder.asMessageChain());
    }

    private void cacheMessageImage(GroupMessageEvent e) {
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof Image) {
                long id = e.getGroup().getId() + e.getMessage().get(MessageSource.Key).getIds()[0];
                imageCachePool.put(id, Image.queryUrl((Image) singleMessage));
                return;
            }
        }
    }

    private void cacheMessageImage(GroupMessagePostSendEvent e) {
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof Image) {
                try {
                    assert e.getReceipt() != null;
                    long id = e.getTarget().getId() + e.getReceipt().getSource().getIds()[0];
                    imageCachePool.put(id, Image.queryUrl((Image) singleMessage));
                    return;
                } catch (Exception ignore) {
                }
            }
        }
    }


    private boolean isDisabled(Group group) {
        if (disabledGroup != null && !disabledGroup.isEmpty()) {
            return disabledGroup.contains(group.getId());
        }
        return false;
    }

    private String getNameOrNick(Member m) {
        return m.getNameCard().isEmpty() ? m.getNick() : m.getNameCard();
    }

    public boolean isPermission(GroupMessageEvent e) {
        return e.getPermission() == MemberPermission.ADMINISTRATOR || e.getPermission() == MemberPermission.OWNER;
    }

    private void sendReplyMessage(GroupMessageEvent e, String text) {
        e.getGroup().sendMessage(new QuoteReply(e.getMessage()).plus(text));
    }

    private boolean nudgeEventAreEqual(NudgeEvent nudge1, NudgeEvent nudge2) {
        if (nudge1 == null || nudge2 == null) return false;
        return !nudge1.getBot().equals(nudge2.getBot()) //bot不能相同
                && nudge1.getFrom().getId() == nudge2.getFrom().getId()
                && nudge1.getTarget().getId() == nudge2.getTarget().getId()
                && nudge1.getSubject().getId() == nudge2.getSubject().getId();
    }

    private boolean messageSourceAreEqual(MessageSource source1, MessageSource source2) {
        if (source1 == null || source2 == null) return false;
        return source1.getTargetId() == source2.getTargetId()
                && Arrays.equals(source1.getIds(), source2.getIds());
    }
}