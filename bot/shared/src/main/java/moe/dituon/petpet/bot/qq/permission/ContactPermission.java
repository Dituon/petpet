package moe.dituon.petpet.bot.qq.permission;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.bot.qq.QQBotService;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ContactPermission {
    /**
     * 通过指令生成模板 (pet id)
     */
    public static final int COMMAND = 0b00001;
    /**
     * 通过戳一戳生成模板
     */
    public static final int NUDGE = 0b00010;
    /**
     * 通过 At 用户生成模板
     */
    public static final int AT = 0b00100;
    /**
     * 通过 发送/回复 图像生成模板
     */
    public static final int IMAGE = 0b01000;
    /**
     * 通过 head + id 生成模板 (#id)
     */
    public static final int COMMAND_HEAD = 0b10000;
    public static final int COMMAND_ALL = COMMAND | NUDGE | AT | IMAGE | COMMAND_HEAD;

    public static final Map<String, Integer> COMMAND_PERMISSION_NAME_MAP = Map.of(
            "all", COMMAND_ALL,
            "command", COMMAND,
            "nudge", NUDGE,
            "at", AT,
            "image", IMAGE,
            "command_head", COMMAND_HEAD
    );

//    public static final Map<String>

    public static final int EDIT_COMMAND_PERMISSION = 0b0001;
    public static final int EDIT_DISABLE_TEMPLATE_LIST = 0b0010;
    public static final int EDIT_NUDGE_PROBABILITY = 0b0100;
    public static final int EDIT_COOLDOWN_TIME = 0b1000;
    public static final int EDIT_ALL = EDIT_COMMAND_PERMISSION | EDIT_DISABLE_TEMPLATE_LIST | EDIT_NUDGE_PROBABILITY | EDIT_COOLDOWN_TIME;

    public static final Map<String, Integer> EDIT_PERMISSION_NAME_MAP = Map.of(
            "all", EDIT_ALL,
            "command_permission", EDIT_COMMAND_PERMISSION,
            "disable_template", EDIT_DISABLE_TEMPLATE_LIST,
            "nudge_probability", EDIT_NUDGE_PROBABILITY,
            "cooldown_time", EDIT_COOLDOWN_TIME
    );

    public static final String ENABLE_MESSAGE = "已启用 %s";
    public static final String DISABLE_MESSAGE = "已禁用 %s";
    public static final String ENABLE_TEMPLATE_MESSAGE = "已启用模板 %s";
    public static final String DISABLE_TEMPLATE_MESSAGE = "已禁用模板 %s";
    public static final String PROBABILITY_MESSAGE = "戳一戳触发概率更新为 %.2f%%";
    public static final String COOLDOWN_MESSAGE = "冷却时间更新为 %s";

    public final Object id;
    public final QQBotService service;
    @Setter
    protected int commandPermission = -1;
    @Setter
    protected int editPermission = -1;
    /**
     * 戳一戳生成概率
     */
    protected float nudgeProbability = -1f;
    protected Set<String> disabledTemplateIds = null;
    /**
     * 冷却时间 (ms)
     */
    protected long cooldownTime = -1L;

    protected final Path configPath;

    public ContactPermission(QQBotService service, Object id) {
        this.id = id;
        this.service = service;
        this.configPath = getConfigFile(id);
        init();
    }

    protected void init() {
        if (!Files.exists(configPath)) {
            return;
        }
        try {
            var config = GroupPermissionConfig.fromJsonString(Files.readString(configPath));
            if (config.getCommandPermission() != null) {
                this.commandPermission = this.service.stringToCommandPermission(config.getCommandPermission());
            }

            if (config.getEditPermission() != null) {
                this.editPermission = this.service.stringToCommandPermission(config.getEditPermission());
            }

            if (config.getNudgeProbability() != null) {
                this.nudgeProbability = config.getNudgeProbability();
            }

            if (config.getDisabledTemplates() != null) {
                this.disabledTemplateIds = config.getDisabledTemplates();
            }

            if (config.getCooldownTime() != null) {
                this.cooldownTime = config.getCooldownTime();
            }
        } catch (IOException e) {
            log.error("Can not load permission config: {}", configPath.toAbsolutePath());
        }
    }

    public int getCommandPermission() {
        if (commandPermission == -1) return service.getDefaultGroupCommandPermission();
        return commandPermission;
    }

    public int getEditPermission() {
        if (editPermission == -1) return service.getDefaultGroupEditPermission();
        return editPermission;
    }

    public long getCooldownTime() {
        if (cooldownTime == -1) return service.getConfig().getGroupCooldownTime();
        return cooldownTime;
    }

    public float getNudgeProbability() {
        if (nudgeProbability == -1) return service.getNudgeProbability();
        return nudgeProbability;
    }

    public Set<String> getDisabledTemplateIds() {
        if (disabledTemplateIds == null) {
            var defaultDisabledTemplates = service.getConfig().getDisabledTemplates();
            if ((getEditPermission() & EDIT_DISABLE_TEMPLATE_LIST) == 0) {
                return defaultDisabledTemplates;
            }
            disabledTemplateIds = new HashSet<>(defaultDisabledTemplates);
        }
        return disabledTemplateIds;
    }

    /**
     * @return null if command permission not set
     */
    public @Nullable String commandPermissionToString() {
        return permissionToString(commandPermission, COMMAND_PERMISSION_NAME_MAP);
    }

    /**
     * @return null if command permission not set
     */
    public static @Nullable String commandPermissionToString(int permission) {
        return permissionToString(permission, COMMAND_PERMISSION_NAME_MAP);
    }

    /**
     * @return null if edit permission not set
     */
    public @Nullable String editPermissionToString() {
        return permissionToString(editPermission, EDIT_PERMISSION_NAME_MAP);
    }

    /**
     * @return null if edit permission not set
     */
    public static @Nullable String editPermissionToString(int permission) {
        return permissionToString(permission, EDIT_PERMISSION_NAME_MAP);
    }

    protected static @Nullable String permissionToString(int permission, Map<String, Integer> nameMap) {
        if (permission == -1) return null;
        var tokens = new ArrayList<String>(nameMap.size());
        for (var entry : nameMap.entrySet()) {
            if ((permission & entry.getValue()) != 0) {
                var name = entry.getKey();
                if ("all".equals(name)) {
                    continue;
                }
                tokens.add(name);
            }
        }
        return String.join(" ", tokens);
    }

    public void saveConfig() {
        if (
                this.commandPermission == -1
                        && this.editPermission == -1
                        && this.nudgeProbability == -1
                        && this.disabledTemplateIds == null
                        && this.cooldownTime == -1
        ) {
            return;
        }
        var config = GroupPermissionConfig.builder()
                .commandPermission(commandPermissionToString())
                .editPermission(editPermissionToString())
                .nudgeProbability(nudgeProbability)
                .disabledTemplates(disabledTemplateIds)
                .cooldownTime(cooldownTime)
                .build();
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
            }
            Files.writeString(configPath, config.toJsonString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("Can not save permission config: {}", configPath.toAbsolutePath(), e);
        }
    }

    protected Path getConfigFile(Object id) {
        return service.getPermissionConfigPath().resolve(id.toString() + ".json");
    }

    public int turnOnCommandPermission(String permissions) {
        int permission = permissions.isBlank() ?
                service.getDefaultGroupCommandPermission() : service.stringToCommandPermission(permissions);
        commandPermission = getCommandPermission() | permission;
        return permission;
    }

    public int turnOffCommandPermission(String permissions) {
        int permission = permissions.isBlank() ?
                service.getDefaultGroupCommandPermission() : service.stringToCommandPermission(permissions);
        commandPermission = getCommandPermission() & ~permission;
        return permission;
    }

    protected void checkEditPermission(int permission) {
        if ((getEditPermission() & permission) == 0) {
            //TODO i18n message
            throw new IllegalStateException("Permission denied");
        }
    }

    public float setContactNudgeProbability(String probability) {
        checkEditPermission(ContactPermission.EDIT_NUDGE_PROBABILITY);
        if (probability.isBlank()) {
            return getNudgeProbability();
        }
        if (probability.endsWith("%")) {
            probability = probability.substring(0, probability.length() - 1);
        }
        float p = Float.parseFloat(probability) / 100;
        p = Math.min(Math.max(p, 0), 1);
        this.nudgeProbability = p;
        return p;
    }

    public long setContactCooldownTime(String time) {
        checkEditPermission(ContactPermission.EDIT_COOLDOWN_TIME);
        if (time.isBlank()) {
            return getCooldownTime();
        }
        this.cooldownTime = service.timeParser.parse(time.trim());
        return cooldownTime;
    }

    public void setContactDisabledTemplateIds(String templateIds) {
        checkEditPermission(ContactPermission.EDIT_DISABLE_TEMPLATE_LIST);
        this.disabledTemplateIds = Arrays.stream(templateIds.trim().split(" +"))
                .map(service::getTemplateId)
                .collect(Collectors.toSet());
    }

    /**
     * @param permissionOrId 传入权限列表或模板 ID
     * @param flag           为 true 时启用模板，false 时禁用模板
     */
    protected @Nullable String editCommandPermissionOrTemplate(String permissionOrId, boolean flag) {
        if (permissionOrId.isBlank()) {
            int p = service.getDefaultGroupCommandPermission();
            if (flag) {
                commandPermission = getCommandPermission() | p;
            } else {
                commandPermission = getCommandPermission() & ~p;
            }
            return String.format(
                    flag ? ENABLE_MESSAGE : DISABLE_MESSAGE,
                    commandPermissionToString(commandPermission)
            );
        }

        var tokens = permissionOrId.trim().split("[\\s|&]+");
        var editedTemplates = new ArrayList<String>(tokens.length);
        boolean canEditTemplateList = (getEditPermission() & EDIT_DISABLE_TEMPLATE_LIST) != 0;
        int i = 0;
        for (String token : tokens) {
            Integer p = service.commandPermissionNameMap.get(token);
            if (p == null) {
                var templateIds = service.getTemplateIds(token);
                if (templateIds.length != 0 && canEditTemplateList) {
                    var thisDisabledTemplateIds = getDisabledTemplateIds();
                    for (String templateId : templateIds) {
                        if (flag) {
                            thisDisabledTemplateIds.remove(templateId);
                        } else {
                            thisDisabledTemplateIds.add(templateId);
                        }
                        editedTemplates.add(templateId);
                    }
                }
                continue;
            }
            i |= p;
        }
        String templateMsg = editedTemplates.isEmpty() ? null : String.format(
                flag ? ENABLE_TEMPLATE_MESSAGE : DISABLE_TEMPLATE_MESSAGE,
                String.join(", ", editedTemplates)
        );
        if (i == 0) {
            if (editedTemplates.isEmpty()) {
                return null;
            }
            return templateMsg;
        }
        commandPermission = flag ? getCommandPermission() | i : getCommandPermission() & ~i;
        var permissionMsg = String.format(
                flag ? ENABLE_MESSAGE : DISABLE_MESSAGE,
                commandPermissionToString(i)
        );
        if (editedTemplates.isEmpty()) {
            return permissionMsg;
        }
        return permissionMsg + "\n" + templateMsg;
    }

    public @Nullable String turnOnCommandPermissionOrTemplate(String permissionOrId) {
        return editCommandPermissionOrTemplate(permissionOrId, true);
    }

    public @Nullable String turnOffCommandPermissionOrTemplate(String permissionOrId) {
        return editCommandPermissionOrTemplate(permissionOrId, false);
    }

    /**
     * @return null if edit permission not success
     */
    public @Nullable String handleEditCommand(String command) {
        command = command.trim();
        int spaceIndex = command.indexOf(' ');
        String operation = spaceIndex == -1 ? command : command.substring(0, spaceIndex);
        operation = service.getConfig().getCommandOperationName().getOrDefault(operation, operation);
        String parameter = spaceIndex == -1 ? "" : command.substring(spaceIndex + 1);
        switch (operation) {
            case "on":
            case "enable": {
                return turnOnCommandPermissionOrTemplate(parameter);
            }
            case "off":
            case "disable": {
                return turnOffCommandPermissionOrTemplate(parameter);
            }
            case "nudge_probability":
                float probability = this.setContactNudgeProbability(parameter);
                return String.format(PROBABILITY_MESSAGE, probability * 100);
            case "cooldown_time":
                this.setContactCooldownTime(parameter);
                return String.format(COOLDOWN_MESSAGE, parameter);
            default:
                return null;
        }
    }
}
