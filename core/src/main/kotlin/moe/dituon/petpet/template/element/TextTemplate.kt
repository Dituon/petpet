package moe.dituon.petpet.template.element

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.internal.FormatLanguage
import moe.dituon.petpet.core.clip.BorderRadius
import moe.dituon.petpet.core.element.text.TextModel
import moe.dituon.petpet.core.position.TextXYWCoords
import moe.dituon.petpet.core.transform.Offset
import moe.dituon.petpet.template.fields.ColorList
import moe.dituon.petpet.template.fields.length.BorderRadiusList
import moe.dituon.petpet.template.fields.length.OffsetList
import moe.dituon.petpet.template.fields.length.TextCoordsList
import moe.dituon.petpet.uitls.FloatOrArray
import moe.dituon.petpet.uitls.GlobalJson
import moe.dituon.petpet.uitls.StringList
import moe.dituon.petpet.uitls.wrapJsonElement
import java.awt.Color
import java.awt.Font
import java.io.File

@Serializable
@OptIn(ExperimentalSerializationApi::class)
enum class TextAlign {
    @JsonNames("left")
    LEFT,

    @JsonNames("right")
    RIGHT,

    @JsonNames("center", "middle")
    CENTER
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
enum class TextBaseline {
    @JsonNames("top")
    TOP,

    @JsonNames("middle", "center")
    MIDDLE,

    @JsonNames("alphabetic")
    ALPHABETIC,

    @JsonNames("bottom")
    BOTTOM
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
enum class TextWrap {
    @JsonNames("none")
    NONE,

    @JsonNames("break")
    BREAK,

    @JsonNames("zoom")
    ZOOM
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
enum class TextStyle(
    val value: Int
) {
    @JsonNames("plain")
    PLAIN(Font.PLAIN),
    @JsonNames("bold")
    BOLD(Font.BOLD),
    @JsonNames("italic")
    ITALIC(Font.ITALIC),
    @JsonNames("bold_italic")
    BOLD_ITALIC(Font.BOLD or Font.ITALIC)
}

typealias TextTemplateBuilder = TextTemplate.Builder

const val DEFAULT_TEXT_ID = "text"

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("text")
data class TextTemplate(
    val id: String? = null,
    val text: StringList,
    val coords: TextCoordsList = listOf(TextXYWCoords.DEFAULT),
    val angle: FloatOrArray = floatArrayOf(0f),
    val color: ColorList = listOf(TextModel.DEFAULT_COLOR),
    @SerialName("font")
    val fontName: StringList = listOf(),
    @JsonNames("min_size")
    val size: FloatOrArray = floatArrayOf(16f),
    @JsonNames("max_size")
    val maxSize: FloatOrArray = floatArrayOf(96f),
    val align: TextAlignList = listOf(TextAlign.LEFT),
    val origin: OffsetList = listOf(Offset.EMPTY),
    val baseline: TextBaselineList = listOf(TextBaseline.TOP),
    val wrap: TextWrapList = listOf(TextWrap.NONE),
    val style: TextStyleList = listOf(TextStyle.PLAIN),
    @JsonNames("stroke_color")
    val strokeColor: ColorList = listOf(TextModel.DEFAULT_STROKE_COLOR),
    @JsonNames("stroke_size")
    val strokeSize: FloatOrArray = floatArrayOf(0f),
    // TODO
    val background: ColorList = emptyList(),
    // TODO
    @JsonNames("border_radius")
    val borderRadius: BorderRadiusList = emptyList(),
    val start: Int = 0,
    val end: Int = -1
) : ElementTemplate() {
    @Transient
    val maxLength: Int = maxOf(
        text.size,
        coords.size,
        angle.size,
        color.size,
        fontName.size,
        size.size,
        align.size,
        origin.size,
        baseline.size,
        wrap.size,
        style.size,
        strokeColor.size,
        strokeSize.size,
        background.size,
        borderRadius.size,
    )

    companion object {
        @OptIn(InternalSerializationApi::class)
        @JvmStatic
        fun fromJson(@FormatLanguage("json", "", "") string: String): TextTemplate {
            return GlobalJson.decodeFromString(string)
        }

        @JvmStatic
        fun fromJsonFile(file: File): TextTemplate {
            return fromJson(file.readText())
        }

        @JvmStatic
        fun builder(text: String) = Builder(text)
        @JvmStatic
        fun builder() = Builder()
    }

    constructor(builder: Builder) : this(
        id = builder.id,
        text = builder.text,
        coords = builder.coords,
        angle = builder.angle,
        color = builder.color,
        fontName = builder.fontName,
        size = builder.size,
        align = builder.align,
        origin = builder.origin,
        baseline = builder.baseline,
        wrap = builder.wrap,
        style = builder.style,
        strokeColor = builder.strokeColor,
        strokeSize = builder.strokeSize,
        background = builder.background,
        borderRadius = builder.borderRadius,
        start = builder.start,
        end = builder.end
    )

    class Builder(
        var id: String?,
        var text: List<String>
    ) {
        constructor(): this(null, mutableListOf(""))
        constructor(text: String) : this(null, mutableListOf(text))
        constructor(text: List<String>): this(null, text)

        var coords: List<TextXYWCoords> = mutableListOf(TextXYWCoords.DEFAULT)
            private set
        var angle: FloatArray = floatArrayOf(0f)
            private set
        var color: List<Color> = mutableListOf(TextModel.DEFAULT_COLOR)
            private set
        var fontName: List<String> = mutableListOf()
            private set
        var size: FloatArray = floatArrayOf(16f)
            private set
        var align: List<TextAlign> = mutableListOf(TextAlign.LEFT)
            private set
        var origin: List<Offset> = mutableListOf(Offset.EMPTY)
            private set
        var baseline: List<TextBaseline> = mutableListOf(TextBaseline.TOP)
            private set
        var wrap: List<TextWrap> = mutableListOf(TextWrap.NONE)
            private set
        var style: List<TextStyle> = mutableListOf(TextStyle.PLAIN)
            private set
        var strokeColor: List<Color> = mutableListOf(TextModel.DEFAULT_STROKE_COLOR)
            private set
        var strokeSize: FloatArray = floatArrayOf(0f)
            private set
        var background: List<Color> = emptyList()
            private set
        var borderRadius: List<BorderRadius> = emptyList()
            private set
        var start: Int = 0
            private set
        var end: Int = -1
            private set

        fun id(id: String) = apply { this.id = id }

        fun text(text: String) = apply { this.text = mutableListOf(text) }
        fun text(text: List<String>) = apply { this.text = text }

        fun coords(coords: TextXYWCoords) = apply { this.coords = mutableListOf(coords) }
        fun coords(coords: List<TextXYWCoords>) = apply { this.coords = coords }

        fun angle(angle: Float) = apply { this.angle = floatArrayOf(angle) }
        fun angle(angle: FloatArray) = apply { this.angle = angle }

        fun color(color: Color) = apply { this.color = mutableListOf(color) }
        fun color(color: List<Color>) = apply { this.color = color }

        fun fontName(fontName: String) = apply { this.fontName = mutableListOf(fontName) }
        fun fontName(fontName: List<String>) = apply { this.fontName = fontName }

        fun size(size: Float) = apply { this.size = floatArrayOf(size) }
        fun size(size: FloatArray) = apply { this.size = size }

        fun align(align: TextAlign) = apply { this.align = mutableListOf(align) }
        fun align(align: List<TextAlign>) = apply { this.align = align }

        fun origin(origin: Offset) = apply { this.origin = mutableListOf(origin) }
        fun origin(origin: List<Offset>) = apply { this.origin = origin }

        fun baseline(baseline: TextBaseline) = apply { this.baseline = mutableListOf(baseline) }
        fun baseline(baseline: List<TextBaseline>) = apply { this.baseline = baseline }

        fun wrap(wrap: TextWrap) = apply { this.wrap = mutableListOf(wrap) }
        fun wrap(wrap: List<TextWrap>) = apply { this.wrap = wrap }

        fun style(style: TextStyle) = apply { this.style = mutableListOf(style) }
        fun style(style: List<TextStyle>) = apply { this.style = style }

        fun strokeColor(strokeColor: Color) = apply { this.strokeColor = mutableListOf(strokeColor) }
        fun strokeColor(strokeColor: List<Color>) = apply { this.strokeColor = strokeColor }

        fun strokeSize(strokeSize: Float) = apply { this.strokeSize = floatArrayOf(strokeSize) }
        fun strokeSize(strokeSize: FloatArray) = apply { this.strokeSize = strokeSize }

        fun background(background: Color) = apply { this.background = mutableListOf(background) }
        fun background(background: List<Color>) = apply { this.background = background }

        fun borderRadius(borderRadius: BorderRadius) = apply { this.borderRadius = mutableListOf(borderRadius) }
        fun borderRadius(borderRadius: List<BorderRadius>) = apply { this.borderRadius = borderRadius }

        fun start(start: Int) = apply { this.start = start }
        fun end(end: Int) = apply { this.end = end }

        fun build() = TextTemplate(this)
    }
}


object TextAlignSerializer : JsonTransformingSerializer<List<TextAlign>>(ListSerializer(TextAlign.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}
typealias TextAlignList = @Serializable(TextAlignSerializer::class) List<TextAlign>

object TextBaselineSerializer :
    JsonTransformingSerializer<List<TextBaseline>>(ListSerializer(TextBaseline.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}
typealias TextBaselineList = @Serializable(TextBaselineSerializer::class) List<TextBaseline>

object TextWrapSerializer : JsonTransformingSerializer<List<TextWrap>>(ListSerializer(TextWrap.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}
typealias TextWrapList = @Serializable(TextWrapSerializer::class) List<TextWrap>

object TextStyleSerializer : JsonTransformingSerializer<List<TextStyle>>(ListSerializer(TextStyle.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}
typealias TextStyleList = @Serializable(TextStyleSerializer::class) List<TextStyle>
