package xmmt.dituon;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;

import java.util.ArrayList;

import static xmmt.dituon.PetData.*;

public final class Petpet extends JavaPlugin {
    public static final Petpet INSTANCE = new Petpet();

    Bot bot = null;
    ArrayList<Group> disabledGroup = new ArrayList<>();

    private Petpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", "2.0")
                .name("PetPet")
                .author("Dituon")
                .build());
    }

    @Override
    public void onEnable() {
        readConfig();
        readData();
        GlobalEventChannel.INSTANCE.subscribeOnce(BotOnlineEvent.class, e -> {
            if (bot == null) {
                bot = e.getBot();
                bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, this::onGroupMessage);
                bot.getEventChannel().subscribeAlways(NudgeEvent.class, this::onNudge);
            }
        });
    }

    private void onNudge(NudgeEvent e) {
        if (isDisabled((Group) e.getSubject())) {
            return; // 如果禁用了petpet就返回
        }
        try {
            sendImage((Group) e.getSubject(), (Member) e.getFrom(), (Member) e.getTarget(), true);
        } catch (Exception ex) { // 如果无法把被戳的对象转换为Member(只有Bot无法强制转换为Member对象)
            sendImage((Group) e.getSubject(), (Member) e.getFrom(), ((Group) e.getSubject()).getBotAsMember(), true);
        }
    }

    private void onGroupMessage(GroupMessageEvent e) {
        if (e.getMessage().contentToString().equals((command + " off")) && !isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.add(e.getGroup());
            sendReplyMessage(e, "已禁用 " + command);
            return;
        }

        if (e.getMessage().contentToString().equals((command + " on")) && isDisabled(e.getGroup()) && isPermission(e)) {
            disabledGroup.remove(e.getGroup());
            sendReplyMessage(e, "已启用 " + command);
            return;
        }

        if (!isDisabled(e.getGroup()) && e.getMessage().contains(At.Key)
                && e.getMessage().contentToString().startsWith(command)) {
            At at = null;
            Member to = e.getSender();
            for (Message m : e.getMessage()) {
                if (m instanceof At) { // 遍历消息取出At的对象
                    at = (At) m;
                    to = e.getGroup().get(at.getTarget());
                }
                if (m instanceof PlainText && at != null) {
                    sendImage(e.getGroup(), e.getSender(), to, m.contentToString().replace(" ", ""));
                    return;
                }
            }
            sendImage(e.getGroup(), e.getSender(), to);
        }
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