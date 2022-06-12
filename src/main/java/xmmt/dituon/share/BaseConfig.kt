package xmmt.dituon.share

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import xmmt.dituon.plugin.Petpet

@Serializable
data class BaseServiceConfig(
    val antialias: Boolean = true
)

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
data class TextExtraData(
    val fromReplacement: String,
    val toReplacement: String,
    val groupReplacement: String,
    val textList: List<String>
)