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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import static xmmt.dituon.PetData.makeImage;

public final class Petpet extends JavaPlugin {
    public static final Petpet INSTANCE = new Petpet();
    Bot bot = null;
    ArrayList<Group> disabledGroup = new ArrayList<>();

    public static boolean antialias = false;
    public static String command = "pet";
    public static int randomMax = 40;

    private Petpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", "1.4")
                .name("PetPet")
                .author("Dituon")
                .build());
    }

    @Override
    public void onEnable() {
        readConfig();
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
            makeImage((Group) e.getSubject(), (Member) e.getFrom(), (Member) e.getTarget(), new Random().nextInt(randomMax));
        } catch (Exception ex) { // 如果无法把被戳的对象转换为Member(只有Bot无法强制转换为Member对象)
            makeImage((Group) e.getSubject(), (Member) e.getFrom(), ((Group) e.getSubject()).getBotAsMember(), new Random().nextInt(randomMax));
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
            int index = new Random().nextInt(14);
            for (Message m : e.getMessage()) {
                if (m instanceof At) { // 遍历消息取出At的对象
                    at = (At) m;
                    to = e.getGroup().get(at.getTarget());
                }
                if (m instanceof PlainText && at != null) {
                    try {
                        index = Integer.parseInt(m.contentToString().replace(" ", ""));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            makeImage(e.getGroup(), e.getSender(), to, index);
        }
    }

    public void readConfig() {
        File configFile = new File("./plugins/petpet.json");
        try {
            if (configFile.exists()) {
                BufferedReader configBr = new BufferedReader(new FileReader(configFile));
                StringBuilder configSb = new StringBuilder();
                String str;
                while ((str = configBr.readLine()) != null) {
                    configSb.append(str);
                }
                configBr.close();
                ConfigJSON config = ConfigJSONKt.decode(configSb.toString());
                command = config.getCommand();
                antialias = config.getAntialias();
                randomMax = (int) (14 / (config.getProbability() * 0.01));
                getLogger().info("Petpet 初始化成功，使用 " + command + " 以生成GIF。");
            } else {
                String defaultConfig = "{\n  \"command\": \"pet\",\n  \"probability\": 30,\n  \"antialias\": false\n}";
                if (!configFile.createNewFile()) {
                    getLogger().error("无法创建配置文件，请检查权限!");
                    return;
                }
                FileOutputStream defaultConfigOS = new FileOutputStream(configFile);
                defaultConfigOS.write(defaultConfig.getBytes(StandardCharsets.UTF_8));
                getLogger().info("创建配置文件成功，请去 Mirai/plugins/ 目录编辑 petpet.json");
                readConfig();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
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