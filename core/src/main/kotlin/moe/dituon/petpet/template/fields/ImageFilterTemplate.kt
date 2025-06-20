package moe.dituon.petpet.template.fields

import com.jhlabs.image.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import lombok.Builder
import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.filter.*
import moe.dituon.petpet.template.element.AvatarTemplate
import moe.dituon.petpet.uitls.*
import java.awt.image.BufferedImage

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class ImageFilterTemplate {
    abstract val maxLength: Int
    open fun hasAnimation() = maxLength > 1
    open fun filter(image: BufferedImage, i: Int) = filter(image, i, null, null)
    abstract fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?,
    ): BufferedImage

    companion object {
        fun getDefaultDeserializer(type: String?) = when (type?.uppercase()) {
            "SWIRL" -> ImageSwirlFilter.serializer()
            "BULGE" -> ImageBulgeFilter.serializer()
            "SWIM" -> ImageSwimFilter.serializer()
            "BLUR" -> ImageBlurFilter.serializer()
            "CONTRAST" -> ImageContrastFilter.serializer()
            "HSB" -> ImageHSBFilter.serializer()
            "HALFTONE" -> ImageHalftoneFilter.serializer()
            "DOTSCREEN" -> ImageDotScreenFilter.serializer()
            "DOT_SCREEN" -> ImageDotScreenFilter.serializer()
            "NOISE" -> ImageNoiseFilter.serializer()
            "DENOISE" -> ImageDenoiseFilter.serializer()
            "OIL" -> ImageOilFilter.serializer()
            "GRAY" -> ImageGrayFilter.serializer()
            "BINARIZATION" -> ImageBinarizeFilter.serializer()
            "BINARIZE" -> ImageBinarizeFilter.serializer()
            "BINARY" -> ImageBinarizeFilter.serializer()
            "MIRROR" -> ImageMirrorFilter.serializer()
            "FLIP" -> ImageFlipFilter.serializer()
            "MIRAGE" -> ImageMirageFilter.serializer()
            else -> throw IllegalArgumentException("Can not deserialize request with action: $type")
        }
    }
}

object ImageFilterSerializer : KSerializer<ImageFilterTemplate> {
    override val descriptor: SerialDescriptor = ImageFilterTemplate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: ImageFilterTemplate) {
        encoder.encodeSerializableValue(ImageFilterTemplate.serializer(), value)
    }

    override fun deserialize(decoder: Decoder): ImageFilterTemplate =
        when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonObject -> GlobalJson.decodeFromJsonElement(ImageFilterTemplate.serializer(), element)
            is JsonPrimitive -> if (element.isString) {
                GlobalJson.decodeFromJsonElement(
                    ImageFilterTemplate.serializer(),
                    JsonObject(mapOf(Pair("type", element)))
                )
            } else {
                throw IllegalArgumentException("Filter type can oly be object or string: $element")
            }

            else -> throw IllegalArgumentException("Can not deserialize request with action: $element")
        }
}

typealias ImageFilterElement = @Serializable(ImageFilterSerializer::class) ImageFilterTemplate

object ImageFilterListSerializer : KSerializer<ImageFilterList> {
    private val serializer = ListSerializer(ImageFilterSerializer)
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: ImageFilterList) =
        encoder.encodeSerializableValue(serializer, value.filterList)

    override fun deserialize(decoder: Decoder): ImageFilterList =
        ImageFilterList(decoder.decodeSerializableValue(serializer))
}

typealias ImageFilterListElement = @Serializable(ImageFilterListSerializer::class) ImageFilterList

@Serializable
@SerialName("swirl")
@Builder
data class ImageSwirlFilter(
    val radius: FloatOrArray = floatArrayOf(0f),
    val angle: FloatOrArray = floatArrayOf(3f),

    val x: FloatOrArray = floatArrayOf(0.5f),
    val y: FloatOrArray = floatArrayOf(0.5f)
) : ImageFilterTemplate() {
    override val maxLength = maxOf(radius.size, angle.size, x.size, y.size)

    fun toFilter(i: Int) = TwirlFilter().apply {
        radius = this@ImageSwirlFilter.radius[i % this@ImageSwirlFilter.radius.size]
        angle = this@ImageSwirlFilter.angle[i % this@ImageSwirlFilter.angle.size] / 2
        centreX = x[i % x.size]
        centreY = y[i % y.size]
    }

    override fun filter(image: BufferedImage, i: Int) = toFilter(i).filter(image, null)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("bulge")
data class ImageBulgeFilter(
    val radius: FloatOrArray = floatArrayOf(0f),
    val strength: FloatOrArray = floatArrayOf(0.5f),

    val x: FloatOrArray = floatArrayOf(0.5f),
    val y: FloatOrArray = floatArrayOf(0.5f)
) : ImageFilterTemplate() {
    override val maxLength = maxOf(radius.size, strength.size, x.size, y.size)

    fun toFilter(i: Int) = PetpetBulgePinchFilter().apply {
        strength = this@ImageBulgeFilter.strength[i % this@ImageBulgeFilter.strength.size]
        radius = this@ImageBulgeFilter.radius[i % this@ImageBulgeFilter.radius.size]
        centerX = x[i % x.size]
        centerY = y[i % y.size]
    }

    override fun filter(image: BufferedImage, i: Int) = toFilter(i).filter(image)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("swim")
data class ImageSwimFilter(
    val scale: FloatOrArray = floatArrayOf(32f),
    val stretch: FloatOrArray = floatArrayOf(1f),
    val angle: FloatOrArray = floatArrayOf(0f),
    val amount: FloatOrArray = floatArrayOf(10f),
    val turbulence: FloatOrArray = floatArrayOf(1f),
    val time: FloatOrArray = floatArrayOf(0f)
) : ImageFilterTemplate() {
    override val maxLength =
        maxOf(scale.size, stretch.size, angle.size, amount.size, turbulence.size, time.size)

    fun toFilter(i: Int) = SwimFilter().apply {
        scale = this@ImageSwimFilter.scale[i % this@ImageSwimFilter.scale.size]
        stretch = this@ImageSwimFilter.stretch[i % this@ImageSwimFilter.stretch.size]
        angle = this@ImageSwimFilter.angle[i % this@ImageSwimFilter.angle.size]
        amount = this@ImageSwimFilter.amount[i % this@ImageSwimFilter.amount.size]
        turbulence = this@ImageSwimFilter.turbulence[i % this@ImageSwimFilter.turbulence.size]
        time = this@ImageSwimFilter.time[i % this@ImageSwimFilter.time.size]
    }

    override fun filter(image: BufferedImage, i: Int) = toFilter(i).filter(image, null)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("blur")
data class ImageBlurFilter(
    val radius: FloatOrArray = floatArrayOf(10f)
) : ImageFilterTemplate() {
    override val maxLength = radius.size

    fun toFilter(i: Int) = BoxBlurFilter().apply {
        radius = this@ImageBlurFilter.radius[i % this@ImageBlurFilter.radius.size]
    }

    override fun filter(image: BufferedImage, i: Int) = toFilter(i).filter(image, null)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("contrast")
data class ImageContrastFilter(
    val brightness: FloatOrArray = floatArrayOf(0f),
    val contrast: FloatOrArray = floatArrayOf(0f)
) : ImageFilterTemplate() {
    override val maxLength = maxOf(brightness.size, contrast.size)

    fun getFilter(i: Int) = ContrastFilter().apply {
        brightness = this@ImageContrastFilter.brightness[i % this@ImageContrastFilter.brightness.size] + 1f
        contrast = this@ImageContrastFilter.contrast[i % this@ImageContrastFilter.contrast.size] + 1f
    }

    override fun filter(image: BufferedImage, i: Int) = getFilter(i).filter(image, null)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("hsb")
data class ImageHSBFilter(
    val hue: FloatOrArray = floatArrayOf(0f),
    val saturation: FloatOrArray = floatArrayOf(0f),
    val brightness: FloatOrArray = floatArrayOf(0f)
) : ImageFilterTemplate() {
    override val maxLength = maxOf(hue.size, saturation.size, brightness.size)

    fun getFilter(i: Int) = HSBAdjustFilter().apply {
        hFactor = hue[i % hue.size]
        sFactor = saturation[i % saturation.size]
        bFactor = brightness[i % brightness.size]
    }

    override fun filter(image: BufferedImage, i: Int) = getFilter(i).filter(image, null)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("halftone")
data class ImageHalftoneFilter(
    val angle: FloatOrArray = floatArrayOf(0f),
    val radius: FloatOrArray = floatArrayOf(4f),
    val x: FloatOrArray = floatArrayOf(0.5f),
    val y: FloatOrArray = floatArrayOf(0.5f)
) : ImageFilterTemplate() {
    override val maxLength = maxOf(angle.size, radius.size, x.size, y.size)

    fun toFilter(i: Int) = ColorHalftoneFilter().apply {
        setdotRadius(radius[i % radius.size])
        val a = this@ImageHalftoneFilter.angle[i % this@ImageHalftoneFilter.angle.size]
        cyanScreenAngle += a
        magentaScreenAngle += a
        yellowScreenAngle += a
    }

    override fun filter(image: BufferedImage, i: Int) = toFilter(i).filter(image, null)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("dotscreen")
data class ImageDotScreenFilter(
    val angle: FloatOrArray = floatArrayOf(0f),
    val radius: FloatOrArray = floatArrayOf(4f),
    val x: FloatOrArray = floatArrayOf(0.5f),
    val y: FloatOrArray = floatArrayOf(0.5f)
) : ImageFilterTemplate() {
    override val maxLength = maxOf(angle.size, radius.size, x.size, y.size)

    fun toFilter(i: Int) = ColorHalftoneFilter().apply {
        setdotRadius(radius[i % radius.size])
        val a = this@ImageDotScreenFilter.angle[i % this@ImageDotScreenFilter.angle.size]
        cyanScreenAngle = a
        magentaScreenAngle = a
        yellowScreenAngle = a
    }

    override fun filter(image: BufferedImage, i: Int) = PetpetGrayFilter.filter(
        toFilter(i).filter(image, null)
    )!!

    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("noise")
data class ImageNoiseFilter(
    val amount: FloatOrArray = floatArrayOf(0.25f)
) : ImageFilterTemplate() {
    override val maxLength = amount.size

    fun toFilter(i: Int) = NoiseFilter().apply {
        amount = (this@ImageNoiseFilter.amount[i % this@ImageNoiseFilter.amount.size] * 100).toInt()
    }

    override fun filter(image: BufferedImage, i: Int) = toFilter(i).filter(image, null)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("denoise")
data class ImageDenoiseFilter(
    val exponent: FloatOrArray = floatArrayOf(20f)
) : ImageFilterTemplate() {
    override val maxLength = exponent.size

    fun toFilter() = MedianFilter()

    override fun filter(image: BufferedImage, i: Int) = toFilter().filter(image, null)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("oil")
data class ImageOilFilter(
    val skip: FloatOrArray = floatArrayOf(4f),
    val range: FloatOrArray = floatArrayOf(12f),
    val levels: FloatOrArray = floatArrayOf(8f)
) : ImageFilterTemplate() {
    override val maxLength = maxOf(skip.size, range.size, levels.size)

    fun toFilter(i: Int) = PetpetOilFilter().apply {
        skip = this@ImageOilFilter.skip[i % this@ImageOilFilter.skip.size].toInt()
        range = this@ImageOilFilter.range[i % this@ImageOilFilter.range.size].toInt()
        levels = this@ImageOilFilter.levels[i % this@ImageOilFilter.levels.size].toInt()
    }

    override fun filter(image: BufferedImage, i: Int) = toFilter(i).filter(image, null)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("mirage")
data class ImageMirageFilter(
    val key: StringList = listOf("unknown"),
    @JsonNames("src")
    val default: StringList = emptyList(),

    @SerialName("inner_scale")
    val innerScale: FloatOrArray = floatArrayOf(0.3f),
    @SerialName("cover_scale")
    val coverScale: FloatOrArray = floatArrayOf(0.8f),
    @SerialName("inner_desat")
    val innerDesat: FloatOrArray = floatArrayOf(0f),
    @SerialName("cover_desat")
    val coverDesat: FloatOrArray = floatArrayOf(0f),
    val weight: FloatOrArray = floatArrayOf(0.7f),
    @SerialName("max_size")
    val maxSize: IntOrArray = intArrayOf(1200),
    val colored: BooleanOrArray = booleanArrayOf(true)
) : ImageFilterTemplate() {
    override val maxLength = maxOf(
        key.size, default.size,
        innerScale.size, coverScale.size,
        innerDesat.size, coverDesat.size,
        weight.size, maxSize.size, colored.size
    )
    @Transient
    val requestKey: Set<String> = key.toMutableSet().apply {
        remove("unknown")
    }

    fun toFilter(i: Int) = PetpetMirageFilter().apply {
        scaleI = innerScale[i % innerScale.size]
        scaleC = coverScale[i % coverScale.size]
        desatI = innerDesat[i % innerDesat.size]
        desatC = coverDesat[i % coverDesat.size]
        weightI = weight[i % weight.size]
        maxSize = this@ImageMirageFilter.maxSize[i % this@ImageMirageFilter.maxSize.size]
        isColored = colored[i % colored.size]
    }

    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ): BufferedImage {
        assert(parentTemplate != null && requestContext != null)
        val frameList = requestContext!!.getFrameList(
            key[i % key.size],
            if (default.isEmpty()) null else default[i % default.size],
            parentTemplate!!.basePath
        )
        return toFilter(i).filter(image, frameList[i].image)
    }
}

@Serializable
@SerialName("gray")
class ImageGrayFilter : ImageFilterTemplate() {
    override val maxLength = 1

    override fun filter(image: BufferedImage, i: Int) = PetpetGrayFilter.filter(image)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("binarize")
class ImageBinarizeFilter : ImageFilterTemplate() {
    override val maxLength = 1

    override fun filter(image: BufferedImage, i: Int) = PetpetBinarizeFilter.filter(image)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("mirror")
class ImageMirrorFilter : ImageFilterTemplate() {
    override val maxLength = 1

    override fun filter(image: BufferedImage, i: Int) = PetpetMirrorFilter.filter(image)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}

@Serializable
@SerialName("flip")
class ImageFlipFilter : ImageFilterTemplate() {
    override val maxLength = 1

    override fun filter(image: BufferedImage, i: Int) = PetpetFlipFilter.filter(image)!!
    override fun filter(
        image: BufferedImage,
        i: Int,
        parentTemplate: AvatarTemplate?,
        requestContext: RequestContext?
    ) = filter(image, i)
}
