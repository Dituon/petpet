package moe.dituon.petpet.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import moe.dituon.petpet.template.Metadata
import java.net.URL

@Serializable
data class UpdateIndex(
    val templatesPath: String = "./templates",
    val templates: Map<String, UpdateIndexTemplateElement> = emptyMap(),
    val fontsPath: String = "./templates/fonts",
    val fonts: Map<String, UpdateIndexFontElement> = emptyMap(),
)

enum class UpdateIndexTemplateElementType {
    @SerialName("template")
    TEMPLATE,

    @SerialName("script")
    SCRIPT,
}

@Serializable
data class UpdateIndexTemplateElement(
    val type: UpdateIndexTemplateElementType,
    val metadata: Metadata?,
    val resource: Map<String, String> = emptyMap(),
    val dependentFonts: Set<String> = emptySet(),
    val size: Int = 0,
    @Transient
    var source: URL? = null,
)

@Serializable
data class UpdateIndexFontElement(
    val md5: String,
    val size: Int = 0,
    val name: Set<String> = emptySet(),
    @Transient
    var source: URL? = null,
)

