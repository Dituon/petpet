package xmmt.dituon.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class ServerConfig(
    val port: Int = 2333,
    val threadPoolSize: Int = 10,
    val headless: Boolean = true
) {
    companion object {
        @JvmStatic
        fun getConfig(str: String): ServerConfig {
            return Json.decodeFromString(str)
        }
    }
}