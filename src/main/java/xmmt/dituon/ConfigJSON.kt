package xmmt.dituon

import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class ConfigJSON(
    val command: String, val probability: Int, val antialias: Boolean
)

fun decode(str: String): ConfigJSON {
    return Json.decodeFromString(str)
}