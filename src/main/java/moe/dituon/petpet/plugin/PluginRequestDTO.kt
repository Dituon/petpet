package moe.dituon.petpet.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Stranger

@Serializable
data class PluginRequestDTO(
    val key: String,
    val form: PluginRequestTargetDTO = PluginRequestTargetDTO(),
    val to: PluginRequestTargetDTO = PluginRequestTargetDTO(),
    val group: PluginRequestTargetDTO = PluginRequestTargetDTO(),
    val bot: PluginRequestTargetDTO = PluginRequestTargetDTO(),
    val randomAvatarList: List<String> = emptyList(),
    val textList: List<String> = emptyList()
) {
    companion object {
        @JvmStatic
        fun decodeFromString(json: String): PluginRequestDTO {
            return Json.decodeFromString(json)
        }
    }
}

@Serializable
data class PluginRequestTargetDTO(
    val name: String? = null,
    val avatar: String? = null,
    val qq: Long? = null
) {
    fun getURL(): String? {
        return avatar ?: if (qq != null) "http://q1.qlogo.cn/g?b=qq&nk=${qq}&s=640" else null
    }
}
