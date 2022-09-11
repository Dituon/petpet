package xmmt.dituon.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xmmt.dituon.share.*
import kotlin.collections.ArrayList

@Serializable
data class PluginConfig(
    val command: String = "pet",
    val probability: Byte = 30,
    val antialias: Boolean = true,
    val disabled: List<String> = emptyList(),
    val commandHead: String = "",
    val respondSelfNudge: Boolean = false,
    val respondReply: Boolean = true,
    val cachePoolSize: Int? = 10000,
    val keyListFormat: ReplyFormat = ReplyFormat.FORWARD,
    val disablePolicy: DisablePolicy = DisablePolicy.FULL,
    val disabledGroups: List<Long> = ArrayList(),
    val fuzzy: Boolean = false,
    val strictCommand: Boolean = true,
    val synchronized: Boolean = false,
    val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    val gifMaxSize: List<Int> = listOf(200, 200, 32),
    val gifQuality: Byte = 90,
    val headless: Boolean = true,
    val autoUpdate: Boolean = true,
    val repositoryUrl: String? = "https://raw.githubusercontent.com/Dituon/petpet/main",
    val devMode: Boolean? = false,
    val coolDown : Int = 10
)

enum class ReplyFormat {
    MESSAGE, FORWARD, IMAGE, URL//TODO
}

enum class DisablePolicy {
    NONE, NUDGE, MESSAGE, FULL
}

fun decode(str: String): PluginConfig {
    return Json.decodeFromString(str)
}

fun encode(config: PluginConfig): String {
    return Json { encodeDefaults = true }.encodeToString(config)
}

fun PluginConfig.toBaseServiceConfig(): BaseServiceConfig {
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