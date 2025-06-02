package moe.dituon.petpet.bot.qq.onebot

import cn.evolvefield.onebot.client.config.BotConfig
import kotlinx.serialization.Serializable
import moe.dituon.petpet.bot.qq.*
import moe.dituon.petpet.bot.utils.Cooler
import moe.dituon.petpet.core.FontManager
import moe.dituon.petpet.service.TemplateUpdaterConfig
import net.mamoe.yamlkt.Comment

@Serializable
data class OnebotConfig(
    @Comment("websocket 地址")
    val url: String = "ws://127.0.0.1:3001",

    @Comment("反向 websocket 端口")
    val reversedPort: Int = -1,

    @Comment("token 或 verifyKey 鉴权")
    val token: String = "",

    @Comment("发送消息时，是否使用 CQ 码")
    val useCQCode: Boolean = false,

    @Comment("重连尝试次数")
    val retryTimes: Int = 5,

    @Comment("重连间隔 (毫秒)")
    val retryWaitMills: Long = 5_000L,

    @Comment("重连休息时间 (毫秒)")
    val retryRestMills: Long = 60_000L,

    @Comment("心跳包检测时间 (秒)，设为 0 关闭检测")
    val heartbeatCheckSeconds: Int = 60,


    @Comment("模板路径")
    val templatePath: List<String> = listOf("./data/xmmt.dituon.petpet/"),

    @Comment("字体路径")
    val fontPath: List<String> = listOf("./data/xmmt.dituon.petpet/fonts"),

    @Comment("触发 petpet 的指令")
    val command: String = "pet",

    @Comment("群聊中使用 戳一戳 的触发概率 (0 - 1)")
    val nudgeProbability: Float = 0.3f,

    @Comment("禁用列表")
    val disabledTemplates: Set<String> = emptySet(),

    @Comment("模板触发前缀")
    val commandHead: String = "",

    @Comment("是否响应机器人发出的戳一戳")
    val respondSelfNudge: Boolean = false,

    @Comment("是否响应私聊 (包括临时会话) 消息")
    val respondFriend: Boolean = true,

    @Comment("是否响应群聊消息")
    val respondGroup: Boolean = true,

    @Comment("消息缓存池容量")
    val imageCachePoolSize: Int = 2048,

    @Comment("默认回复类型, 详见文档")
    val defaultReplyType: ReplyType = ReplyType.TEMPLATE,

    @Comment("默认回复类型为 `template` 时的模板 id, 留空则允许模板自行注册")
    val defaultTemplate: String? = null,

    @Comment("禁用群聊 (暂时无效)")
    val disabledGroups: Set<Long> = emptySet(),

    @Comment("默认字体")
    val defaultFontFamily: String = FontManager.DEFAULT_FONT,

//    @Comment("是否使用模糊匹配用户名")
//    val fuzzy: Boolean = false,

//    @Comment("GIF缩放阈值/尺寸 (暂时不可用)")
//    val gifMaxSize: List<Int> = listOf(200, 200, 32),

    @Comment("GIF 质量, 1 为质量最佳, 超过 20 不会有明显性能提升")
    val gifQuality: Int = 5,

    @Comment("是否启用 headless 模式")
    val headless: Boolean = true,

//    @Comment("是否自动从仓库更新模板")
//    val autoUpdate: Boolean = true,
//
//    @Comment("用于自动更新的仓库地址")
//    val repositoryUrls: List<String> = listOf(""),

//    @Comment("是否启用开发模式 (支持热重载)")
//    val devMode: Boolean = false,

    @Comment("触发图片生成后的用户冷却时长 (毫秒), 设为 0 禁用")
    val userCooldownTime: Long = Cooler.DEFAULT_USER_COOLDOWN,

    @Comment("触发图片生成后的群聊冷却时长 (同上)")
    val groupCooldownTime: Long = Cooler.DEFAULT_GROUP_COOLDOWN,

    @Comment("触发冷却后的回复消息, '${QQBotService.REPLY_NUDGE_KEYWORD}' 为戳一戳, 设为空字符串禁用")
    val inCoolDownMessage: String = Cooler.DEFAULT_MESSAGE,

    @Comment("指令权限别名")
    val commandPermissionName: Map<String, String> = DEFAULT_COMMAND_PERMISSION_NAME,

    @Comment("管理操作指令别名")
    val commandOperationName: Map<String, String> = DEFAULT_COMMAND_OPERATION_NAME,

    @Comment("时间单位别名")
    val timeUnitName: Map<String, Long> = DEFAULT_TIME_UNIT_NAME,

    @Comment("默认群聊权限")
    val defaultGroupCommandPermission: String = "all",

    @Comment("默认群聊管理权限")
    val defaultGroupEditPermission: String = DEFAULT_DEFAULT_GROUP_EDIT_PERMISSION,

    @Comment("HTTP 图像服务器端口")
    val httpServerPort: Int = OnebotBotService.DEFAULT_HTTP_SERVER_PORT,

    @Comment("传递给 Onebot 协议端的图像服务器地址")
    val httpServerUrl: String = OnebotBotService.DEFAULT_HTTP_SERVER_URL,

    @Comment("自动更新配置, 详见文档")
    val update: TemplateUpdaterConfig = TemplateUpdaterConfig()
) {
    fun toClientConfig() = BotConfig(
        url,
        reversedPort,
        token,
        token.isNotBlank(),
        false,
        useCQCode,
        retryTimes,
        retryWaitMills,
        retryRestMills,
        heartbeatCheckSeconds,
        false
    )

    fun toQQBotConfig() = QQBotConfig(
        command = command,
        commandHead = commandHead,
        respondSelfNudge = respondSelfNudge,
        respondFriend = respondFriend,
        respondGroup = respondGroup,
        defaultFontFamily = defaultFontFamily,
        defaultReplyType = defaultReplyType,
        defaultTemplate = defaultTemplate,
        commandPermissionName = commandPermissionName,
        timeUnitName = timeUnitName,
        commandOperationName = commandOperationName,
        defaultGroupCommandPermission = defaultGroupCommandPermission,
        defaultGroupEditPermission = defaultGroupEditPermission,
        nudgeProbability = nudgeProbability,
        disabledGroups = disabledGroups,
        disabledTemplates = disabledTemplates,
        imageCachePoolSize = imageCachePoolSize,
        groupCooldownTime = groupCooldownTime,
        userCooldownTime = userCooldownTime,
        inCoolDownMessage = inCoolDownMessage,
        headless = headless,
        update = update,
        gifQuality = gifQuality,
    )
}