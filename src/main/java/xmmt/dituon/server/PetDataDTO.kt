package xmmt.dituon.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xmmt.dituon.share.AvatarType
import xmmt.dituon.share.BasePetService.VERSION
import xmmt.dituon.share.KeyData
import kotlin.streams.toList

@Serializable
data class PetDataDTO(
    val version: Float = VERSION,
    val petData: List<PetDataObject>
) {
    companion object {
        @JvmStatic
        fun encodeToString(dataMap: Map<String, KeyData>): String{
            val dataList: ArrayList<PetDataObject> = ArrayList()
            dataMap.forEach { (key, data) ->
                dataList.add(PetDataObject(key, data.avatar.stream().map { a -> a.type }.toList()))
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
    val types: List<AvatarType>
)


