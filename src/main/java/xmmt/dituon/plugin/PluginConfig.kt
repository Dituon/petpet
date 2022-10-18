package xmmt.dituon.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import xmmt.dituon.share.*
import kotlin.collections.ArrayList

object Config : AutoSavePluginConfig("PetPet") {
    @ValueDescription("触发 petpet 的指令")
    val command: String by value("pet")
    @ValueDescription("使用 戳一戳 的触发概率")
    val probability: Int by value(30)
    @ValueDescription("是否使用抗锯齿")
    val antialias: Boolean by value(true)
    @ValueDescription("禁用列表")
    val disabled: List<String> by value(emptyList())
    @ValueDescription("keyCommand前缀")
    val commandHead: String by value("")
    @ValueDescription("是否响应机器人发出的戳一戳")
    val respondSelfNudge: Boolean by value(false)
    @ValueDescription("是否使用响应回复")
    val respondReply: Boolean by value(true)
    @ValueDescription("消息缓存池容量")
    val cachePoolSize: Int? by value(10000)
    @ValueDescription("keyList响应格式")
    val keyListFormat: ReplyFormat by value(ReplyFormat.FORWARD)
    @ValueDescription("禁用策略")
    val disablePolicy: DisablePolicy by value(DisablePolicy.FULL)
    @ValueDescription("禁用群聊列表")
    val disabledGroups: List<Long> by value(ArrayList())
    @ValueDescription("是否使用模糊匹配用户名")
    val fuzzy: Boolean by value(false)
    @ValueDescription("是否使用严格匹配模式")
    val strictCommand: Boolean by value(true)
    @ValueDescription("是否使用消息事件同步锁")
    val synchronized: Boolean by value(false)
    @ValueDescription("GIF编码器")
    val gifEncoder: Encoder by value(Encoder.ANIMATED_LIB)
    @ValueDescription("GIF缩放阈值/尺寸")
    val gifMaxSize: List<Int> by value(listOf(200, 200, 32))
    @ValueDescription("GIF质量, 仅适用于ANIMATED_LIB编码器")
    val gifQuality: Int by value(90)
    @ValueDescription("是否使用headless模式")
    val headless: Boolean by value(true)
    @ValueDescription("图片合成线程池容量, 填入0为CPU线程数+1 (默认值)")
    val threadPoolSize: Int by value(0)
    @ValueDescription("是否自动从仓库同步PetData")
    val autoUpdate: Boolean by value(true)
    @ValueDescription("用于自动更新的仓库地址")
    val repositoryUrl: String? by value("https://raw.githubusercontent.com/Dituon/petpet/main")
    @ValueDescription("是否启用开发模式 (支持热重载)")
    val devMode: Boolean? by value(false)
    @ValueDescription("触发图片生成后的用户冷却时长, 填入-1则禁用, 单位为秒")
    val coolDown : Int by value(10)
    @ValueDescription("触发图片生成后的群聊冷却时长")
    val groupCoolDown: Int by value(-1)
    @ValueDescription("触发冷却后的回复消息, '[nudge]'为戳一戳")
    val inCoolDownMessage: String by value("技能冷却中...")
}

enum class ReplyFormat {
    MESSAGE, FORWARD, IMAGE, URL//TODO
}

enum class DisablePolicy {
    NONE, NUDGE, MESSAGE, FULL
}

fun decode(str: String): Config {
    return Json.decodeFromString(str)
}

fun encode(config: Config): String {
    return Json { encodeDefaults = true }.encodeToString(config)
}

fun Config.toBaseServiceConfig(): BaseServiceConfig {
    return BaseServiceConfig(antialias = this.antialias)
}

@Serializable
data class UpdateIndex(
    val version: Float,
    val dataList: List<String>,
    val fontList: List<String>,
) {
    companion object {
        @JvmStatic
        fun getUpdate(str: String): UpdateIndex {
            return Json.decodeFromString(str)
        }
    }
}