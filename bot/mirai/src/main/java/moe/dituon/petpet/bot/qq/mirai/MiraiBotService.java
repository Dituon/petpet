package moe.dituon.petpet.bot.qq.mirai;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.bot.TemplateExtraMetadata;
import moe.dituon.petpet.bot.qq.QQBotService;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class MiraiBotService extends QQBotService {
    public static final File DEFAULT_CUSTOM_METADATA_PATH = new File("TemplateMetadata.yml");

    @Getter
    protected final MiraiPluginConfig miraiConfig;
    @Getter
    @Setter
    protected File customMetadataPath = DEFAULT_CUSTOM_METADATA_PATH;

    public MiraiBotService(MiraiPluginConfig config) {
        super(config.toQQBotConfig());
        this.miraiConfig = config;
    }

    public Image uploadImage(byte @NotNull [] bytes, Contact contact) throws IOException {
        ExternalResource resource = ExternalResource.create(bytes);
        if (contact == null) contact = Bot.getInstances().get(0).getAsFriend();
        Image image = contact.uploadImage(resource);
        resource.close();
        return image;
    }

    /**
     * 尝试缓存图像, 如果无法获取消息 id (低概率事件) 则返回 false
     */
    public boolean cacheImage(@Nullable MessageSource source, String imageUrl) {
        long id = getUniqueSourceId(source);
        if (id == 0L) {
            return false;
        }
        this.getImageCachePool().put(id, imageUrl);
        return true;
    }

    public @Nullable String getCachedImage(MessageSource source) {
        long id = getUniqueSourceId(source);
        return this.getImageCachePool().get(id);
    }

    public @Nullable String getCachedImage(MessageSource source, Long targetId) {
        long id = getUniqueSourceId(source, targetId);
        return this.getImageCachePool().get(id);
    }

    /**
     * 计算消息唯一 id, 如果无法获取消息 id (低概率事件) 则返回 0
     */
    public long getUniqueSourceId(@Nullable MessageSource source) {
        if (source == null) {
            return 0L;
        }
        int[] sourceIds = source.getIds();
        if (sourceIds.length == 0) {
            return 0L;
        }
        // message id = target id + source id
        return source.getTargetId() + sourceIds[0];
    }

    public long getUniqueSourceId(@Nullable MessageSource source, Long targetId) {
        if (source == null) {
            return 0L;
        }
        int[] sourceIds = source.getIds();
        if (sourceIds.length == 0) {
            return 0L;
        }
        // message id = target id + source id
        return targetId + sourceIds[0];
    }

    @Override
    protected Map<String, TemplateExtraMetadata> initCustomTemplateMetadata() {
        Map<String, TemplateExtraMetadata> metadataMap;
        try {
            var str = Files.readString(customMetadataPath.toPath());
            metadataMap = UtilsKt.decodeCustomMetadataConfig(str);
        } catch (Exception ex) {
            if (!(ex instanceof NoSuchFileException)) {
                log.warn("无法读取自定义模板元数据文件: {}", customMetadataPath.getAbsolutePath(), ex);
            }
            return Collections.emptyMap();
        }
        return metadataMap;
    }

    @Override
    public void updateScriptService() {
        var savedMap = buildSavedMetadataMap();
        if (savedMap.isEmpty()) {
            super.updateScriptService();
            return;
        }
        try {
            var str = UtilsKt.encodeCustomMetadataConfig(savedMap);
            Files.writeString(customMetadataPath.toPath(), str);
        } catch (IOException e) {
            log.warn("无法保存自定义模板元数据文件: {}", customMetadataPath.getAbsolutePath(), e);
        }
        super.updateScriptService();
    }
}
