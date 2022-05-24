package xmmt.dituon;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Random;

import static xmmt.dituon.ImageSynthesis.sendImage;

public final class Petpet extends JavaPlugin {
    public static final Petpet INSTANCE = new Petpet();
    public static boolean antialias = false;
    public static String command = "pet";
    public static int randomMax = 40;
    Bot bot = null;

    private Petpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", "1.1")
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
        if (e.getFrom().getId() == e.getTarget().getId()) {
            makeImage((Group) e.getSubject(), (Member) e.getTarget(), new Random().nextInt(randomMax));
            return;
        }
        makeImage((Group) e.getSubject(), (Member) e.getFrom(), (Member) e.getTarget(), new Random().nextInt(randomMax));
    }

    private void onGroupMessage(GroupMessageEvent e) {
        if (e.getMessage().contains(At.Key) && e.getMessage().contentToString().startsWith(command)) {
            for (Message m : e.getMessage()) {
                if (m instanceof At) {
                    At at = (At) m;
                    Member to = e.getGroup().get(at.getTarget());
                    assert to != null;
                    if (to.getId() == e.getSender().getId()) {
                        makeImage(e.getGroup(), to, new Random().nextInt(14));
                    }
                    makeImage(e.getGroup(), e.getSender(), to, new Random().nextInt(14));
                }
            }
        }
    }

    void makeImage(Group group, Member member, int index) {
        makeImage(group, group.getBotAsMember(), member, index);
    }

    void makeImage(Group group, Member from, Member to, int index) {
        int[][] fromPos;
        int[][] toPos;
        int[][] pos;

        switch (index) {
            case 0:
                fromPos = new int[][]{
                        {92, 64, 40, 40}, {135, 40, 40, 40}, {84, 105, 40, 40}, {80, 110, 40, 40},
                        {155, 82, 40, 40}, {60, 96, 40, 40}, {50, 80, 40, 40}, {98, 55, 40, 40},
                        {35, 65, 40, 40}, {38, 100, 40, 40}, {70, 80, 40, 40}, {84, 65, 40, 40},
                        {75, 65, 40, 40}
                };
                toPos = new int[][]{
                        {58, 90, 50, 50}, {62, 95, 50, 50}, {42, 100, 50, 50}, {50, 100, 50, 50},
                        {56, 100, 50, 50}, {18, 120, 50, 50}, {28, 110, 50, 50}, {54, 100, 50, 50},
                        {46, 100, 50, 50}, {60, 100, 50, 50}, {35, 115, 50, 50}, {20, 120, 50, 50},
                        {40, 96, 50, 50}
                };
                group.sendMessage(Objects.requireNonNull(
                        sendImage(from, to, "./res/petpet/kiss/", fromPos, toPos)));
                break;
            case 1:
                fromPos = new int[][]{
                        {102, 95, 70, 80}, {108, 60, 50, 100}, {97, 18, 65, 95},
                        {65, 5, 75, 75}, {95, 57, 100, 55}, {109, 107, 65, 75}
                };
                toPos = new int[][]{
                        {39, 91, 75, 75}, {49, 101, 75, 75}, {67, 98, 75, 75},
                        {55, 86, 75, 75}, {61, 109, 75, 75}, {65, 101, 75, 75}
                };
                group.sendMessage(Objects.requireNonNull(
                        sendImage(from, to, "./res/petpet/rub/", fromPos, toPos)));
                break;
            case 2:
                fromPos = new int[][]{
                        {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0},
                        {289, 70, 33, 33}, {280, 73, 32, 32}, {259, 31, 35, 35}, {-50, 220, 175, 175}
                };
                toPos = new int[][]{
                        {108, 36, 32, 32}, {122, 36, 32, 32}, {0, 0, 0, 0}, {19, 129, 123, 123},
                        {-50, 200, 185, 185}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}
                };
                group.sendMessage(Objects.requireNonNull(
                        sendImage(from, to, "./res/petpet/throw/", fromPos, toPos)));
                break;
            case 3:
                pos = new int[][]{
                        {14, 20, 98, 98}, {12, 33, 101, 85}, {8, 40, 110, 76}, {10, 33, 102, 84}, {12, 20, 98, 98}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/petpet/", pos, false, false)));
                break;
            case 4:
                pos = new int[][]{
                        {180, 60, 100, 100}, {184, 75, 100, 100}, {183, 98, 100, 100},
                        {179, 118, 110, 100}, {156, 194, 150, 48}, {178, 136, 122, 69},
                        {175, 66, 122, 85}, {170, 42, 130, 96}, {175, 34, 118, 95},
                        {179, 35, 110, 93}, {180, 54, 102, 93}, {183, 58, 97, 92},
                        {174, 35, 120, 94}, {179, 35, 109, 93}, {181, 54, 101, 92},
                        {182, 59, 98, 92}, {183, 71, 90, 96}, {180, 131, 92, 101},
                        {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/play/", pos, true, false)));
                break;
            case 5:
                pos = new int[][]{
                        {87, 77, 220, 220}, {96, 85, 220, 220}, {92, 79, 220, 220}, {92, 78, 220, 220},
                        {92, 75, 220, 220}, {92, 75, 220, 220}, {93, 76, 220, 220}, {90, 80, 220, 220}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/roll/", pos, false, true)));
                break;
            case 6:
                pos = new int[][]{
                        {90, 90, 105, 150}, {90, 83, 96, 172}, {90, 90, 106, 148},
                        {88, 88, 97, 167}, {90, 85, 89, 179}, {90, 90, 106, 151},
                        {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0},
                        {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/bite/", pos, false, false)));
                break;
            case 7:
                pos = new int[][]{
                        {25, 66, 80, 80}, {25, 66, 80, 80}, {23, 68, 80, 80},
                        {20, 69, 80, 80}, {22, 68, 80, 80}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/twist/", pos, false, true)));
                break;
            case 8:
                pos = new int[][]{
                        {135, 240, 138, 47}, {135, 240, 138, 47}, {150, 190, 105, 95}, {150, 190, 105, 95},
                        {148, 188, 106, 98}, {146, 196, 110, 88}, {145, 223, 112, 61}, {145, 223, 112, 61}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/pound/", pos, false, false)));
                break;
            case 9:
                pos = new int[][]{
                        {65, 128, 77, 72}, {67, 128, 73, 72}, {54, 139, 94, 61}, {57, 135, 86, 65}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/thump/", pos, false, false)));
                break;
            case 10:
                pos = new int[][]{
                        {60, 308, 210, 195}, {60, 308, 210, 198}, {45, 330, 250, 172}, {58, 320, 218, 180},
                        {60, 310, 215, 193}, {40, 320, 250, 285}, {48, 308, 226, 192}, {51, 301, 223, 200}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/knock/", pos, false, false)));
                break;
            case 11:
                pos = new int[][]{
                        {82, 100, 130, 119}, {82, 94, 126, 125}, {82, 120, 128, 99}, {81, 164, 132, 55},
                        {79, 163, 132, 55}, {82, 140, 127, 79}, {83, 152, 125, 67}, {75, 157, 140, 62},
                        {72, 165, 144, 54}, {80, 132, 128, 87}, {81, 127, 127, 92}, {79, 111, 132, 108}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/suck/", pos, false, false)));
                break;
            case 12:
                pos = new int[][]{
                        {62, 143, 158, 113}, {52, 177, 173, 105}, {42, 192, 192, 92}, {46, 182, 184, 100},
                        {54, 169, 174, 110}, {69, 128, 144, 135}, {65, 130, 152, 124}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/hammer/", pos, false, false)));
                break;
            case 13:
                pos = new int[][]{
                        {39, 169, 267, 141}, {40, 167, 264, 143}, {38, 174, 270, 135}, {40, 167, 264, 143}, {38, 174, 270, 135},
                        {40, 167, 264, 143}, {38, 174, 270, 135}, {40, 167, 264, 143}, {38, 174, 270, 135}, {28, 176, 293, 134},
                        {5, 215, 333, 96}, {10, 210, 321, 102}, {3, 210, 330, 104}, {4, 210, 328, 102}, {4, 212, 328, 100},
                        {4, 212, 328, 100}, {4, 212, 328, 100}, {4, 212, 328, 100}, {4, 212, 328, 100}, {29, 195, 285, 120}
                };
                group.sendMessage(Objects.requireNonNull(
                        ImageSynthesis.sendImage(to, "./res/petpet/tightly/", pos, false, false)));
                break;
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
}