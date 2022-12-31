package moe.dituon.petpet.websocket.gocq

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moe.dituon.petpet.plugin.*
import moe.dituon.petpet.server.ServerPetService
import moe.dituon.petpet.server.ServerServiceConfigInterface
import moe.dituon.petpet.share.Encoder

@Serializable
data class GoCQPluginConfig (
    val eventWebSocketUrl: String = GoCQPetService.EVENT_WEBSOCKET_URL,
    val apiWebSocketUrl: String = GoCQPetService.API_WEBSOCKET_URL,

    // Server
    override val port: Int = ServerPetService.DEFAULT_PORT,
    override val dataPath: String = ServerPetService.DEFAULT_DATA_PATH,
    override val webServerThreadPoolSize: Int = ServerPetService.DEFAULT_SERVER_THREAD_POOL_SIZE,

    // Plugin
    override val command: String = "pet",
    override val disabled: List<String> = emptyList(),
    override val commandHead: String = "",
    override val respondReply: Boolean = true,
    override val cachePoolSize: Int = 10000,
    override val keyListFormat: ReplyFormat = ReplyFormat.FORWARD,
    override val fuzzy: Boolean = false,
    override val strictCommand: Boolean = true,
    override val synchronized: Boolean = false,

    // AutoUpdate
    override val repositoryUrl: String = DataUpdater.DEFAULT_REPO_URL,
    override val autoUpdate: Boolean = true,

    // CoolDown
    override val coolDown: Int = Cooler.DEFAULT_USER_COOLDOWN,
    override val groupCoolDown: Int = Cooler.DEFAULT_GROUP_COOLDOWN,
    override val inCoolDownMessage: String = Cooler.DEFAULT_MESSAGE,

    // BaseServiceConfig
    override val antialias: Boolean = true,
    override val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    override val gifMaxSize: List<Int> = listOf(200, 200, 32),
    override val gifQuality: Int = 5,
    override val headless: Boolean = true,
    override val threadPoolSize: Int = 0
): PluginServiceConfigInterface, ServerServiceConfigInterface ,AutoUpdate, CoolDown {
    companion object {
        @JvmStatic
        fun parse(str: String): GoCQPluginConfig {
            return Json.decodeFromString(str)
        }
    }
}