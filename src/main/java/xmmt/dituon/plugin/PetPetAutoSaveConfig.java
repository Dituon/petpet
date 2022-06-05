package xmmt.dituon.plugin;

import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;


public class PetPetAutoSaveConfig extends JavaAutoSavePluginConfig {
    public static final PetPetAutoSaveConfig INSTANCE = new PetPetAutoSaveConfig();

    public PetPetAutoSaveConfig() {
        super("PetPet");
    }

    public final Value<PluginConfig> content = typedValue("content", createKType(PluginConfig.class), new PluginConfig());

}
