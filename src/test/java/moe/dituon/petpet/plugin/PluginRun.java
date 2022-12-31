package moe.dituon.petpet.plugin;

import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal;
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader;
import org.junit.Test;
import moe.dituon.petpet.mirai.MiraiPetpet;

public class PluginRun {
    public static void main(String[] args) {
        // 保持console运行
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(new MiraiConsoleImplementationTerminal());

        PluginManager.INSTANCE.loadPlugin(MiraiPetpet.INSTANCE);

        PluginManager.INSTANCE.enablePlugin(MiraiPetpet.INSTANCE);
    }

    @Test
    public void test() {
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(new MiraiConsoleImplementationTerminal());
        PluginManager.INSTANCE.loadPlugin(MiraiPetpet.INSTANCE);
        PluginManager.INSTANCE.enablePlugin(MiraiPetpet.INSTANCE);
    }

}
