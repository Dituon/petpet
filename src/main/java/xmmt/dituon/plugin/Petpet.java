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
import xmmt.dituon.plugin.parser.PetPetParserAutoSaveConfig;
import xmmt.dituon.plugin.parser.PetpetDrawStatement;
import xmmt.dituon.plugin.parser.PetpetParser;
import xmmt.dituon.plugin.parser.PetpetParserConfig;
import xmmt.dituon.plugin.parser.PetpetSpecialStatement;
import xmmt.dituon.plugin.parser.hundun.statement.Statement;
import xmmt.dituon.share.AvatarExtraData;
import xmmt.dituon.share.BaseConfigFactory;
import xmmt.dituon.share.TextExtraData;

import java.util.*;
import java.util.stream.Collectors;

public final class Petpet extends JavaPlugin {
    public static final Petpet INSTANCE = new Petpet();
    public static final float VERSION = 3.3F;

    ArrayList<Group> disabledGroup = new ArrayList<>();
    PluginPetService pluginPetService;
    private PetpetParser petpetParser;

    private Petpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", String.valueOf(VERSION))
                .name("PetPet")
                .author("Dituon")
                .build());
        this.pluginPetService = new PluginPetService();
    }

    @Override
    public void onEnable() {
        try {
            this.reloadPluginConfig(PetPetAutoSaveConfig.INSTANCE);
            this.reloadPluginConfig(PetPetParserAutoSaveConfig.INSTANCE);
        } catch (Exception ex) {
            ex.printStackTrace();
            getLogger().info("Mirai 2.11.0 提供了新的 JavaAutoSaveConfig 方法, 请更新Mirai版本至 2.11.0 (不是2.11.0-M1)\n若不想更新可使用本插件 2.0 版本");
        }

        pluginPetService.readConfigByPluginAutoSave();
        pluginPetService.readData(getDataFolder());
        // 3. 初始化parser
        PetpetParserConfig config = PetPetParserAutoSaveConfig.INSTANCE.content.get();

        Map<String, List<String>> namesMap = pluginPetService.getDataMap().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> {
                            List<String> allNames = new ArrayList<>();
                            allNames.add(entry.getKey());
                            if (entry.getValue().getAlias() != null) {
                                allNames.addAll(entry.getValue().getAlias());
                            }
                            return  allNames;
                        })
                );
        petpetParser = new PetpetParser(config, namesMap);
        System.out.println("Petpet 初始化成功，使用 " + petpetParser.getConfig().getMainCommand() + " 以生成GIF。");

        if (pluginPetService.headless) {
            System.setProperty("java.awt.headless", "true");
        }

        getLogger().info("\n____ ____ ____ ____ ____ ____ \n| . \\| __\\|_ _\\| . \\| __\\|_ _\\\n" +
                "| __/|  ]_  || | __/|  ]_  || \n|/   |___/  |/ |/   |___/  |/ ");

        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, this::onGroupMessageEvent);
        GlobalEventChannel.INSTANCE.subscribeAlways(NudgeEvent.class, this::onNudge);
    }

    private void onNudge(NudgeEvent e) {
        if (isDisabled((Group) e.getSubject())) {
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

    public void onGroupMessageEvent(GroupMessageEvent event) {
        MessageChain messageChain = event.getMessage();
        Group group = event.getGroup();
        Statement statement = null;
        try {
            statement = petpetParser.simpleParse(messageChain);
        } catch (Exception e) {
            System.out.println("Parser error during parse: “" + messageChain.contentToString() + "”");
            e.printStackTrace();
        }

        if (statement == null) {
            return;
        }
        String logMessage = statement.getClass().getSimpleName() + " " +  statement.getTokens().stream().map(token -> token.getType().name()).collect(Collectors.joining(","));
        System.out.println("parse结果: " + logMessage);
        if (statement instanceof PetpetSpecialStatement) {

            PetpetSpecialStatement specialStatement = (PetpetSpecialStatement) statement;

            switch (specialStatement.getSubType()) {
                case ON:
                    if (!isPermission(event)) {
                        break;
                    }
                    disabledGroup.remove(group);
                    sendReplyMessage(event, "已启用" + petpetParser.getConfig().getMainCommand());
                    break;
                case OFF:
                    if (!isPermission(event)) {
                        break;
                    }
                    disabledGroup.add(group);
                    sendReplyMessage(event, "已禁用" + petpetParser.getConfig().getMainCommand());
                    break;
                case LIST_KEY:
                    if (isDisabled(group)) {
                        break;
                    }
                    event.getGroup().sendMessage("Petpet KeyList:\n"+pluginPetService.getKeyAliasListString());
                    break;
                case NONE:
                default:
                    // 并不是真的特殊指令， do nothing
                    break;
            }
        } else if (statement instanceof PetpetDrawStatement) {
            if (isDisabled(group)) {
                return;
            }

            PetpetDrawStatement drawStatement = (PetpetDrawStatement) statement;

            /*
                依次准备各sendImage参数。这些参数要么来自petpetStatement，要么来自默认值，均在本方法内准备好。
             */

            Member from = event.getGroup().getBotAsMember();
            Member to;
            if (drawStatement.getAt() != null) {
                to = Objects.requireNonNull(event.getGroup().get(drawStatement.getAt().getTarget()));
            } else {
                to = event.getSender();
            }

            String key;
            if (drawStatement.getTemplateId() != null) {
                key = drawStatement.getTemplateId();
            } else {
                key = pluginPetService.randomableList.get(new Random().nextInt(pluginPetService.randomableList.size()));
            }

            String otherText;
            if (drawStatement.getAdditionTexts().size() > 0) {
                otherText = drawStatement.getAdditionTexts().get(0);
            } else {
                otherText = null;
            }

            TextExtraData textExtraData = new TextExtraData(
                    from.getNameCard().isEmpty() ? from.getNick() : from.getNameCard(),
                    to.getNameCard().isEmpty() ? to.getNick() : to.getNameCard(),
                    group.getName(),
                    otherText == null || otherText.equals("") ? new ArrayList<>() :
                            new ArrayList<>(Arrays.asList(otherText.split("\\s+")))
            );

            String toAvatarUrl;
            if (drawStatement.getImage() != null) {
                toAvatarUrl = Image.queryUrl(drawStatement.getImage());
            } else {
                toAvatarUrl = to.getAvatarUrl();
            }

            AvatarExtraData avatarExtraData = BaseConfigFactory.getAvatarExtraDataFromUrls(
                    from.getAvatarUrl(), toAvatarUrl, group.getAvatarUrl(), group.getBotAsMember().getAvatarUrl()
            );


            pluginPetService.sendImage(event.getGroup(), key, avatarExtraData, textExtraData);
        }
    }

}