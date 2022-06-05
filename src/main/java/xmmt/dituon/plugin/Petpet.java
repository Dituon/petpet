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

import java.util.ArrayList;

public final class Petpet extends JavaPlugin {
    public static final Petpet INSTANCE = new Petpet();

    ArrayList<Group> disabledGroup = new ArrayList<>();
    PluginPetService pluginPetService;

    private Petpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", "2.4")
                .name("PetPet")
                .author("Dituon")
                .build());
        this.pluginPetService = new PluginPetService();
    }

    @Override
    public void onEnable() {
        this.reloadPluginConfig(PetPetAutoSaveConfig.INSTANCE);

        pluginPetService.readConfigByPluginAutoSave();
        try {
            pluginPetService.readData(getDataFolder());
        } catch (Exception ex) {
            ex.printStackTrace();
            getLogger().info("Mirai 2.11.0 提供了新的 JavaAutoSaveConfig 方法, 请更新Mirai版本至 2.11.0 (不是2.11.0-M1)\n若不想更新可使用本插件 2.0 版本");
        }
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, this::onGroupMessage);
        GlobalEventChannel.INSTANCE.subscribeAlways(NudgeEvent.class, this::onNudge);
    }

    private void onNudge(NudgeEvent e) {
        if (isDisabled((Group) e.getSubject())) {
            return; // 如果禁用了petpet就返回
        }
        try {
            pluginPetService.sendImage((Group) e.getSubject(), (Member) e.getFrom(), (Member) e.getTarget(), true);
        } catch (Exception ex) { // 如果无法把被戳的对象转换为Member(只有Bot无法强制转换为Member对象)
            pluginPetService.sendImage((Group) e.getSubject(), (Member) e.getFrom(), ((Group) e.getSubject()).getBotAsMember(), true);
        }
    }

    private void onGroupMessage(GroupMessageEvent e) {
        if (e.getMessage().contentToString().equals((pluginPetService.command + " off")) && !isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.add(e.getGroup());
            sendReplyMessage(e, "已禁用 " + pluginPetService.command);
            return;
        }

        if (e.getMessage().contentToString().equals((pluginPetService.command + " on")) && isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.remove(e.getGroup());
            sendReplyMessage(e, "已启用 " + pluginPetService.command);
            return;
        }

        if (pluginPetService.keyCommand) {
            String key = null;
            for (Message m : e.getMessage()) {
                if (m instanceof PlainText &&
                        pluginPetService.dataMap.containsKey(m.contentToString().replace(" ", ""))) {
                    key = m.contentToString().replace(" ", "");
                    continue;
                }
                if (m instanceof Image && key != null) {
                    respondImage(e.getGroup(), e.getSender(), Image.queryUrl((Image) m), key);
                    return;
                }
                if (m instanceof At && key != null) {
                    At at = (At) m;
                    pluginPetService.sendImage(e.getGroup(), e.getSender(), e.getGroup().get(at.getTarget()), key);
                    return;
                }
            }
        }

        if (!isDisabled(e.getGroup()) && e.getMessage().contentToString().startsWith(pluginPetService.command)) {
            if (pluginPetService.respondImage && e.getMessage().contains(Image.Key)) {
                String toURL = null;
                for (Message m : e.getMessage()) {
                    if (m instanceof Image) {
                        toURL = Image.queryUrl((Image) m);
                        getLogger().info(toURL);
                        continue;
                    }
                    if (m instanceof PlainText && toURL != null && !m.contentToString().endsWith(" ")) {
                        respondImage(e.getGroup(), e.getSender(), toURL, m.contentToString().replace(" ", ""));
                    }
                }
            }
            At at = null;
            Member to = e.getSender();
            for (Message m : e.getMessage()) {
                if (m instanceof At) { // 遍历消息取出At的对象
                    at = (At) m;
                    to = e.getGroup().get(at.getTarget());
                    continue;
                }
                if (m instanceof PlainText && at != null && !m.contentToString().endsWith(" ")) {
                    pluginPetService.sendImage(e.getGroup(), e.getSender(), to, m.contentToString().replace(" ", ""));
                    return;
                }
            }
            pluginPetService.sendImage(e.getGroup(), e.getSender(), to);
        }
    }

    private void respondImage(Group g, Member m, String imgURL, String key) {
        pluginPetService.sendImage(g, m, m.getAvatarUrl(), imgURL,
                key, new String[]{
                        m.getNameCard().isEmpty() ? m.getNick() : m.getNameCard(),
                        "你",
                        g.getName()
                });
    }

    private void respondImage(GroupMessageEvent e) {

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