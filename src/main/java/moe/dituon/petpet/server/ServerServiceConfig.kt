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
    var preview: Boolean = false,

    val antialias: Boolean = true,
    val resampling: Boolean = true,
//    val serviceThreadPoolSize: Int = 0,
    val gifMaxSize: List<Int> = emptyList(),
    val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    val gifQuality: Int = 5,
    val serviceThreadPoolSize: Int = 0,
    val headless: Boolean = true
) {
    fun stringify(): String {
        return encodeDefaultsPrettyJson.encodeToString(this)
    }

    fun toBaseServiceConfig() = BaseServiceConfig(
        antialias = antialias,
        resampling = resampling,
//        serviceThreadPoolSize = serviceThreadPoolSize,
        gifMaxSize = gifMaxSize,
        gifEncoder = gifEncoder,
        gifQuality = gifQuality,
        threadPoolSize = serviceThreadPoolSize,
        headless = headless
    )

    companion object {
        @JvmStatic
        fun parse(str: String): ServerServiceConfig {
            return Json.decodeFromString(str)
        }
    }
}

const val DEFAULT_AVATAR_URL = "https://q1.qlogo.cn/g?b=qq&nk=2544193782&s=640"

@Serializable
data class PreviewConfigDTO(
    val form: TargetDTO = TargetDTO("form", DEFAULT_AVATAR_URL),
    val to: TargetDTO = TargetDTO("to", DEFAULT_AVATAR_URL),
    val group: TargetDTO = TargetDTO("group", DEFAULT_AVATAR_URL),
    val bot: TargetDTO = TargetDTO("bot", DEFAULT_AVATAR_URL),
    val randomAvatarList: List<String> = emptyList(),
    val textList: List<String> = listOf("petpet!")
) {
    fun stringify(): String {
        return encodeDefaultsPrettyJson.encodeToString(this)
    }

    companion object {
        @JvmStatic
        fun decodeFromString(json: String): PreviewConfigDTO {
            return Json.decodeFromString(json)
        }
    }
}