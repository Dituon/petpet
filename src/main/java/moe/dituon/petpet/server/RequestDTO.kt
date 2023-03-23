package moe.dituon.petpet.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class RequestDTO(
    val key: String,
    val from: TargetDTO = TargetDTO("from", ""),
    val to: TargetDTO = TargetDTO("to", ""),
    val group: TargetDTO = TargetDTO("group", ""),
    val bot: TargetDTO = TargetDTO("bot", ""),
    val randomAvatarList: List<String> = emptyList(),
    val textList: List<String> = emptyList()
) {
    companion object {
        @JvmStatic
        fun parse(json: String): RequestDTO {
            return Json.decodeFromString(json)
        }
    }
}

@Serializable
data class TargetDTO(
    val name: String,
    val avatar: String
)
