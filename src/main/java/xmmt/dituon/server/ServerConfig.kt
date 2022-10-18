package xmmt.dituon.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import xmmt.dituon.share.BaseConfigFactory
import xmmt.dituon.share.BaseServiceConfig
import xmmt.dituon.share.BufferedGifEncoder
import xmmt.dituon.share.Encoder

@Serializable
data class ServerConfig (
    val port: Int = 2333,
    val dataPath: String = "data/xmmt.dituon.petpet",
    val webServerThreadPoolSize: Int = 10,
    val gifMaxSize: List<Int> = emptyList(),
    val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    val gifMakerThreadPoolSize: Int = 0,
    val headless: Boolean = true
) {
    companion object {
        @JvmStatic
        fun getConfig(str: String): ServerConfig {
            return Json.decodeFromString(str)
        }
    }
}