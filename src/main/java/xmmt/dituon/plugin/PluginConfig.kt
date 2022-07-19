package xmmt.dituon.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import xmmt.dituon.share.AvatarData
import xmmt.dituon.share.BaseServiceConfig
import xmmt.dituon.share.TextData
import xmmt.dituon.share.Type

@Serializable
data class PluginConfig(
    val version: Float = Petpet.VERSION,
    val command: String = "pet",
    val probability: Short = 30,
    val antialias: Boolean = true,
    val disabled: List<String> = emptyList(),
    val keyCommand: Boolean = true,
    val keyCommandHead: String = "",
    val commandMustAt: Boolean = false,
    val respondImage: Boolean = true,
    val respondSelfNudge: Boolean = false,
    val keyListFormat: ReplyFormat = ReplyFormat.MESSAGE,
    val fuzzy: Boolean = false,
    val synchronized: Boolean = false,
    val headless: Boolean = true,
    val autoUpdate: Boolean = true,
    val updateIgnore: List<String> = emptyList(),
    val repositoryUrl: String? = "https://dituon.github.io/petpet"
)

enum class ReplyFormat {
    MESSAGE, FORWARD
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