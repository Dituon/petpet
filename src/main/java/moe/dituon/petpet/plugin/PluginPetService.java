package moe.dituon.petpet.plugin;

import moe.dituon.petpet.share.BasePetService;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PluginPetService extends BasePetService {
    public String command = "pet";
    public String commandHead = "";
    public boolean respondReply = true;
    public int cachePoolSize = 10000;
    public ReplyFormat replyFormat = ReplyFormat.MESSAGE;
    public boolean fuzzy = false;
    public boolean strictCommand = true;
    public boolean messageSynchronized = false;
    public boolean headless = true;
    public ArrayList<String> disabledKey = new ArrayList<>();
    public ArrayList<String> randomableList = new ArrayList<>();

    public List<Long> disabledGroups;

    public void readPluginServiceConfig(PluginServiceConfig config) {
        command = config.getCommand();
        antialias = config.getAntialias();
        commandHead = config.getCommandHead();
        respondReply = config.getRespondReply();
        cachePoolSize = config.getCachePoolSize();
        replyFormat = config.getKeyListFormat();
        fuzzy = config.getFuzzy();
        strictCommand = config.getStrictCommand();
        messageSynchronized = config.getSynchronized();

        readBaseServiceConfig(config.toBaseServiceConfig());

        if (super.quality < 1 || super.quality >= 49) {
            LOGGER.warning(
                    MessageFormat.format("Petpet Plugin 的GIF质量参数范围为 1-49 (1为最佳), 提供的质量参数为{0}, 已自动更改为默认值5", quality)
            );
            super.quality = 5;
        }

        LOGGER.info("Petpet GifMakerThreadPoolSize: " + super.getGifEncoderThreadPoolSize());

        for (String path : config.getDisabled()) {
            disabledKey.add(path.replace("\"", ""));
        }

        LOGGER.info("ヾ(≧▽≦*)o Petpet 初始化成功，使用 " + command + " 以获取keyList!");
    }

    public void readData(File dir) {
        // 1. 所有key加载到dataMap
        super.readData(Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                .filter(File::isDirectory)
                .filter(file -> !disabledKey.contains(file.getName()))
                .toArray(File[]::new));

        // 2. 其中某些key加入randomableList
        dataMap.forEach((path, keyData) -> {
            if (Boolean.TRUE.equals(super.dataMap.get(path).getInRandomList())) {
                randomableList.add(path);
            }
        });

        LOGGER.info("Petpet 加载完毕 (共 " + dataMap.size() + " 素材，随机表列包含 " +
                randomableList.size() + " 素材，已禁用 " + disabledKey.size() + ")");
    }

    public String getKeyAliasListString() {
        return keyListString;
    }
}