package moe.dituon.petpet.bot.qq.mirai;

import moe.dituon.petpet.bot.qq.mirai.handler.MiraiGroupMessageHandler;
import moe.dituon.petpet.bot.qq.mirai.handler.MiraiGroupNudgeHandler;
import moe.dituon.petpet.bot.qq.mirai.handler.MiraiMessageHandler;
import moe.dituon.petpet.bot.qq.mirai.handler.MiraiSentMessageHandler;
import moe.dituon.petpet.service.BaseService;
import moe.dituon.petpet.service.EnvironmentChecker;
import moe.dituon.petpet.service.TemplateUpdater;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.utils.MiraiLogger;

import java.awt.*;
import java.io.File;

@SuppressWarnings("unused")
public final class MiraiPetpet extends JavaPlugin {
    public static final MiraiPetpet INSTANCE = new MiraiPetpet();
    private MiraiBotService service;
    private final MiraiLogger log = getLogger();

    private MiraiPetpet() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.petpet", BaseService.VERSION)
                .name("PetPet")
                .author("Dituon")
                .build());
    }

    @Override
    public void onEnable() {
        System.setProperty("sun.java2d.opengl", "true");
        reloadPluginConfig(MiraiPluginConfig.INSTANCE);

        var config = MiraiPluginConfig.INSTANCE;
        service = new MiraiBotService(config);
        service.setCustomMetadataPath(getConfigFolderPath()
                .toAbsolutePath()
                .resolve(MiraiBotService.DEFAULT_CUSTOM_METADATA_PATH.getName())
                .toFile()
        );
        service.setPermissionConfigPath(getConfigFolderPath().resolve("permissions"));
        var defaultFont = loadService();
        if (config.getUpdate().getEnabled()) new Thread(() -> {
            boolean success = new TemplateUpdater(config.getUpdate(), service).startUpdate();
            if (success) {
                log.info("Petpet 正在重载数据...");
                service.clear();
                var newDefaultFont = loadService();
                log.info(String.format("已加载 %s 模板; 随机表列包含 %s 模板;", service.getStaticModelMap().size(), service.getRandomIdList().size()));
                log.info(String.format("已注册 %s 脚本; 默认模板为 %s;", service.getScriptModelMap().size(), service.getDefaultTemplateId()));
                log.info(String.format("已加载 %s 字体; 默认字体为 %s;", GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames().length, newDefaultFont));

            }
        }).start();

        EnvironmentChecker.check();
        log.info("\u001B[95m\n\n" +
                "    ██████╗ ███████╗████████╗██████╗ ███████╗████████╗\n" +
                "    ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔════╝╚══██╔══╝\n" +
                "    ██████╔╝█████╗     ██║   ██████╔╝█████╗     ██║   \n" +
                "    ██╔═══╝ ██╔══╝     ██║   ██╔═══╝ ██╔══╝     ██║   \n" +
                "    ██║     ███████╗   ██║   ██║     ███████╗   ██║   \n" +
                "    ╚═╝     ╚══════╝   ╚═╝   ╚═╝     ╚══════╝   ╚═╝     " +
                "v" + BaseService.VERSION + "\n");
        log.info("Petpet-Mirai 启动成功:");
        log.info(String.format("已加载 %s 模板; 随机表列包含 %s 模板;", service.getStaticModelMap().size(), service.getRandomIdList().size()));
        log.info(String.format("已注册 %s 脚本; 默认模板为 %s;", service.getScriptModelMap().size(), service.getDefaultTemplateId()));
        log.info(String.format("已加载 %s 字体; 默认字体为 %s;", GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames().length, defaultFont));

        var groupMessageHandler = new MiraiGroupMessageHandler(service);
        var messageHandler = new MiraiMessageHandler(service);

        if (config.getRespondGroup()) {
            GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, groupMessageHandler::handle);
        }
        if (config.getRespondFriend()) {
            GlobalEventChannel.INSTANCE.subscribeAlways(UserMessageEvent.class, messageHandler::handle);
        }

        // 缓存 Bot 自己发送的图像
        // 用户发送的图像缓存逻辑在 MiraiMessageChainWrapper 中处理以提升性能
        if (config.getCachePoolSize() > 0) {
            var sentMessageHandler = new MiraiSentMessageHandler(service);

            if (config.getRespondGroup()) {
                GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessagePostSendEvent.class, sentMessageHandler::handle);
                GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageSyncEvent.class, sentMessageHandler::handle);
            }
            if (config.getRespondFriend()) {
                GlobalEventChannel.INSTANCE.subscribeAlways(UserMessagePostSendEvent.class, sentMessageHandler::handle);
                GlobalEventChannel.INSTANCE.subscribeAlways(FriendMessageSyncEvent.class, sentMessageHandler::handle);
                GlobalEventChannel.INSTANCE.subscribeAlways(GroupTempMessageSyncEvent.class, sentMessageHandler::handle);
            }
        }

        if (config.getProbability() > 0) {
            var nudgeHandler = new MiraiGroupNudgeHandler(service);
            GlobalEventChannel.INSTANCE.subscribeAlways(NudgeEvent.class, nudgeHandler::handle);
        }
    }

    /**
     * @return 默认字体 name
     */
    private String loadService() {
        service.addTemplates(getDataFolder());
        service.addFonts(getDataFolder().toPath().resolve("./fonts"));
        service.updateScriptService();
        return service.updateDefaultFont();
    }
}