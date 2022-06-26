package xmmt.dituon.plugin.parser;

import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;
import xmmt.dituon.plugin.PluginConfig;


public class PetPetParserAutoSaveConfig extends JavaAutoSavePluginConfig {
    public static final PetPetParserAutoSaveConfig INSTANCE = new PetPetParserAutoSaveConfig();

    public PetPetParserAutoSaveConfig() {
        super("PetpetParserConfig");
    }

    public final Value<PetpetParserConfig> content = typedValue("content", createKType(PetpetParserConfig.class), new PetpetParserConfig());

}
