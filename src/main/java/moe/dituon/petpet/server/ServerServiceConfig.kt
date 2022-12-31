package moe.dituon.petpet.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moe.dituon.petpet.share.BaseServiceConfigInterface
import moe.dituon.petpet.share.Encoder

interface ServerServiceConfigInterface : BaseServiceConfigInterface{
    val port: Int
    val dataPath: String
    val webServerThreadPoolSize: Int

    fun toServerServiceConfig() = ServerServiceConfig(
        port = port,
        dataPath = dataPath,
        webServerThreadPoolSize = webServerThreadPoolSize,
        antialias = antialias,
        gifMaxSize = gifMaxSize,
        gifEncoder = gifEncoder,
        gifQuality = gifQuality,
        threadPoolSize = threadPoolSize,
        headless = headless
    )
}

@Serializable
data class ServerServiceConfig (
    override val port: Int = ServerPetService.DEFAULT_PORT,
    override val dataPath: String = ServerPetService.DEFAULT_DATA_PATH,
    override val webServerThreadPoolSize: Int = ServerPetService.DEFAULT_SERVER_THREAD_POOL_SIZE,
    override val antialias: Boolean = true,
    override val gifMaxSize: List<Int> = emptyList(),
    override val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    override val gifQuality: Int = 5,
    override val threadPoolSize: Int = 0,
    override val headless: Boolean = true
) : ServerServiceConfigInterface {
    companion object {
        @JvmStatic
        fun parse(str: String): ServerServiceConfig {
            return Json.decodeFromString(str)
        }
    }
}