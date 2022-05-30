package xmmt.dituon

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

@Serializable
data class ConfigJSON(
    val version: Float, val command: String, val probability: Int, val antialias: Boolean,
    val disabled: JsonArray, val resPath: String
)

fun decode(str: String): ConfigJSON {
    return Json.decodeFromString(str)
}

enum class Type {
    GIF, IMG
}

enum class Avatar {
    SINGLE , DOUBLE
}

@Serializable
data class DataJSON(
    val type: Type, val avatar: Avatar, val pos: JsonArray, val text: String ,
    val round: Boolean, val rotate: Boolean, val avatarOnTop: Boolean
)

fun getData(str: String): DataJSON{
    return Json.decodeFromString(str)
}