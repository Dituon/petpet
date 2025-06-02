package moe.dituon.petpet.bot.qq

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.dituon.petpet.bot.utils.Cooler
import moe.dituon.petpet.core.BaseRenderConfig
import moe.dituon.petpet.core.FontManager
import moe.dituon.petpet.service.TemplateUpdaterConfig
import java.util.concurrent.TimeUnit

val DEFAULT_COMMAND_PERMISSION_NAME = mapOf(
    "所有" to "all",
    "cmd" to "command",
    "指令" to "command",
    "提及" to "at",
    "img" to "image",
    "回复" to "image",
    "图像" to "image",
    "id" to "command_head",
    "key" to "command_head",
    "指令头" to "command_head",
)

val DEFAULT_COMMAND_OPERATION_NAME = mapOf(
    "启用" to "on",
    "禁用" to "off",
    "概率" to "nudge_probability",
    "戳一戳概率" to "nudge_probability",
    "冷却" to "cooldown_time",
    "冷却时间" to "cooldown_time",
    "禁用" to "disable_template",
    "禁用模板" to "disable_template",
)

val DEFAULT_TIME_UNIT_NAME = mapOf(
    "毫秒" to 1L,
    "秒" to TimeUnit.SECONDS.toMillis(1L),
    "分" to TimeUnit.MINUTES.toMillis(1L),
    "小时" to TimeUnit.HOURS.toMillis(1L),
    "天" to TimeUnit.DAYS.toMillis(1L),
)

const val DEFAULT_DEFAULT_GROUP_EDIT_PERMISSION = "command_permission nudge_probability disable_template"

enum class ReplyType {
    @SerialName("random")
    RANDOM,
    @SerialName("text")
    TEXT,
    @SerialName("forward_text")
    FORWARD_TEXT,
    @SerialName("template")
    TEMPLATE,
    @SerialName("url")
    URL, // TODO
}

@Serializable
data class QQBotConfig(
    val command: String = "pet",
    @SerialName("command_head")
    val commandHead: String = "",
    @SerialName("respond_self_nudge")
    val respondSelfNudge: Boolean = false,
    @SerialName("respond_friend")
    val respondFriend: Boolean = true,
    @SerialName("respond_group")
    val respondGroup: Boolean = true,
    @SerialName("default_font_family")
    val defaultFontFamily: String = FontManager.DEFAULT_FONT,

    @SerialName("default_reply_type")
    val defaultReplyType: ReplyType = ReplyType.TEMPLATE,
    @SerialName("default_template")
    val defaultTemplate: String? = null,
    @SerialName("command_permission_name")
    val commandPermissionName: Map<String, String> = DEFAULT_COMMAND_PERMISSION_NAME,
    @SerialName("time_unit_name")
    val timeUnitName: Map<String, Long> = DEFAULT_TIME_UNIT_NAME,
    @SerialName("command_operation_name")
    val commandOperationName: Map<String, String> = DEFAULT_COMMAND_OPERATION_NAME,

    @SerialName("group_command_permission")
    val defaultGroupCommandPermission: String = "all",
    @SerialName("group_edit_permission")
    val defaultGroupEditPermission: String = DEFAULT_DEFAULT_GROUP_EDIT_PERMISSION,
    @SerialName("nudge_probability")
    val nudgeProbability: Float = QQBotService.DEFAULT_NUDGE_PROBABILITY,
    @SerialName("disabled_groups")
    val disabledGroups: Set<Long> = emptySet(),
    @SerialName("disabled_templates")
    val disabledTemplates: Set<String> = emptySet(),
    @SerialName("image_cache_pool_size")
    val imageCachePoolSize: Int = 2048,
    @SerialName("group_cooldown_time")
    val groupCooldownTime: Long = Cooler.DEFAULT_GROUP_COOLDOWN,
    @SerialName("user_cooldown_time")
    val userCooldownTime: Long = Cooler.DEFAULT_USER_COOLDOWN,
    @SerialName("in_cooldown_message")
    val inCoolDownMessage: String = Cooler.DEFAULT_MESSAGE,

    val headless: Boolean = true,

    val update: TemplateUpdaterConfig = TemplateUpdaterConfig(),

    @SerialName("gif_quality")
    override val gifQuality: Int = 10,
): BaseRenderConfig(
    gifQuality = gifQuality,
)
