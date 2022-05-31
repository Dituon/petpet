import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal;
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader;
import xmmt.dituon.Petpet;

public class PluginRun {
    public static void main(String[] args) {
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(new MiraiConsoleImplementationTerminal());

        PluginManager.INSTANCE.loadPlugin(Petpet.INSTANCE);

        PluginManager.INSTANCE.enablePlugin(Petpet.INSTANCE);
    }
}
