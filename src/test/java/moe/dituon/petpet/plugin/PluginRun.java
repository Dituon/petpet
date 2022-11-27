package moe.dituon.petpet.plugin;

import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal;
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader;
import org.junit.Test;
import moe.dituon.petpet.plugin.Petpet;

public class PluginRun {
    public static void main(String[] args) {
        // 保持console运行
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(new MiraiConsoleImplementationTerminal());

        PluginManager.INSTANCE.loadPlugin(Petpet.INSTANCE);

        PluginManager.INSTANCE.enablePlugin(Petpet.INSTANCE);
    }

    @Test
    public void test() {
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(new MiraiConsoleImplementationTerminal());
        PluginManager.INSTANCE.loadPlugin(Petpet.INSTANCE);
        PluginManager.INSTANCE.enablePlugin(Petpet.INSTANCE);
    }

}
