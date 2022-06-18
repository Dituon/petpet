package xmmt.dituon.share

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

@Serializable
data class BaseServiceConfig(
    val antialias: Boolean = true
)

enum class Type {
    GIF, IMG
}

@Serializable
data class KeyData(
    val type: Type,
    val avatar: List<AvatarData>,
    val text: List<TextData>,
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

enum class AvatarType {
    FROM, TO, GROUP, BOT
}

@Serializable
data class AvatarData @JvmOverloads constructor(
    val type: AvatarType,
    val pos: JsonArray? = null,
    val format: String? = "png",
    val angle: Int? = 0,
    val round: Boolean? = false,
    val rotate: Boolean? = false,
    val avatarOnTop: Boolean? = true,
    val antialias: Boolean? = false
)

@Serializable
data class AvatarExtraData(
    val fromAvatarUrl: String? = null,
    val toAvatarUrl: String? = null,
    val groupAvatarUrl: String? = null,
    val botAvatarUrl: String? = null
)