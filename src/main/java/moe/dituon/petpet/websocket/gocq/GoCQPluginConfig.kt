package moe.dituon.petpet.websocket.gocq

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.dituon.petpet.plugin.Cooler
import moe.dituon.petpet.plugin.DataUpdater
import moe.dituon.petpet.plugin.PluginServiceConfig
import moe.dituon.petpet.plugin.ReplyFormat
import moe.dituon.petpet.server.ServerPetService
import moe.dituon.petpet.server.ServerServiceConfig
import moe.dituon.petpet.share.Encoder
import moe.dituon.petpet.share.encodeDefaultsPrettyJson

@Serializable
data class GoCQPluginConfig(
    val eventWebSocketUrl: String = GoCQPetService.EVENT_WEBSOCKET_URL,
    val apiWebSocketUrl: String = GoCQPetService.API_WEBSOCKET_URL,

    // Server
    val port: Int = ServerPetService.DEFAULT_PORT,
    val dataPath: String = ServerPetService.DEFAULT_DATA_PATH,
    val webServerThreadPoolSize: Int = ServerPetService.DEFAULT_SERVER_THREAD_POOL_SIZE,

    // Plugin
    val command: String = "pet",
    val disabled: List<String> = emptyList(),
    val commandHead: String = "",
    val respondReply: Boolean = true,
    val cachePoolSize: Int = 10000,
    val keyListFormat: ReplyFormat = ReplyFormat.FORWARD,
    val fuzzy: Boolean = false,
    val strictCommand: Boolean = true,
    val synchronized: Boolean = false,

    // AutoUpdate
    val repositoryUrls: Array<String> = arrayOf(DataUpdater.DEFAULT_REPO_URL),
    val autoUpdate: Boolean = true,

    // CoolDown
    val coolDown: Long = Cooler.DEFAULT_USER_COOLDOWN,
    val groupCoolDown: Long = Cooler.DEFAULT_GROUP_COOLDOWN,
    val inCoolDownMessage: String = Cooler.DEFAULT_MESSAGE,

    // BaseServiceConfig
    val antialias: Boolean = true,
    val resampling: Boolean = true,
//    val serviceThreadPoolSize: Int = 0,
    val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    val gifMaxSize: List<Int> = listOf(200, 200, 32),
    val gifQuality: Int = 5,
    val threadPoolSize: Int = 0,
    val headless: Boolean = true
) {
    fun stringify(): String {
        return encodeDefaultsPrettyJson.encodeToString(this)
    }

    fun toPluginServiceConfig() = PluginServiceConfig(
        command = command,
        disabled = disabled,
        commandHead = commandHead,
        respondReply = respondReply,
        cachePoolSize = cachePoolSize,
        keyListFormat = keyListFormat,
        fuzzy = fuzzy,
        strictCommand = strictCommand,
        synchronized = synchronized,
        antialias = antialias,
        resampling = resampling,
//        serviceThreadPoolSize = serviceThreadPoolSize,
        gifMaxSize = gifMaxSize,
        gifEncoder = gifEncoder,
        gifQuality = gifQuality,
        threadPoolSize = threadPoolSize,
        headless = headless,
        autoUpdate = autoUpdate,
        repositoryUrls = repositoryUrls
    )

    fun toServerServiceConfig() = ServerServiceConfig(
        port = port,
        dataPath = dataPath,
        webServerThreadPoolSize = webServerThreadPoolSize,

        antialias = antialias,
        resampling = resampling,
//        serviceThreadPoolSize = serviceThreadPoolSize,
        gifMaxSize = gifMaxSize,
        gifEncoder = gifEncoder,
        gifQuality = gifQuality,
        serviceThreadPoolSize = threadPoolSize,
        headless = headless
    )

    companion object {
        @JvmStatic
        fun parse(str: String): GoCQPluginConfig {
            return Json.decodeFromString(str)
        }
    }
}