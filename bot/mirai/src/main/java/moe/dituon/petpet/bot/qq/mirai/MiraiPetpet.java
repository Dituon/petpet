package moe.dituon.petpet.bot.qq.mirai;

import moe.dituon.petpet.service.BaseService;
import moe.dituon.petpet.service.EnvironmentChecker;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

import java.awt.*;

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
        service.addTemplates(getDataFolder());
        service.addFonts(getDataFolder().toPath().resolve("./fonts"));
        service.setPermissionConfigPath(getConfigFolderPath().resolve("permissions"));
        var defaultFont = service.updateDefaultFont();
        service.updateScriptService();
        //TODO
//        if (config.getAutoUpdate()) new Thread(() -> {
//            DataUpdater updater = new DataUpdater(service, getDataFolder());
//            if (updater.autoUpdate()) {
//                getLogger().info("Petpet 模板更新完毕, 正在重载");
//                config.keyListString = "";
//                config.readData(MiraiPetpet.dataFolder);
//            }
//        }).start();

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

        var handler = new GroupMessageEventHandler(service);
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, handler::handle);
    }
}