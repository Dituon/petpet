package moe.dituon.petpet.httpserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.dituon.petpet.core.GlobalContext
import moe.dituon.petpet.core.element.PetpetModel
import moe.dituon.petpet.service.BaseService
import moe.dituon.petpet.template.Metadata
import moe.dituon.petpet.uitls.GlobalJson

@Serializable
data class ServerInfo(
    val version: String = BaseService.VERSION,
    @SerialName("api_version")
    val apiVersion: Int = GlobalContext.API_VERSION,
    @SerialName("graphics_api")
    val graphicsApi: String = "awt",
    val templates: List<TemplateInfo>,
) {
    fun toJsonString(): String {
        return GlobalJson.encodeToString(serializer(), this)
    }

    companion object {
        @JvmStatic
        fun fromModelMap(map: Map<String, PetpetModel>) = ServerInfo(
            templates = map.filter { (_, model) ->
                !model.metadata.hidden
            }.map { (id, model) ->
                TemplateInfo(id, model.metadata)
            }
        )
    }
}

@Serializable
data class TemplateInfo(
    val id: String,
    val metadata: Metadata,
    //TODO: Required id
)
