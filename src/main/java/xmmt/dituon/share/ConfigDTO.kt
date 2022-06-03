package xmmt.dituon.share

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject


@Serializable
data class ConfigDTO(
    val version: Float = 2.2F,
    val command: String = "pet",
    val probability: Int = 30,
    val antialias: Boolean = false,
    val disabled: List<String> = emptyList(),
    val keyCommand: Boolean = false,
    val respondImage: Boolean = false
)

fun decode(str: String): ConfigDTO {
    return Json.decodeFromString(str)
}

fun encode(config: ConfigDTO): String {
    return Json { encodeDefaults = true }.encodeToString(config)
}

enum class Type {
    GIF, IMG
}

enum class Avatar {
    SINGLE, DOUBLE
}

@Serializable
data class DataJSON(
    val type: Type, val avatar: Avatar, val pos: JsonArray, val text: JsonArray,
    val round: Boolean, val rotate: Boolean, val avatarOnTop: Boolean
)

fun getData(str: String): DataJSON {
    return Json.decodeFromString(str)
}