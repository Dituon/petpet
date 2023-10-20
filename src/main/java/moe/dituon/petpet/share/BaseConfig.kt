package moe.dituon.petpet.share

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
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

enum class TextStyle {
    PLAIN, BOLD, ITALIC
}

enum class Position {
    LEFT, RIGHT, TOP, BOTTOM, CENTER
}

@Serializable
data class TextData @JvmOverloads constructor(
    var text: String,
    var pos: List<Int>? = null,
    var color: JsonElement? = null,
    var font: String? = null,
    var size: Int? = null,
    var align: TextAlign? = TextAlign.LEFT,
    var wrap: TextWrap? = TextWrap.NONE,
    var style: TextStyle? = TextStyle.PLAIN,
    var position: List<Position>? = listOf(Position.LEFT, Position.TOP),
    var strokeColor: JsonElement? = null,
    var strokeSize: Short? = null,
    var greedy: Boolean? = false
)

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

enum class AvatarTransformOrigin {
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
sealed class AvatarFilter

@Serializable
@SerialName("SWIRL")
data class AvatarSwirlFilter(
    val radius: Float = 0f,
    val angle: Float = 3f,

    val x: Float = 0.5f,
    val y: Float = 0.5f
): AvatarFilter()

@Serializable
@SerialName("BULGE")
data class AvatarBulgeFilter(
    val radius: Float = 0f,
    val strength: Float = 0.5f,

    val x: Float = 0.5f,
    val y: Float = 0.5f
): AvatarFilter()

@Serializable
@SerialName("BLUR")
data class AvatarBlurFilter(
    val radius: Float = 10f
): AvatarFilter()

@Serializable
@SerialName("CONTRAST")
data class AvatarContrastFilter(
    val brightness: Float = 0f,
    val contrast: Float = 0f
): AvatarFilter()

@Serializable
@SerialName("HSB")
data class AvatarHSBFilter(
    val hue: Float = 0f,
    val saturation: Float = 0f,
    val brightness: Float = 0f
): AvatarFilter()

@Serializable
@SerialName("HALFTONE")
data class AvatarHalftoneFilter(
    val angle: Float = 0f,
    val radius: Float = 4f,

    val x: Float = 0.5f,
    val y: Float = 0.5f
): AvatarFilter()

@Serializable
@SerialName("DOT_SCREEN")
data class AvatarDotScreenFilter(
    val angle: Float = 0f,
    val radius: Float = 4f,

    val x: Float = 0.5f,
    val y: Float = 0.5f
): AvatarFilter()

@Serializable
@SerialName("NOISE")
data class AvatarNoiseFilter(
    val amount: Float = 0.25f
): AvatarFilter()

@Serializable
@SerialName("DENOISE")
data class AvatarDenoiseFilter(
    val exponent: Short = 20
): AvatarFilter()

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
    var origin: AvatarTransformOrigin = AvatarTransformOrigin.DEFAULT,
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
    var color: JsonElement? = null
)

data class GifRenderParams(
    val encoder: Encoder,
    val delay: Int = 65,
    val maxSize: List<Int>?,
    val antialias: Boolean,
    val quality: Int = 5,
    val reverse: Boolean = false
)