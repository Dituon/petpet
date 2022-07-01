package xmmt.dituon.plugin;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.*;
import xmmt.dituon.share.BaseConfigFactory;
import xmmt.dituon.share.TextExtraData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class Petpet extends JavaPlugin {
    public static final Petpet INSTANCE = new Petpet();
    public static final float VERSION = 3.4F;

    ArrayList<Group> disabledGroup = new ArrayList<>();
    PluginPetService pluginPetService;

    private Petpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", String.valueOf(VERSION))
                .name("PetPet")
                .author("Dituon")
                .build());
        this.pluginPetService = new PluginPetService();
    }

    @Override
    public void onEnable() {
        getLogger().info(System.getProperty("sun.jnu.encoding"));

        try {
            this.reloadPluginConfig(PetPetAutoSaveConfig.INSTANCE);
        } catch (Exception ex) {
            ex.printStackTrace();
            getLogger().info("Mirai 2.11.0 提供了新的 JavaAutoSaveConfig 方法, 请更新Mirai版本至 2.11.0 (不是2.11.0-M1)\n若不想更新可使用本插件 2.0 版本");
        }

        pluginPetService.readConfigByPluginAutoSave();
        pluginPetService.readData(getDataFolder());

        if (pluginPetService.headless) {
            System.setProperty("java.awt.headless", "true");
        }

        getLogger().info("\n____ ____ ____ ____ ____ ____ \n| . \\| __\\|_ _\\| . \\| __\\|_ _\\\n" +
                "| __/|  ]_  || | __/|  ]_  || \n|/   |___/  |/ |/   |___/  |/ ");

        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, this::onGroupMessage);
        GlobalEventChannel.INSTANCE.subscribeAlways(NudgeEvent.class, this::onNudge);
    }

    private void onNudge(NudgeEvent e) {
        if (!(e.getSubject() instanceof Group) || isDisabled((Group) e.getSubject())) {
            return; // 如果禁用了petpet就返回
        }
        try {
            pluginPetService.sendImage((Group) e.getSubject(), (Member) e.getFrom(), (Member) e.getTarget(), true);
        } catch (Exception ex) { // 如果无法把被戳的对象转换为Member(只有Bot无法强制转换为Member对象)
            try {
                pluginPetService.sendImage((Group) e.getSubject(), (Member) e.getFrom(), ((Group) e.getSubject()).getBotAsMember(), true);
            } catch (Exception ignored) { // 如果bot戳了别人
                if (!pluginPetService.respondSelfNudge) {
                    return;
                }
                pluginPetService.sendImage((Group) e.getSubject(), ((Group) e.getSubject()).getBotAsMember(), (Member) e.getFrom(), true);
            }
        }
    }

    private void onGroupMessage(GroupMessageEvent e) {
        if (!e.getMessage().contains(PlainText.Key)) return;
        if (!pluginPetService.respondImage && !e.getMessage().contains(Image.Key)) return;
        if (!pluginPetService.commandMustAt && !e.getMessage().contains(At.Key)) return;

        String messageString = e.getMessage().contentToString().trim();
        if (!pluginPetService.keyCommand &&
                !messageString.startsWith(pluginPetService.command)) return;

        if (messageString.equals(pluginPetService.command + " off") &&
                !isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.add(e.getGroup());
            sendReplyMessage(e, "已禁用 " + pluginPetService.command);
            return;
        }

        if (messageString.equals(pluginPetService.command + " on") &&
                isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.remove(e.getGroup());
            sendReplyMessage(e, "已启用 " + pluginPetService.command);
            return;
        }

        if (messageString.equals(pluginPetService.command)) {
            e.getGroup().sendMessage("Petpet KeyList: \n" + pluginPetService.getKeyAliasListString());
            return;
        }

        String fromName = "我";
        String toName = "你";
        String groupName = "你群";

        StringBuilder messageText = new StringBuilder();
        String fromUrl = e.getBot().getAvatarUrl();
        String toUrl = e.getSender().getAvatarUrl();
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof PlainText) {
                messageText.append(singleMessage.contentToString()).append(' ');
                continue;
            }
            if (singleMessage instanceof At) {
                fromName = e.getSenderName();
                fromUrl = e.getSender().getAvatarUrl();

                Member to = e.getGroup().get(((At) singleMessage).getTarget());
                toName = "".equals(to.getNameCard()) ? to.getNick() : to.getNameCard();
                toUrl = to.getAvatarUrl();

                groupName = e.getGroup().getName();
                continue;
            }
            if (singleMessage instanceof Image) {
                fromUrl = e.getSender().getAvatarUrl();
                toUrl = Image.queryUrl((Image) singleMessage);
                groupName = e.getGroup().getName();
            }
        }

        String command = messageText.toString().trim();

        List<String> strList = new ArrayList<>(Arrays.asList(command.contains(" ") ?
                command.trim().split("\\s+") : new String[]{command})); //空格分割指令
        if (strList.get(0).equals(pluginPetService.command)) strList = strList.subList(1, strList.size()); //去掉command
        if (strList.isEmpty()) { //pet @xxx
            strList.add(pluginPetService.randomableList.get(
                    new Random().nextInt(pluginPetService.randomableList.size())));
        }

        if (!pluginPetService.getDataMap().containsKey(strList.get(0))) { //没有指定key
            if (pluginPetService.getAliaMap().containsKey(strList.get(0))) { //别名
                strList.set(0, pluginPetService.getAliaMap().get(strList.get(0)));
            } else {
                return;
            }
        }

        pluginPetService.sendImage(e.getGroup(), strList.get(0),
                BaseConfigFactory.getAvatarExtraDataFromUrls(
                        fromUrl, toUrl, e.getGroup().getAvatarUrl(), e.getBot().getAvatarUrl()
                ), new TextExtraData(
                        fromName, toName, groupName, strList.subList(1, strList.size())
                ));
    }

    private boolean isDisabled(Group group) {
        if (disabledGroup != null && !disabledGroup.isEmpty()) {
            return disabledGroup.contains(group);
        }
        return false;
    }

    public boolean isPermission(GroupMessageEvent e) {
        return e.getPermission() == MemberPermission.ADMINISTRATOR || e.getPermission() == MemberPermission.OWNER;
    }

    private void sendReplyMessage(GroupMessageEvent e, String text) {
        e.getGroup().sendMessage(new QuoteReply(e.getMessage()).plus(text));
    }
}