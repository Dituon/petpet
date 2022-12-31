package moe.dituon.petpet.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moe.dituon.petpet.share.BaseServiceConfigInterface
import moe.dituon.petpet.share.Encoder

interface Nudge {
    val probability: Int
    val respondSelfNudge: Boolean
}

interface AutoUpdate {
    val autoUpdate: Boolean
    val repositoryUrl: String
}

interface CoolDown {
    val coolDown: Int
    val groupCoolDown: Int
    val inCoolDownMessage: String
}

interface DevMode {
    val devMode: Boolean
}

interface MessageHook {
    val messageHook: Boolean
}

interface PluginServiceConfigInterface : BaseServiceConfigInterface {
    val command: String
    val disabled: List<String>
    val commandHead: String
    val respondReply: Boolean
    val cachePoolSize: Int
    val keyListFormat: ReplyFormat
    val fuzzy: Boolean
    val strictCommand: Boolean
    val synchronized: Boolean

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
        gifEncoder = gifEncoder,
        gifMaxSize = gifMaxSize,
        gifQuality = gifQuality,
        headless = headless,
        threadPoolSize = threadPoolSize
    )
}

data class PluginServiceConfig(
    override val command: String = "pet",
    override val disabled: List<String> = emptyList(),
    override val commandHead: String = "",
    override val respondReply: Boolean = true,
    override val cachePoolSize: Int = 10000,
    override val keyListFormat: ReplyFormat = ReplyFormat.FORWARD,
    override val fuzzy: Boolean = false,
    override val strictCommand: Boolean = true,
    override val synchronized: Boolean = false,

    // BaseServiceConfig
    override val antialias: Boolean = true,
    override val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    override val gifMaxSize: List<Int> = listOf(200, 200, 32),
    override val gifQuality: Int = 5,
    override val headless: Boolean = true,
    override val threadPoolSize: Int = 0
) : PluginServiceConfigInterface {
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
    val version: Float,
    val dataList: List<String>,
    val fontList: List<String>
) {
    companion object {
        @JvmStatic
        fun parse(str: String): UpdateIndex {
            return Json.decodeFromString(str)
        }
    }
}