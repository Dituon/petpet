package moe.dituon.petpet.httpserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.dituon.petpet.core.GlobalContext
import moe.dituon.petpet.core.element.PetpetModel
import moe.dituon.petpet.core.element.PetpetTemplateModel
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
    val templates: List<TemplateInfo> = emptyList(),
) {
    fun toJsonString(): String {
        return GlobalJson.encodeToString(serializer(), this)
    }

    companion object {
        @JvmStatic
        fun fromService(service: BaseService) = ServerInfo(
            templates = service.staticModelMap.filter { (_, model) ->
                !model.metadata.hidden
            }.map { (id, model) ->
                TemplateInfo.fromModel(id, model, service)
            }
        )
    }
}

@Serializable
data class TemplateInfo(
    val id: String,
    val metadata: Metadata,
    @SerialName("required_image")
    val requiredImage: Set<String>? = null,
    @SerialName("required_text")
    val requiredText: Set<String>? = null,
    @SerialName("image_expected_size")
    val imageExpectedSize: Map<String, Int>? = null,
) {
    companion object {
        @JvmStatic
        fun fromModel(id: String, model: PetpetModel, service: BaseService) = if (model is PetpetTemplateModel) TemplateInfo(
            id = id,
            metadata = model.metadata,
            requiredImage = model.requestImageKeys,
            requiredText = model.requestTextKeys,
            imageExpectedSize = service.getTemplateExpectedSize(model)
        ) else TemplateInfo(
            id = id,
            metadata = model.metadata
        )
    }
}
