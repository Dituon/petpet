package xmmt.dituon.plugin;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.*;
import xmmt.dituon.share.TextExtraData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public final class Petpet extends JavaPlugin {
    public static final Petpet INSTANCE = new Petpet();

    ArrayList<Group> disabledGroup = new ArrayList<>();
    PluginPetService pluginPetService;

    private Petpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", "2.5")
                .name("PetPet")
                .author("Dituon")
                .build());
        this.pluginPetService = new PluginPetService();
    }

    @Override
    public void onEnable() {
        try {
        this.reloadPluginConfig(PetPetAutoSaveConfig.INSTANCE);
        } catch (Exception ex) {
            ex.printStackTrace();
            getLogger().info("Mirai 2.11.0 提供了新的 JavaAutoSaveConfig 方法, 请更新Mirai版本至 2.11.0 (不是2.11.0-M1)\n若不想更新可使用本插件 2.0 版本");
        }

        pluginPetService.readConfigByPluginAutoSave();
        pluginPetService.readData(getDataFolder());
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, this::onGroupMessage);
        GlobalEventChannel.INSTANCE.subscribeAlways(NudgeEvent.class, this::onNudge);
    }

    private void onNudge(NudgeEvent e) {
        if (isDisabled((Group) e.getSubject()) || e.getFrom() instanceof Bot) {
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
            String otherText = null;
            for (Message m : e.getMessage()) {
                if (m instanceof PlainText && key == null) {
                    String firstWord = getFirstWord(m.contentToString());
                    if (pluginPetService.dataMap.containsKey(firstWord)) {
                        key = firstWord;
                        otherText = m.contentToString().replace(key, "").trim();
                        continue;
                    }
                    break;
                }
                if (pluginPetService.respondImage && m instanceof Image && key != null) {
                    respondImage(e.getGroup(), e.getSender(), Image.queryUrl((Image) m), key, otherText);
                    return;
                }
                if (m instanceof At && key != null) {
                    At at = (At) m;
                    pluginPetService.sendImage(e.getGroup(), e.getSender(),
                            Objects.requireNonNull(e.getGroup().get(at.getTarget())), key, otherText);
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
                    if (m instanceof PlainText && toURL != null) {
                        String firstWord = getFirstWord(m.contentToString());
                        respondImage(e.getGroup(), e.getSender(), toURL, firstWord,
                                m.contentToString().replace(firstWord, "").trim());
                        return;
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
                if (m instanceof PlainText && at != null) {
                    String firstWord = getFirstWord(m.contentToString());
                    pluginPetService.sendImage(e.getGroup(), e.getSender(), to, firstWord,
                            m.contentToString().replace(firstWord, "").trim());
                    return;
                }
            }
            pluginPetService.sendImage(e.getGroup(), e.getSender(), to);
        }
    }

    private String getFirstWord(String text) {
        return text.trim().contains(" ") ?
                text.trim().split("\\s+")[0] : text.trim();
    }

    private void respondImage(Group g, Member m, String imgURL, String key, String otherText) {
        pluginPetService.sendImage(g, m, m.getAvatarUrl(), imgURL,
                key,
                new TextExtraData(
                        m.getNameCard().isEmpty() ? m.getNick() : m.getNameCard(),
                        "你",
                        g.getName(),
                        otherText == null || otherText.equals("") ? new ArrayList<>() :
                                new ArrayList<>(Arrays.asList(otherText.split("\\s+")))
                )
        );
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