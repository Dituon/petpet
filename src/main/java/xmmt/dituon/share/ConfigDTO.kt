package xmmt.dituon.share

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement


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
    NONE, SINGLE, DOUBLE
}

@Serializable
data class KeyData(
    val type: Type,
    val avatar: Avatar,
    val pos: JsonArray,
    val text: List<TextData>,
    val round: Boolean,
    val rotate: Boolean,
    val avatarOnTop: Boolean
)

fun getData(str: String): KeyData {
    return Json.decodeFromString(str)
}

@Serializable
data class TextData @JvmOverloads constructor(
    val text: String,
    val pos: List<Int>? = null,
    val color: JsonElement? = null,
    val font: String? = null,
    val size: Int? = null
)

@Serializable
data class TextExtraData (
    var fromReplacement: String,
    val toReplacement: String,
    val groupReplacement: String,
)