package moe.dituon.petpet.share

import kotlinx.serialization.*
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.json.*
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage

val encodeDefaultsPrettyJson = Json {
    encodeDefaults = true
    prettyPrint = true
}

val encodeDefaultsJson = Json { encodeDefaults = true }

val encodeDefaultsIgnoreUnknownKeysJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

//@Serializable
//abstract class AbstractBaseServiceConfig {
//    abstract val antialias: Boolean
//    abstract val gifMaxSize: List<Int>
//    abstract val gifEncoder: Encoder
//    abstract val gifQuality: Int
//    abstract val threadPoolSize: Int
//    abstract val headless: Boolean
//}

@Serializable
data class BaseServiceConfig(
    val antialias: Boolean = true,
    val resampling: Boolean = true,
//    val serviceThreadPoolSize: Int = 0,
    val gifMaxSize: List<Int> = emptyList(),
    val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    val gifQuality: Int = 5,
    val threadPoolSize: Int = 0,
    val headless: Boolean = true
) {
    fun stringify(): String {
        return encodeDefaultsPrettyJson.encodeToString(this)
    }

    companion object {
        @JvmStatic
        fun parse(str: String): BaseServiceConfig {
            return Json.decodeFromString(str)
        }
    }
}

enum class Encoder {
    BUFFERED_STREAM, ANIMATED_LIB
}

enum class Type {
    GIF, IMG
}

@Serializable
data class TemplateDTO(
    val type: Type,
    val avatar: List<AvatarData> = emptyList(),
    val text: List<TextData> = emptyList(),
    var background: BackgroundData? = null,
    var delay: Int? = 65,
    var alias: List<String>? = null,
    val format: String? = "png", //未实装
    val inRandomList: Boolean? = true,
    var reverse: Boolean? = false,
    val hidden: Boolean? = false
) {
    constructor(type: Type) : this(type, emptyList(), emptyList())
    private var useRandomList:Boolean? = null
    fun isUseRandomList():Boolean{
        if (useRandomList == null) {
            useRandomList = avatar.any { a -> a.type == AvatarType.RANDOM }
        }
        return useRandomList as Boolean
    }

    companion object {
        @JvmStatic
        fun getData(str: String): TemplateDTO {
            return Json.decodeFromString(str)
        }
    }
}

enum class TextAlign {
    LEFT, RIGHT, CENTER
}

enum class TextWrap {
    NONE, BREAK, ZOOM
}

enum class TextStyle(val value: Int) {
    PLAIN(Font.PLAIN),
    BOLD(Font.BOLD),
    ITALIC(Font.ITALIC),
    BOLD_ITALIC(BOLD.value or ITALIC.value)
}

enum class Position {
    LEFT, RIGHT, TOP, BOTTOM, CENTER
}

@Serializable
data class TextData @JvmOverloads constructor(
    var text: String,
    var pos: IntArray = intArrayOf(50, 50),
    var angle: Short = 0,
    var color: String = TextModel.DEFAULT_COLOR_STR,
    var font: String = TextModel.DEFAULT_FONT,
    var size: Int = 16,
    var align: TextAlign = TextAlign.LEFT,
    var wrap: TextWrap = TextWrap.NONE,
    var style: TextStyle = TextStyle.PLAIN,
    var position: List<Position>? = listOf(Position.LEFT, Position.TOP),
    var origin: TransformOrigin = TransformOrigin.DEFAULT,
    var strokeColor: String = TextModel.DEFAULT_STROKE_COLOR_STR,
    var strokeSize: Short = 0,
    var greedy: Boolean = false
) {
    fun getAwtColor() : Color{
        if (color == TextModel.DEFAULT_COLOR_STR) return TextModel.DEFAULT_COLOR
        return decodeColor(color)
    }
    fun getStrokeAwtColor() : Color{
        if (strokeColor == TextModel.DEFAULT_STROKE_COLOR_STR) return TextModel.DEFAULT_STROKE_COLOR
        return decodeColor(strokeColor)
    }
}

fun decodeColor(nm: String): Color {
    return if (nm.length <= 7) {
        Color.decode(nm)
    } else {
        val i = java.lang.Long.decode(nm)
        Color(
            ((i shr 24) and 0xFF).toInt(),
            ((i shr 16) and 0xFF).toInt(),
            ((i shr 8) and 0xFF).toInt(),
            (i and 0xFF).toInt()
        )
    }
}

@Serializable
data class TextExtraData(
    val fromReplacement: String,
    val toReplacement: String,
    val groupReplacement: String,
    val textList: List<String>
)

enum class AvatarType {
    FROM, TO, GROUP, BOT, RANDOM
}

enum class AvatarPosType {
    ZOOM, DEFORM
}

enum class CropType {
    NONE, PIXEL, PERCENT
}

enum class TransformOrigin {
    DEFAULT, CENTER
}

enum class FitType {
    CONTAIN, COVER, FILL
}

enum class AvatarStyle {
    MIRROR, FLIP, GRAY, BINARIZATION
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class AvatarFilter {
    abstract fun hasAnimation(): Boolean
    abstract val maxLength: Int
}

object FloatArraySerializer : JsonTransformingSerializer<FloatArray>(FloatArraySerializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element !is JsonArray) JsonArray(listOf(element)) else element
}

@Serializable
@SerialName("SWIRL")
data class AvatarSwirlFilter(
    @Serializable(with = FloatArraySerializer::class)
    val radius: FloatArray = floatArrayOf(0f),
    @Serializable(with = FloatArraySerializer::class)
    val angle: FloatArray = floatArrayOf(3f),

    @Serializable(with = FloatArraySerializer::class)
    val x: FloatArray = floatArrayOf(0.5f),
    @Serializable(with = FloatArraySerializer::class)
    val y: FloatArray = floatArrayOf(0.5f)
): AvatarFilter() {
    override val maxLength = intArrayOf(radius.size, angle.size, x.size, y.size).maxOrNull() ?: 1
    override fun hasAnimation() = maxLength > 1
}

@Serializable
@SerialName("BULGE")
data class AvatarBulgeFilter(
    @Serializable(with = FloatArraySerializer::class)
    val radius: FloatArray = floatArrayOf(0f),
    @Serializable(with = FloatArraySerializer::class)
    val strength: FloatArray = floatArrayOf(0.5f),

    @Serializable(with = FloatArraySerializer::class)
    val x: FloatArray = floatArrayOf(0.5f),
    @Serializable(with = FloatArraySerializer::class)
    val y: FloatArray = floatArrayOf(0.5f)
) : AvatarFilter() {
    override val maxLength = arrayOf(radius.size, strength.size, x.size, y.size).maxOrNull() ?: 1
    override fun hasAnimation() = maxLength > 1
}

@Serializable
@SerialName("SWIM")
data class AvatarSwimFilter(
    @Serializable(with = FloatArraySerializer::class)
    val scale: FloatArray = floatArrayOf(32f),
    @Serializable(with = FloatArraySerializer::class)
    val stretch: FloatArray = floatArrayOf(1f),
    @Serializable(with = FloatArraySerializer::class)
    val angle: FloatArray = floatArrayOf(0f),
    @Serializable(with = FloatArraySerializer::class)
    val amount: FloatArray = floatArrayOf(10f),
    @Serializable(with = FloatArraySerializer::class)
    val turbulence: FloatArray = floatArrayOf(1f),
    @Serializable(with = FloatArraySerializer::class)
    val time: FloatArray = floatArrayOf(0f)
) : AvatarFilter() {
    override val maxLength = arrayOf(scale.size, stretch.size, angle.size, amount.size, turbulence.size, time.size).maxOrNull() ?: 1
    override fun hasAnimation() = maxLength > 1
}

@Serializable
@SerialName("BLUR")
data class AvatarBlurFilter(
    @Serializable(with = FloatArraySerializer::class)
    val radius: FloatArray = floatArrayOf(10f)
) : AvatarFilter() {
    override val maxLength = radius.size
    override fun hasAnimation() = maxLength > 1
}

@Serializable
@SerialName("CONTRAST")
data class AvatarContrastFilter(
    @Serializable(with = FloatArraySerializer::class)
    val brightness: FloatArray = floatArrayOf(0f),
    @Serializable(with = FloatArraySerializer::class)
    val contrast: FloatArray = floatArrayOf(0f)
) : AvatarFilter() {
    override val maxLength = arrayOf(brightness.size, contrast.size).maxOrNull() ?: 1
    override fun hasAnimation() = maxLength > 1
}

@Serializable
@SerialName("HSB")
data class AvatarHSBFilter(
    @Serializable(with = FloatArraySerializer::class)
    val hue: FloatArray = floatArrayOf(0f),
    @Serializable(with = FloatArraySerializer::class)
    val saturation: FloatArray = floatArrayOf(0f),
    @Serializable(with = FloatArraySerializer::class)
    val brightness: FloatArray = floatArrayOf(0f)
) : AvatarFilter() {
    override val maxLength = arrayOf(hue.size, saturation.size, brightness.size).maxOrNull() ?: 1
    override fun hasAnimation() = maxLength > 1
}

@Serializable
@SerialName("HALFTONE")
data class AvatarHalftoneFilter(
    @Serializable(with = FloatArraySerializer::class)
    val angle: FloatArray = floatArrayOf(0f),
    @Serializable(with = FloatArraySerializer::class)
    val radius: FloatArray = floatArrayOf(4f),
    @Serializable(with = FloatArraySerializer::class)
    val x: FloatArray = floatArrayOf(0.5f),
    @Serializable(with = FloatArraySerializer::class)
    val y: FloatArray = floatArrayOf(0.5f)
) : AvatarFilter() {
    override val maxLength = arrayOf(angle.size, radius.size, x.size, y.size).maxOrNull() ?: 1
    override fun hasAnimation() = maxLength > 1
}

@Serializable
@SerialName("DOT_SCREEN")
data class AvatarDotScreenFilter(
    @Serializable(with = FloatArraySerializer::class)
    val angle: FloatArray = floatArrayOf(0f),
    @Serializable(with = FloatArraySerializer::class)
    val radius: FloatArray = floatArrayOf(4f),
    @Serializable(with = FloatArraySerializer::class)
    val x: FloatArray = floatArrayOf(0.5f),
    @Serializable(with = FloatArraySerializer::class)
    val y: FloatArray = floatArrayOf(0.5f)
) : AvatarFilter() {
    override val maxLength = arrayOf(angle.size, radius.size, x.size, y.size).maxOrNull() ?: 1
    override fun hasAnimation() = maxLength > 1
}

@Serializable
@SerialName("NOISE")
data class AvatarNoiseFilter(
    @Serializable(with = FloatArraySerializer::class)
    val amount: FloatArray = floatArrayOf(0.25f)
) : AvatarFilter() {
    override val maxLength = amount.size
    override fun hasAnimation() = maxLength > 1
}

@Serializable
@SerialName("DENOISE")
data class AvatarDenoiseFilter(
    @Serializable(with = FloatArraySerializer::class)
    val exponent: FloatArray = floatArrayOf(20f)
) : AvatarFilter() {
    override val maxLength = exponent.size
    override fun hasAnimation() = maxLength > 1
}

@Serializable
data class AvatarData @JvmOverloads constructor(
    val type: AvatarType,
    var pos: JsonArray = Json.decodeFromString(JsonArray.serializer(), "[0,0,100,100]"),
    var posType: AvatarPosType = AvatarPosType.ZOOM,
    var crop: JsonArray? = null,
    var cropType: CropType = CropType.NONE,
    var fit: FitType = FitType.FILL,
    var style: List<AvatarStyle> = emptyList(),
    var filter: List<AvatarFilter> = emptyList(),
    var angle: Short = 0,
    var origin: TransformOrigin = TransformOrigin.DEFAULT,
    var opacity: Float = 1.0F,
    var round: Boolean = false,
    var rotate: Boolean = false,
    var avatarOnTop: Boolean = true,
    var antialias: Boolean? = null,
    var resampling: Boolean? = null
)

@Deprecated("使用GifAvatarExtraDataProvider以保证对GIF格式的解析")
data class AvatarExtraDataProvider(
    val fromAvatar: (() -> BufferedImage)? = null,
    val toAvatar: (() -> BufferedImage)? = null,
    val groupAvatar: (() -> BufferedImage)? = null,
    val botAvatar: (() -> BufferedImage)? = null,
    val randomAvatar: (() -> BufferedImage)? = null
)

data class GifAvatarExtraDataProvider(
    val fromAvatar: (() -> List<BufferedImage>)? = null,
    val toAvatar: (() -> List<BufferedImage>)? = null,
    val groupAvatar: (() -> List<BufferedImage>)? = null,
    val botAvatar: (() -> List<BufferedImage>)? = null,
    val randomAvatar: (() -> List<BufferedImage>)? = null
)

@Serializable
data class BackgroundData @JvmOverloads constructor(
    var size: JsonArray,
    var color: String = BackgroundModel.DEFAULT_COLOR_STR,
    var length: Short = 0
){
    fun getAwtColor(): Color{
        if (color == BackgroundModel.DEFAULT_COLOR_STR) return BackgroundModel.DEFAULT_COLOR
        return decodeColor(color)
    }
}

data class GifRenderParams(
    val encoder: Encoder,
    val delay: Int = 65,
    val maxSize: List<Int>?,
    val antialias: Boolean,
    val quality: Int = 5,
    val reverse: Boolean = false
)