package xmmt.dituon.plugin;

import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;
import xmmt.dituon.share.ConfigDTO;

public class PetPetAutoSaveConfig extends JavaAutoSavePluginConfig {
    public static final PetPetAutoSaveConfig INSTANCE = new PetPetAutoSaveConfig();

    public PetPetAutoSaveConfig() {
        super("PetPetAutoSaveConfig");
    }

    public final Value<ConfigDTO> content = typedValue("content", createKType(ConfigDTO.class), new ConfigDTO()); // 默认值 "test"

}
