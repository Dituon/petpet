package moe.dituon.petpet.template

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.internal.FormatLanguage
import moe.dituon.petpet.template.element.AvatarTemplate
import moe.dituon.petpet.template.element.BackgroundTemplate
import moe.dituon.petpet.template.element.ElementTemplate
import moe.dituon.petpet.uitls.GlobalJson
import moe.dituon.petpet.uitls.IntOrArray
import java.io.File
import java.nio.file.Path
import kotlin.math.round

private val systemPath = Path.of(System.getProperty("user.dir")).toFile()

@Serializable
@OptIn(ExperimentalSerializationApi::class)
enum class TemplateType(val type: String) {
    @JsonNames("image")
    IMAGE("image"),

    @JsonNames("gif")
    GIF("gif")
}

private const val DEFAULT_DELAY = 65

typealias PetpetTemplateBuilder = PetpetTemplate.Builder

@Serializable
data class PetpetTemplate(
    val type: TemplateType = TemplateType.IMAGE,
    val metadata: Metadata = DEFAULT_METADATA,
    val elements: List<ElementTemplate> = emptyList(),
    val canvas: TemplateCanvas = TemplateCanvas(),
    val delay: IntOrArray = intArrayOf(-1),
    val fps: Float? = null,
) {
    init {
        if (delay[0] == -1) { // set default
            if (fps != null) {
                delay[0] = round(1000 / fps).toInt()
            } else {
                delay[0] = DEFAULT_DELAY
            }
        }
        for (d in delay) { // check range
            require(d in 1..65535) {
                throw IllegalArgumentException("delay must be between 0 and 65535")
            }
        }
    }

    private constructor(builder: Builder) : this(
        type = builder.type,
        metadata = builder.metadata,
        elements = builder.elements,
        canvas = builder.canvas,
        delay = builder.delay,
        fps = builder.fps
    )

    @Transient
    var basePath: File = systemPath
        set(value) {
            // java side is nullable
            if (value == null) {
                return
            }
            for (element in elements) when (element) {
                is AvatarTemplate -> element.basePath = value
                is BackgroundTemplate -> element.basePath = value
                else -> {}
            }
            field = value
        }

    companion object {
        @OptIn(InternalSerializationApi::class)
        @JvmStatic
        fun fromJson(@FormatLanguage("json", "", "") string: String) =
            GlobalJson.decodeFromString(serializer(), string)

        @JvmStatic
        fun fromJsonElement(element: JsonElement) =
            GlobalJson.decodeFromJsonElement(serializer(), element)

        @JvmStatic
        fun fromJsonFile(file: File): PetpetTemplate {
            val template = fromJson(file.readText())
            template.basePath = file.parentFile
            return template
        }

        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        var type: TemplateType = TemplateType.IMAGE
            private set
        var metadata: Metadata = DEFAULT_METADATA
            private set
        var elements: MutableList<ElementTemplate> = mutableListOf()
            private set
        var canvas: TemplateCanvas = TemplateCanvas()
            private set
        var delay: IntOrArray = intArrayOf(-1)
            private set
        var fps: Float? = null
            private set

        fun type(type: TemplateType) = apply { this.type = type }
        fun metadata(metadata: Metadata) = apply { this.metadata = metadata }
        fun element(element: ElementTemplate) = addElement(element)
        fun elements(elements: List<ElementTemplate>) = apply { this.elements = elements.toMutableList() }
        fun addElement(element: ElementTemplate) = apply { this.elements.add(element) }
        fun canvas(canvas: TemplateCanvas) = apply { this.canvas = canvas }
        fun delay(delay: IntOrArray) = apply { this.delay = delay }
        fun fps(fps: Float?) = apply { this.fps = fps }

        fun build() = PetpetTemplate(this)
    }
}
