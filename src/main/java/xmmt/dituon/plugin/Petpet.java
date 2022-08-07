package xmmt.dituon.plugin;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.*;
import xmmt.dituon.share.BaseConfigFactory;
import xmmt.dituon.share.TextExtraData;

import java.io.File;
import java.util.*;

public final class Petpet extends JavaPlugin {
    public static final Petpet INSTANCE = new Petpet();
    public static final float VERSION = 4.2F;

    private static final ArrayList<Group> disabledGroup = new ArrayList<>();
    public static PluginPetService service;
    public static File dataFolder;

    private static MessageSource previousMessage;
    private static NudgeEvent previousNudge;

    private static HashMap<Long, String> imageCachePool;

    private Petpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", String.valueOf(VERSION))
                .name("PetPet")
                .author("Dituon")
                .build());
        service = new PluginPetService();
    }

    @Override
    public void onEnable() {
        try {
            this.reloadPluginConfig(PetPetAutoSaveConfig.INSTANCE);
            service.readConfigByPluginAutoSave();
        } catch (NoClassDefFoundError ignored) {
            getLogger().error("Mirai 2.11.0 提供了新的 JavaAutoSaveConfig 方法, 请更新Mirai版本至 2.11.0 (不是2.11.0-M1)\n使用旧版本将无法配置config");
        }

        dataFolder = getDataFolder();
        service.readData(dataFolder);

        if (service.headless) System.setProperty("java.awt.headless", "true");
        if (service.autoUpdate) new Thread(DataUpdater::autoUpdate).start();

        getLogger().info("\n             _                _   \n  _ __   ___| |_   _ __   ___| |_ \n" +
                " | '_ \\ / _ \\ __| | '_ \\ / _ \\ __|\n | |_) |  __/ |_  | |_) |  __/ |_ \n" +
                " | .__/ \\___|\\__| | .__/ \\___|\\__|\n |_|              |_|             v" + VERSION);

        GlobalEventChannel.INSTANCE.subscribeAlways(NudgeEvent.class,
                service.messageSynchronized ? this::onNudgeSynchronized : this::onNudge);
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class,
                service.messageSynchronized ? this::onGroupMessageSynchronized : this::onGroupMessage);
        if (service.respondReply) {
            imageCachePool = new HashMap<>();
            GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, this::cacheMessageImage);
            GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessagePostSendEvent.class, this::cacheMessageImage);
        }
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
        // 如果禁用了petpet就返回
        if (!(e.getSubject() instanceof Group) || isDisabled((Group) e.getSubject())) return;
        try {
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

        if (messageString.equals(service.command + " off") &&
                !isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.add(e.getGroup());
            sendReplyMessage(e, "已禁用 " + service.command);
            return;
        }

        if (messageString.equals(service.command + " on") &&
                isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.remove(e.getGroup());
            sendReplyMessage(e, "已启用 " + service.command);
            return;
        }

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
                    List<String> keyList = new ArrayList<>();
                    keyList.add(service.getKeyAliasListString());
                    service.sendImage(e.getGroup(), "key_list",
                            BaseConfigFactory.getGifAvatarExtraDataFromUrls(null, null, null, null),
                            new TextExtraData("", "", "", keyList));
                    break;
            }
            return;
        }

        String key = null;

        boolean fuzzyLock = false; //锁住模糊匹配

        String fromName = getNameOrNick(e.getGroup().getBotAsMember());
        String toName = e.getSenderName();
        String groupName = e.getGroup().getName();

        StringBuilder messageText = new StringBuilder();
        String fromUrl = e.getBot().getAvatarUrl();
        String toUrl = e.getSender().getAvatarUrl();
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof QuoteReply && service.respondReply) {
                long id = e.getGroup().getId() + ((QuoteReply) singleMessage).getSource().getIds()[0];
                toUrl = imageCachePool.get(id) != null ? imageCachePool.get(id) : toUrl;
                continue;
            }
            if (singleMessage instanceof PlainText) {
                String text = singleMessage.contentToString();
                messageText.append(text).append(' ');
                continue;
            }
            if (singleMessage instanceof At) {
                fromName = getNameOrNick(e.getSender());
                fromUrl = e.getSender().getAvatarUrl();

                Member to = e.getGroup().get(((At) singleMessage).getTarget());
                toName = getNameOrNick(to);
                toUrl = to.getAvatarUrl();

                fuzzyLock = true;
                continue;
            }
            if (singleMessage instanceof Image) {
                fromName = getNameOrNick(e.getSender());
                fromUrl = e.getSender().getAvatarUrl();
                toName = "这个";
                toUrl = Image.queryUrl((Image) singleMessage);
                fuzzyLock = true;
            }
        }

        String commandData = messageText.toString().trim();
        ArrayList<String> spanList = new ArrayList<>(Arrays.asList(commandData.trim().split("\\s+")));
        if (spanList.isEmpty()) return;

        if (service.command.equals(spanList.get(0))) {
            spanList.remove(0); //去掉指令头
            key = service.randomableList.get(new Random().nextInt(service.randomableList.size())); //随机key
        }

        if (!spanList.isEmpty()) {
            if (service.getDataMap().containsKey(spanList.get(0))) key = spanList.get(0); //key
            else if (service.getAliaMap().containsKey(spanList.get(0))) { //别名
                String[] keys = service.getAliaMap().get(spanList.get(0));
                key = keys[new Random().nextInt(keys.length)];
            }
        }

        if (key == null) return;

        if (service.fuzzy && !spanList.isEmpty() && !fuzzyLock) {
            for (Member m : e.getGroup().getMembers()) {
                if (m.getNameCard().toLowerCase().contains(spanList.get(0).toLowerCase())
                        || m.getNick().toLowerCase().contains(spanList.get(0).toLowerCase())) {
                    fromName = getNameOrNick(e.getSender());
                    fromUrl = e.getSender().getAvatarUrl();
                    toName = getNameOrNick(m);
                    toUrl = m.getAvatarUrl();
                }
            }
        }

        service.sendImage(e.getGroup(), key,
                BaseConfigFactory.getGifAvatarExtraDataFromUrls(
                        fromUrl, toUrl, e.getGroup().getAvatarUrl(), e.getBot().getAvatarUrl()
                ), new TextExtraData(
                        fromName, toName, groupName, spanList
                ));
    }

    private void cacheMessageImage(GroupMessageEvent e) {
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof Image) {
                if (imageCachePool.size() >= service.cachePoolSize) imageCachePool.clear();
                long id = e.getGroup().getId() + e.getMessage().get(MessageSource.Key).getIds()[0];
                imageCachePool.put(id, Image.queryUrl((Image) singleMessage));
                return;
            }
        }
    }


    private void cacheMessageImage(GroupMessagePostSendEvent e) {
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof Image) {
                if (imageCachePool.size() >= service.cachePoolSize) imageCachePool.clear();
                //GroupMessagePostSendEvent获取的MessageChain不包含MessageSource
                long id = e.getTarget().getId() + e.getMessage().get(MessageSource.Key).getIds()[0];
                imageCachePool.put(id, Image.queryUrl((Image) singleMessage));
                return;
            }
        }
    }


    private boolean isDisabled(Group group) {
        if (disabledGroup != null && !disabledGroup.isEmpty()) {
            return disabledGroup.contains(group);
        }
        return false;
    }

    private String getNameOrNick(Member m) {
        return "".equals(m.getNameCard()) ? m.getNick() : m.getNameCard();
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