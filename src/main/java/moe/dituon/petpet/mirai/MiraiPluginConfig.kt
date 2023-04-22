package moe.dituon.petpet.mirai

import moe.dituon.petpet.plugin.*
import moe.dituon.petpet.share.Encoder
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object MiraiPluginConfig : AutoSavePluginConfig("PetPet")
//    ,Nudge, AutoUpdate, CoolDown, DevMode, MessageHook 
{
    @ValueDescription("触发 petpet 的指令")
    val command: String by value("pet")

    @ValueDescription("使用 戳一戳 的触发概率")
    val probability: Int by value(30)

    @ValueDescription("是否使用抗锯齿")
    val antialias: Boolean by value(true)

    @ValueDescription("是否使用重采样")
    val resampling: Boolean by value(true)

    @ValueDescription("禁用列表")
    val disabled: List<String> by value(emptyList())

    @ValueDescription("keyCommand前缀")
    val commandHead: String by value("")

    @ValueDescription("是否响应机器人发出的戳一戳")
    val respondSelfNudge: Boolean by value(false)

    @ValueDescription("是否使用响应回复")
    val respondReply: Boolean by value(true)

    @ValueDescription("消息缓存池容量")
    val cachePoolSize: Int by value(10000)

    @ValueDescription("keyList响应格式")
    val keyListFormat: ReplyFormat by value(ReplyFormat.FORWARD)

    @ValueDescription("禁用群聊")
    val disabledGroups: List<Long> by value(emptyList())

    @ValueDescription("禁用策略")
    val disablePolicy: DisablePolicy by value(DisablePolicy.FULL)

    @ValueDescription("是否使用模糊匹配用户名")
    val fuzzy: Boolean by value(false)

    @ValueDescription("是否使用严格匹配模式")
    val strictCommand: Boolean by value(true)

    @ValueDescription("是否使用消息事件同步锁")
    val synchronized: Boolean by value(false)

//    @ValueDescription("核心线程池容量, 填入0为CPU线程数+1 (默认值)")
//    val serviceThreadPoolSize: Int by value(0)

    @ValueDescription("GIF编码器")
    val gifEncoder: Encoder by value(Encoder.ANIMATED_LIB)

    @ValueDescription("GIF缩放阈值/尺寸")
    val gifMaxSize: List<Int> by value(listOf(200, 200, 32))

    @ValueDescription("GIF质量, 仅适用于ANIMATED_LIB编码器, 1为质量最佳, 超过20不会有明显性能提升")
    val gifQuality: Int by value(5)

    @ValueDescription("是否使用headless模式")
    val headless: Boolean by value(true)

    @ValueDescription("GIF合成线程池容量, 填入0为CPU线程数+1 (默认值)")
    val gifEncoderThreadPoolSize: Int by value(0)

    @ValueDescription("是否自动从仓库同步PetData")
    val autoUpdate: Boolean by value(true)

    @ValueDescription("用于自动更新的仓库地址")
    val repositoryUrl: String by value(DataUpdater.DEFAULT_REPO_URL)

    @ValueDescription("是否启用开发模式 (支持热重载)")
    val devMode: Boolean by value(false)

    @ValueDescription("是否启用消息注入 (详见文档)")
    val messageHook: Boolean by value(false)

    @ValueDescription("触发图片生成后的用户冷却时长, 填入-1则禁用, 单位为毫秒")
    val coolDown:Long by value(Cooler.DEFAULT_USER_COOLDOWN)

    @ValueDescription("触发图片生成后的群聊冷却时长 (同上)")
    val groupCoolDown: Long by value(Cooler.DEFAULT_GROUP_COOLDOWN)

    @ValueDescription("触发冷却后的回复消息, '[nudge]'为戳一戳, 可使用消息注入MessageHook (详见文档)")
    val inCoolDownMessage: String by value(Cooler.DEFAULT_MESSAGE)

    @ValueDescription("启动WebServer (详见文档)")
    val startWebServer: Boolean by value(false)

    fun toPluginServiceConfig() = PluginServiceConfig(
        command = command,
        disabled = disabled,
        commandHead = commandHead,
        respondReply = respondReply,
        cachePoolSize = cachePoolSize,
        keyListFormat = keyListFormat,
        fuzzy = fuzzy,
        strictCommand = strictCommand,
        synchronized = synchronized,
        antialias = antialias,
//        serviceThreadPoolSize = serviceThreadPoolSize,
        gifMaxSize = gifMaxSize,
        gifEncoder = gifEncoder,
        gifQuality = gifQuality,
        threadPoolSize = gifEncoderThreadPoolSize,
        headless = headless
    )
}