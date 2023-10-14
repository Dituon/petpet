package moe.dituon.petpet.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moe.dituon.petpet.share.BaseServiceConfig
import moe.dituon.petpet.share.Encoder
import moe.dituon.petpet.share.encodeDefaultsIgnoreUnknownKeysJson

//interface Nudge {
//    val probability: Int
//    val respondSelfNudge: Boolean
//}
//
//interface AutoUpdate {
//    val autoUpdate: Boolean
//    val repositoryUrl: String
//}
//
//interface CoolDown {
//    val coolDown: Int
//    val groupCoolDown: Int
//    val inCoolDownMessage: String
//}
//
//interface DevMode {
//    val devMode: Boolean
//}
//
//interface MessageHook {
//    val messageHook: Boolean
//}

//@Serializable
//abstract class AbstractPluginServiceConfig : AbstractBaseServiceConfig() {
//    abstract val command: String
//    abstract val disabled: List<String>
//    abstract val commandHead: String
//    abstract val respondReply: Boolean
//    abstract val cachePoolSize: Int
//    abstract val keyListFormat: ReplyFormat
//    abstract val fuzzy: Boolean
//    abstract val strictCommand: Boolean
//    abstract val synchronized: Boolean
//}

@Serializable
data class PluginServiceConfig(
    val command: String = "pet",
    val disabled: List<String> = emptyList(),
    val commandHead: String = "",
    val respondReply: Boolean = true,
    val cachePoolSize: Int = 10000,
    val keyListFormat: ReplyFormat = ReplyFormat.FORWARD,
    val fuzzy: Boolean = false,
    val strictCommand: Boolean = true,
    val synchronized: Boolean = false,

    val antialias: Boolean = true,
    val resampling: Boolean = true,
//    val serviceThreadPoolSize: Int = 0,
    val gifMaxSize: List<Int> = emptyList(),
    val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    val gifQuality: Int = 5,

    val threadPoolSize: Int = 0,
    val headless: Boolean = true,

    val autoUpdate: Boolean = true,
    val repositoryUrls: Array<String> = emptyArray()
) {

    fun toBaseServiceConfig() = BaseServiceConfig(
        antialias = antialias,
        resampling = resampling,
//        serviceThreadPoolSize = serviceThreadPoolSize,
        gifMaxSize = gifMaxSize,
        gifEncoder = gifEncoder,
        gifQuality = gifQuality,
        gifEncoderThreadPoolSize = threadPoolSize,
        headless = headless
    )

    companion object {
        @JvmStatic
        fun parse(str: String): PluginServiceConfig {
            return Json.decodeFromString(str)
        }
    }
}

enum class ReplyFormat {
    MESSAGE, FORWARD, IMAGE, URL//TODO
}

enum class DisablePolicy {
    NONE, NUDGE, MESSAGE, FULL
}

@Serializable
data class UpdateIndex(
    val version: Float = 0f,
    val dataPath: String = DataUpdater.DEFAULT_REPO_DATA_PATH,
    val dataList: List<String> = emptyList(),
    val fontList: List<String> = emptyList(),
    var url: String = ""
) {
    companion object {
        @JvmStatic
        fun parse(str: String): UpdateIndex {
            return encodeDefaultsIgnoreUnknownKeysJson.decodeFromString(str)
        }
    }
}