package moe.dituon.petpet.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.dituon.petpet.share.BaseServiceConfig
import moe.dituon.petpet.share.Encoder
import moe.dituon.petpet.share.encodeDefaultsPrettyJson

//@Serializable
//sealed class AbstractServerServiceConfig : AbstractBaseServiceConfig() {
//    abstract val port: Int
//    abstract val dataPath: String
//    abstract val webServerThreadPoolSize: Int
//}

@Serializable
data class ServerServiceConfig(
    val port: Int = ServerPetService.DEFAULT_PORT,
    val dataPath: String = ServerPetService.DEFAULT_DATA_PATH,
    val webServerThreadPoolSize: Int = ServerPetService.DEFAULT_SERVER_THREAD_POOL_SIZE,

    val antialias: Boolean = true,
    val gifMaxSize: List<Int> = emptyList(),
    val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    val gifQuality: Int = 5,
    val threadPoolSize: Int = 0,
    val headless: Boolean = true
) {
    fun stringify(): String {
        return encodeDefaultsPrettyJson.encodeToString(this)
    }

    fun toBaseServiceConfig() = BaseServiceConfig(
        antialias = antialias,
        gifMaxSize = gifMaxSize,
        gifEncoder = gifEncoder,
        gifQuality = gifQuality,
        threadPoolSize = threadPoolSize,
        headless = headless
    )

    companion object {
        @JvmStatic
        fun parse(str: String): ServerServiceConfig {
            return Json.decodeFromString(str)
        }
    }
}