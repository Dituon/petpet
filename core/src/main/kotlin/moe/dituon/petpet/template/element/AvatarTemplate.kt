package moe.dituon.petpet.template.element

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.internal.FormatLanguage
import moe.dituon.petpet.core.clip.BorderRadius
import moe.dituon.petpet.core.filter.ImageFilterList
import moe.dituon.petpet.core.length.LengthType
import moe.dituon.petpet.core.length.NumberLength
import moe.dituon.petpet.core.position.AvatarCoords
import moe.dituon.petpet.core.position.AvatarXYWHCoords
import moe.dituon.petpet.core.position.FitType
import moe.dituon.petpet.core.transform.Offset
import moe.dituon.petpet.core.utils.image.ImageBorder
import moe.dituon.petpet.core.utils.image.ImageCropper
import moe.dituon.petpet.core.utils.image.ImageXYWHCropper
import moe.dituon.petpet.template.fields.BorderList
import moe.dituon.petpet.template.fields.ImageFilterListElement
import moe.dituon.petpet.template.fields.ImageFilterTemplate
import moe.dituon.petpet.template.fields.length.*
import moe.dituon.petpet.template.fields.transition.RotateTransitionElement
import moe.dituon.petpet.uitls.FloatOrArray
import moe.dituon.petpet.uitls.GlobalJson
import moe.dituon.petpet.uitls.IntAsStringList
import moe.dituon.petpet.uitls.StringList
import java.io.File
import java.nio.file.Path

private val systemPath = Path.of(System.getProperty("user.dir")).toFile()
private val defaultCoords = AvatarXYWHCoords(
    listOf(
        NumberLength(0f, LengthType.PX),
        NumberLength(0f, LengthType.PX),
        NumberLength(100f, LengthType.PX),
        NumberLength(100f, LengthType.PX)
    )
)

typealias AvatarTemplateBuilder = AvatarTemplate.Builder

const val DEFAULT_AVATAR_ID = "image"

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("image")
data class AvatarTemplate(
    val id: String? = null,
    val key: IntAsStringList = listOf("unknown"),
    @JsonNames("src")
    val default: StringList = emptyList(),
    val coords: AvatarCoordsList,
    val crop: AvatarCropperList = listOf(ImageCropper.EmptyImageCropper.INSTANCE),
    val fit: FitTypeList = listOf(FitType.FILL),
    val position: OffsetList = listOf(Offset.CENTER),
    val angle: FloatOrArray = floatArrayOf(0f),
    val rotate: RotateTransitionElement? = null,
    val origin: OffsetList = listOf(Offset.CENTER),
    val opacity: FloatOrArray = floatArrayOf(1f),
    val border: BorderList = listOf(ImageBorder.EmptyImageBorder.INSTANCE),
    @JsonNames("border_radius")
    val borderRadius: BorderRadiusList = emptyList(),
    val filter: ImageFilterListElement = ImageFilterList.EMPTY,
) : ElementTemplate() {
    @Transient
    var basePath: File = systemPath

    @Transient
    val maxLength: Int = maxOf(
        key.size,
        default.size,
        coords.size,
        crop.size,
        fit.size,
        position.size,
        angle.size,
        origin.size,
        opacity.size,
        border.size,
        borderRadius.size,
        if (filter.isEmpty()) 0 else filter.maxOf { it.maxLength }
    )

    private constructor(builder: Builder) : this(
        id = builder.id,
        key = builder.key,
        default = builder.default,
        coords = builder.coords,
        crop = builder.crop,
        fit = builder.fit,
        position = builder.position,
        angle = builder.angle,
        origin = builder.origin,
        opacity = builder.opacity,
        border = builder.border,
        borderRadius = builder.borderRadius,
        filter = builder.filter,
    )

    companion object {
        @OptIn(InternalSerializationApi::class)
        @JvmStatic
        fun fromJson(@FormatLanguage("json", "", "") string: String): AvatarTemplate {
            return GlobalJson.decodeFromString(string)
        }

        @JvmStatic
        fun fromJsonFile(file: File): AvatarTemplate {
            val avatar = fromJson(file.readText())
            avatar.basePath = file.parentFile
            return avatar
        }

        @JvmStatic
        fun builder(id: String) = Builder(id)

        @JvmStatic
        fun builder() = Builder()
    }

    class Builder(
        var id: String?
    ) {
        constructor() : this(null)

        var key: List<String> = listOf("unknown")
            private set
        var coords: List<AvatarCoords> = listOf(defaultCoords)
            private set
        var crop: List<ImageCropper> = listOf(
            ImageCropper.EmptyImageCropper.INSTANCE
        )
            private set
        var default: List<String> = emptyList()
            private set
        var fit: List<FitType> = listOf(FitType.FILL)
            private set
        var position: List<Offset> = listOf(Offset.EMPTY)
            private set
        var angle: FloatArray = floatArrayOf(0f)
            private set
        var origin: List<Offset> = listOf(Offset.EMPTY)
            private set
        var opacity: FloatArray = floatArrayOf(1f)
            private set
        var border: List<ImageBorder> = listOf(ImageBorder.EmptyImageBorder.INSTANCE)
            private set
        var borderRadius: List<BorderRadius> = emptyList()
            private set
        var filter: ImageFilterList = ImageFilterList.EMPTY
            private set

        fun id(id: String) = apply { this.id = id }

        fun key(key: String) = apply { this.key = listOf(key) }
        fun key(key: List<String>) = apply { this.key = key }

        fun coords(coords: AvatarCoords) = apply { this.coords = listOf(coords) }
        fun coords(coords: List<AvatarCoords>) = apply { this.coords = coords }

        fun crop(crop: ImageXYWHCropper) = apply { this.crop = listOf(crop) }
        fun crop(crop: List<ImageXYWHCropper>) = apply { this.crop = crop }

        fun default(default: String) = apply { this.default = listOf(default) }
        fun default(default: List<String>) = apply { this.default = default }

        fun src(src: String) = apply { this.default = listOf(src) }
        fun src(src: List<String>) = apply { this.default = default }

        fun fit(fit: FitType) = apply { this.fit = listOf(fit) }
        fun fit(fit: List<FitType>) = apply { this.fit = fit }

        fun position(position: Offset) = apply { this.position = listOf(position) }
        fun position(position: List<Offset>) = apply { this.position = position }

        fun angle(angle: Float) = apply { this.angle = floatArrayOf(angle) }
        fun angle(angle: FloatArray) = apply { this.angle = angle }

        fun origin(origin: Offset) = apply { this.origin = listOf(origin) }
        fun origin(origin: List<Offset>) = apply { this.origin = origin }

        fun opacity(opacity: Float) = apply { this.opacity = floatArrayOf(opacity) }
        fun opacity(opacity: FloatArray) = apply { this.opacity = opacity }

        fun border(border: ImageBorder) = apply { this.border = listOf(border) }
        fun border(border: List<ImageBorder>) = apply { this.border = border }

        fun borderRadius(borderRadius: BorderRadius) = apply { this.borderRadius = listOf(borderRadius) }
        fun borderRadius(borderRadius: List<BorderRadius>) = apply { this.borderRadius = borderRadius }

        fun filter(filter: ImageFilterTemplate) = apply {
            this.filter =
                ImageFilterList(listOf(filter))
        }

        fun filter(filter: List<ImageFilterTemplate>) = apply {
            this.filter =
                ImageFilterList(filter)
        }

        fun filter(filter: ImageFilterList) = apply { this.filter = filter }

        fun build() = AvatarTemplate(this)
    }
}