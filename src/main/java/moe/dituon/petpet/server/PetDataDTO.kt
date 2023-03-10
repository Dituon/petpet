package moe.dituon.petpet.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.dituon.petpet.share.AvatarType
import moe.dituon.petpet.share.BasePetService.VERSION
import moe.dituon.petpet.share.KeyData
import kotlin.streams.toList

@Serializable
data class PetDataDTO(
    val version: Float = VERSION,
    val petData: List<PetDataObject>
) {
    companion object {
        @JvmStatic
        fun stringify(dataMap: Map<String, KeyData>): String {
            val dataList: ArrayList<PetDataObject> = ArrayList()
            dataMap.forEach { (key, data) ->
                if (data.hidden == false) dataList.add(
                    PetDataObject(
                        key,
                        data.avatar.stream().map { a -> a.type }.toList(),
                        data.alias ?: emptyList()
                    )
                )
            }
            return Json.encodeToString(
                PetDataDTO(VERSION, dataList)
            )
        }
    }
}

@Serializable
data class PetDataObject(
    val key: String,
    val types: List<AvatarType>,
    val alias: List<String>
)


