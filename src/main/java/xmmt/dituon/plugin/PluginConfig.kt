package xmmt.dituon.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xmmt.dituon.share.BaseServiceConfig

@Serializable
data class PluginConfig(
    val version: Float = Petpet.VERSION,
    val command: String = "pet",
    val probability: Int = 30,
    val antialias: Boolean = true,
    val disabled: List<String> = emptyList(),
    val keyCommand: Boolean = false,
    val commandMustAt: Boolean = false,
    val respondImage: Boolean = false,
    val respondSelfNudge: Boolean = false,
    val headless: Boolean = false
)

fun decode(str: String): PluginConfig {
    return Json.decodeFromString(str)
}

fun encode(config: PluginConfig): String {
    return Json { encodeDefaults = true }.encodeToString(config)
}

fun PluginConfig.toBaseServiceConfig(): BaseServiceConfig {
    return BaseServiceConfig(antialias = this.antialias)
}