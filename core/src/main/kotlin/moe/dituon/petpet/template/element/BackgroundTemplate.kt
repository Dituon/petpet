package moe.dituon.petpet.template.element

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.internal.FormatLanguage
import moe.dituon.petpet.core.position.AvatarXYWHCoords
import moe.dituon.petpet.template.fields.ColorElement
import moe.dituon.petpet.template.fields.length.AvatarCoordsList
import moe.dituon.petpet.uitls.GlobalJson
import java.awt.Color
import java.io.File
import java.nio.file.Path

private val systemPath = Path.of(System.getProperty("user.dir")).toFile()
val DEFAULT_BACKGROUND_COLOR = Color(0, 0, 0, 0)

@Serializable
//@OptIn(ExperimentalSerializationApi::class)
@SerialName("background")
//@JsonClassDiscriminator("type")
data class BackgroundTemplate(
    // TODO
    val color: ColorElement = DEFAULT_BACKGROUND_COLOR,
    // TODO
    val coords: AvatarCoordsList = listOf(AvatarXYWHCoords.FILL),
    val src: String = "./",
    // TODO
    val images: List<String> = emptyList(),
    val reverse: Boolean = false
) : ElementTemplate() {
    @Transient
    var basePath: File = systemPath

    companion object {
        @OptIn(InternalSerializationApi::class)
        @JvmStatic
        fun fromJson(@FormatLanguage("json", "", "") string: String): BackgroundTemplate {
            return GlobalJson.decodeFromString(string)
        }

        @JvmStatic
        fun fromJsonFile(file: File): BackgroundTemplate {
            val background = fromJson(file.readText())
            background.basePath = file.parentFile
            return background
        }
    }
}
