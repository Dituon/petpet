package moe.dituon.petpet.template

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import lombok.Builder
import moe.dituon.petpet.core.GlobalContext
import moe.dituon.petpet.uitls.GlobalJson

val DEFAULT_METADATA = Metadata()

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@Builder
data class Metadata(
    @JsonNames("api_version")
    val apiVersion: Int = GlobalContext.API_VERSION,
    @JsonNames("template_version")
    val templateVersion: Int = 0,
    val alias: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val author: String = "",
    val desc: String = "",
    val hidden: Boolean = false,
    @JsonNames("in_random_list")
    val inRandomList: Boolean = true,
    @JsonNames("default_template_weight")
    val defaultTemplateWeight: Int = 0,
    val preview: String? = null
) {
    companion object {
        @JvmStatic
        fun fromJsonElement(element: JsonElement) = GlobalJson.decodeFromJsonElement(serializer(), element)
    }
}