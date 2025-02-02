package moe.dituon.petpet.bot.qq.mirai

import moe.dituon.petpet.bot.qq.*
import moe.dituon.petpet.bot.utils.Cooler
import moe.dituon.petpet.core.FontManager
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object MiraiPluginConfig : AutoSavePluginConfig("PetPet") {
    @ValueDescription("触发 petpet 的指令")
    val command: String by value("pet")

    @ValueDescription("群聊中使用 戳一戳 的触发概率 (0 - 100)")
    val probability: Float by value(30f)

    @ValueDescription("禁用列表")
    val disabled: Set<String> by value(emptySet())

    @ValueDescription("模板触发前缀")
    val commandHead: String by value("")

    @ValueDescription("是否响应机器人发出的戳一戳 (暂时无效)")
    val respondSelfNudge: Boolean by value(false)

    @ValueDescription("是否响应群聊消息")
    val respondGroup: Boolean by value(true)

    @ValueDescription("是否响应私聊 (包括临时会话) 消息")
    val respondFriend: Boolean by value(true)

    @ValueDescription("默认回复类型, 详见文档")
    val defaultReplyType: ReplyType by value(ReplyType.TEMPLATE)

    @ValueDescription("默认回复类型为 `template` 时的模板 id, 留空则允许模板自行注册")
    val defaultTemplate: String by value("")

    @ValueDescription("消息缓存池容量")
    val cachePoolSize: Int by value(2048)

    @ValueDescription("禁用群聊 (暂时无效)")
    val disabledGroups: Set<Long> by value(emptySet())

    @ValueDescription("默认字体")
    val defaultFontFamily: String by value(FontManager.DEFAULT_FONT)

//    @ValueDescription("是否使用模糊匹配用户名")
//    val fuzzy: Boolean by value(false)

//    @ValueDescription("是否使用消息事件同步锁")
//    val synchronized: Boolean by value(false)

    @ValueDescription("GIF缩放阈值/尺寸 (暂时不可用)")
    val gifMaxSize: List<Int> by value(listOf(200, 200, 32))

    @ValueDescription("GIF 质量, 1 为质量最佳, 超过 20 不会有明显性能提升")
    val gifQuality: Int by value(5)

    @ValueDescription("是否使用 headless 模式")
    val headless: Boolean by value(true)

    @ValueDescription("是否自动从仓库更新模板 (暂时无效)")
    val autoUpdate: Boolean by value(true)

    @ValueDescription("用于自动更新的仓库地址 (暂时无效)")
    val repositoryUrls: List<String> by value(listOf(""))

//    @ValueDescription("是否启用开发模式 (支持热重载)")
//    val devMode: Boolean by value(false)

//    @ValueDescription("是否启用消息注入 (详见文档)")
//    val messageHook: Boolean by value(false)

    @ValueDescription("触发图片生成后的用户冷却时长 (毫秒), 设为 0 禁用")
    val userCoolDown:Long by value(Cooler.DEFAULT_USER_COOLDOWN)

    @ValueDescription("触发图片生成后的群聊冷却时长 (同上)")
    val groupCoolDown: Long by value(Cooler.DEFAULT_GROUP_COOLDOWN)

    @ValueDescription("触发冷却后的回复消息, '${QQBotService.REPLY_NUDGE_KEYWORD}' 为戳一戳, 设为空字符串禁用")
    val inCoolDownMessage: String by value(Cooler.DEFAULT_MESSAGE)

    @ValueDescription("指令权限别名")
    val commandPermissionName: Map<String, String> by value(DEFAULT_COMMAND_PERMISSION_NAME)

    @ValueDescription("管理操作指令别名")
    val commandOperationName: Map<String, String> by value(DEFAULT_COMMAND_OPERATION_NAME)

    @ValueDescription("时间单位别名")
    val timeUnitName: Map<String, Long> by value(DEFAULT_TIME_UNIT_NAME)

    @ValueDescription("默认群聊权限")
    val defaultGroupCommandPermission: String by value("all")

    @ValueDescription("默认群聊管理权限")
    val defaultGroupEditPermission: String by value(DEFAULT_DEFAULT_GROUP_EDIT_PERMISSION)

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
        nudgeProbability = probability / 100f,
        disabledGroups = disabledGroups,
        disabledTemplates = disabled,
        imageCachePoolSize = cachePoolSize,
        groupCooldownTime = groupCoolDown,
        userCooldownTime = userCoolDown,
        inCoolDownMessage = inCoolDownMessage,
        autoUpdate = autoUpdate,
        repositoryUrls = repositoryUrls,
        headless = headless,
        gifQuality = gifQuality,
    )
}