package moe.dituon.petpet.websocket.gocq

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import moe.dituon.petpet.share.encodeDefaultsJson

enum class GoCQRole {
    owner, admin, member
}

@Serializable
data class GoCQMemberDTO(
    val user_id: Long,
    val nickname: String,
    val card: String?,
    val role: GoCQRole?
) {
    fun getName(): String = card ?: nickname
}

private val ignoreUnknownKeysJson = Json { ignoreUnknownKeys = true }

enum class GoCQPostType { message }
enum class GoCQMessageType { group }
enum class GoCQMessageSubType { normal, anonymous }

@Serializable
data class GoCQGroupMessageEventDTO(
    val time: Long,
    val self_id: Long,
    val post_type: GoCQPostType,
    val message_type: GoCQMessageType,
    val sub_type: GoCQMessageSubType,
    val message_id: Int,
    val message: JsonArray,
//    val raw_message: String,
    val sender: GoCQMemberDTO,
    val group_id: Long
) {
    companion object {
        @JvmStatic
        fun parse(str: String): GoCQGroupMessageEventDTO {
            return ignoreUnknownKeysJson.decodeFromString(str)
        }

        @JvmStatic
        fun parse(obj: JsonElement): GoCQGroupMessageEventDTO {
            return ignoreUnknownKeysJson.decodeFromJsonElement(obj)
        }
    }
}

@Serializable
data class GoCQRequestDTO(
    val action: String,
    val params: GoCQRequestParamDTO,
    val echo: String = ""
) {
    fun stringify(): String {
        return encodeDefaultsJson.encodeToString(this)
    }
}

@Serializable
sealed interface GoCQRequestParamDTO

@Serializable
@Polymorphic
data class GoCQGetGroupMemberRequestParamDTO(
    val group_id: Long,
    val user_id: Long
) : GoCQRequestParamDTO {
    fun toRequestDTO(): GoCQRequestDTO {
        return GoCQRequestDTO("get_group_member_info", this, user_id.toString())
    }
}

@Serializable
data class GoCQResponseDTO(
    val retcode: Byte,
    val data: JsonObject,
    val echo: String
) {
    fun toGetGroupMemberResponseDTO(): GoCQGetGroupMemberResponseDTO {
        return GoCQGetGroupMemberResponseDTO(
            retcode, ignoreUnknownKeysJson.decodeFromJsonElement(GoCQMemberDTO.serializer(), data), echo
        )
    }

    companion object {
        @JvmStatic
        fun parse(str: String): GoCQResponseDTO {
            return ignoreUnknownKeysJson.decodeFromString(str)
        }
    }
}

@Serializable
data class GoCQGetGroupMemberResponseDTO(
    val retcode: Byte,
    val data: GoCQMemberDTO,
    val echo: String
)

@Serializable
sealed interface GoCQSingleMessageDTO

@Serializable
data class GoCQImageMessageDTO(
    val type: String = "image",
    val data: GoCQImageDataDTO
) : GoCQSingleMessageDTO

@Serializable
data class GoCQImageDataDTO(
    val file: String
) {
    fun toMessage() = GoCQImageMessageDTO("image", this)
}

@Serializable
data class GoCQSendMessageRequestParamDTO(
    val group_id: Long,
    val message: List<GoCQImageMessageDTO>
) : GoCQRequestParamDTO {
    constructor(id: Long, url: String) : this(id, listOf(GoCQImageDataDTO(url).toMessage()))

    fun toRequestDTO() = GoCQRequestDTO("send_msg", this, "")
}
