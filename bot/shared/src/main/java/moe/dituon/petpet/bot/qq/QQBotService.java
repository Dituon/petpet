package moe.dituon.petpet.bot.qq;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.bot.BotService;
import moe.dituon.petpet.bot.TemplateExtraMetadata;
import moe.dituon.petpet.bot.qq.permission.ContactPermission;
import moe.dituon.petpet.bot.qq.permission.TimeParser;
import moe.dituon.petpet.core.element.ElementModel;
import moe.dituon.petpet.core.element.PetpetModel;
import moe.dituon.petpet.core.element.PetpetTemplateModel;
import moe.dituon.petpet.core.element.avatar.AvatarModel;
import moe.dituon.petpet.script.PetpetScriptModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 针对于 QQ 群聊的抽象 Bot 服务
 */
@Slf4j
public class QQBotService extends BotService {
    public static final float DEFAULT_NUDGE_PROBABILITY = 0.3f;
    public static final String REPLY_NUDGE_KEYWORD = "[nudge]";

    @Getter
    protected final QQBotConfig config;
    @Getter
    public final Map<String, Integer> commandPermissionNameMap;
    @Getter
    protected final float nudgeProbability;
    @Getter
    protected final Map<Long, String> imageCachePool;
    @Getter
    protected final int defaultGroupCommandPermission;
    @Getter
    protected final int defaultGroupEditPermission;
    @Getter
    protected final Map<Object, ContactPermission> groupPermissionMap = new HashMap<>(256);
    @Setter
    @Getter
    protected Path permissionConfigPath = Path.of("./permissions");
    @Getter
    public final boolean cooldownReplyNudge;
    @Getter
    public final TimeParser timeParser;

    @Setter
    @Getter
    protected boolean savePermission = true;

    public QQBotService(QQBotConfig config) {
        if (config.getHeadless()) {
            System.setProperty("java.awt.headless", "true");
        }
        this.config = config;
        this.nudgeProbability = config.getNudgeProbability();
        this.imageCachePool = new LinkedHashMap<>(config.getImageCachePoolSize(), 0.75f, true) {
            @Override
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > config.getImageCachePoolSize();
            }
        };

        this.commandPermissionNameMap = new HashMap<>(
                ContactPermission.COMMAND_PERMISSION_NAME_MAP.size()
                        + config.getCommandPermissionName().size()
        );
        initPermissionNameMap(config);
        this.defaultGroupCommandPermission = stringToCommandPermission(config.getDefaultGroupCommandPermission());
        this.defaultGroupEditPermission = stringToEditPermission(config.getDefaultGroupEditPermission());
        this.timeParser = new TimeParser(config.getTimeUnitName());
        this.cooldownReplyNudge = REPLY_NUDGE_KEYWORD.equals(config.getInCoolDownMessage());

        Runtime.getRuntime().addShutdownHook(new Thread(this::onJvmExit));
    }

    @Override
    public PetpetModel addTemplate(String id, PetpetModel model) {
        if (id.equals(config.getDefaultTemplate())) {
            super.defaultTemplateLock = true;
            super.defaultTemplate = model;
            this.defaultTemplateId = id;
        }
        return super.addTemplate(id, model);
    }

    public ContactPermission getPermission(Object id) {
        return groupPermissionMap.computeIfAbsent(id, k -> new ContactPermission(this, k));
    }

    @Override
    public @NotNull PetpetModel getDefaultTemplate() {
        if (defaultTemplate == null) {
            defaultTemplate = new TemplateIndexScriptModel(this);
        }
        return defaultTemplate;
    }

    @Override
    protected boolean isModelInRandomList(String id, PetpetModel model) {
        return config.getDisabledTemplates().contains(id) || model.getMetadata().getInRandomList()  ;
    }

    private void initPermissionNameMap(QQBotConfig config) {
        this.commandPermissionNameMap.putAll(ContactPermission.COMMAND_PERMISSION_NAME_MAP);
        for (Map.Entry<String, String> entry : config.getCommandPermissionName().entrySet()) {
            int i = stringToCommandPermission(entry.getValue());
            if (i == 0) continue;
            this.commandPermissionNameMap.put(entry.getKey(), i);
        }
    }

    /**
     * This method parses each permission identifier by string. <br/>
     * The permission identifiers in the permission string are separated by spaces, vertical bars (|), or amp symbols (&)
     */
    public int stringToCommandPermission(String permissions) {
        return stringToPermission(permissions, commandPermissionNameMap);
    }

    /**
     * This method parses each permission identifier by string. <br/>
     * The permission identifiers in the permission string are separated by spaces, vertical bars (|), or amp symbols (&)
     */
    public static int stringToEditPermission(String permissions) {
        return stringToPermission(permissions, ContactPermission.EDIT_PERMISSION_NAME_MAP);
    }

    protected static int stringToPermission(String permissions, Map<String, Integer> nameMap) {
        if (permissions.isBlank()) return -1;
        var tokens = permissions.trim().split("[\\s|&]+");
        int i = 0;
        for (String token : tokens) {
            Integer p = nameMap.get(token);
            if (p == null) {
                log.warn("Unknown permission name: {}", token);
                continue;
            }
            i |= p;
        }
        return i;
    }

    public String updateDefaultFont() {
        return setDefaultFontFamily(config.getDefaultFontFamily());
    }

    protected Map<String, TemplateExtraMetadata> buildSavedMetadataMap() {
        var tempMap = new HashMap<String, TemplateExtraMetadata>(staticModelMap.size());
        staticModelMap.entrySet().stream()
                .filter(e -> !getExtraMetadataMap().containsKey(e.getKey()) && e.getValue().getMetadata() != null)
                .forEach(e -> tempMap.put(e.getKey(), TemplateExtraMetadata.fromMetadata(e.getValue().getMetadata())));
        if (tempMap.isEmpty()) {
            return Collections.emptyMap();
        }
        tempMap.putAll(getExtraMetadataMap());
        return tempMap;
    }

    @Override
    public void updateScriptService() {
        super.updateScriptService();
        if (defaultTemplate == null) {
            log.warn("无法找到默认模板, 将使用默认 forward_text 回复方案");
        }
    }

    // save permission config
    protected void onJvmExit() {
        if (!savePermission) return;
        int i = 0;
        for (ContactPermission permission : groupPermissionMap.values()) {
            permission.saveConfig();
            i++;
        }
        if (i != 0) {
            log.info("Saved {} permission config", i);
        }
    }
}
