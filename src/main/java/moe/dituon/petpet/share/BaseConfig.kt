package moe.dituon.petpet.share

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import java.awt.image.BufferedImage

val encodeDefaultsPrettyJson = Json {
    encodeDefaults = true
    prettyPrint = true
}

val encodeDefaultsJson = Json { encodeDefaults = true }

interface BaseServiceConfigInterface {
    val antialias: Boolean
    val gifMaxSize: List<Int>
    val gifEncoder: Encoder
    val gifQuality: Int
    val threadPoolSize: Int
    val headless: Boolean

    fun toBaseServiceConfig() = BaseServiceConfig(
        antialias = antialias,
        gifMaxSize = gifMaxSize,
        gifEncoder = gifEncoder,
        gifQuality = gifQuality,
        threadPoolSize = threadPoolSize,
        headless = headless
    )
    fun stringify(): String {
        return encodeDefaultsPrettyJson.encodeToString(this)
    }
}

@Serializable
data class BaseServiceConfig(
    override val antialias: Boolean = true,
    override val gifMaxSize: List<Int> = emptyList(),
    override val gifEncoder: Encoder = Encoder.ANIMATED_LIB,
    override val gifQuality: Int = 100,
    override val threadPoolSize: Int = 0,
    override val headless: Boolean = true
) : BaseServiceConfigInterface

enum class Encoder {
    BUFFERED_STREAM, ANIMATED_LIB, SQUAREUP_LIB
}

enum class Type {
    GIF, IMG
}

@Serializable
data class KeyData(
    val type: Type,
    val avatar: List<AvatarData>,
    val text: List<TextData>,
    var background: BackgroundData? = null,
    var delay: Int? = 65,
    var alias: List<String>? = null,
    val format: String? = "png", //未实装
    val inRandomList: Boolean? = true,
    var reverse: Boolean? = false,
    val hidden: Boolean? = false
) {
    constructor(type: Type) : this(type, ArrayList(), ArrayList())

    companion object {
        @JvmStatic
        fun getData(str: String): KeyData {
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

enum class FitType {
    CONTAIN, COVER, FILL
}

enum class AvatarStyle {
    MIRROR, FLIP, GRAY, BINARIZATION
}

@Serializable
data class AvatarData @JvmOverloads constructor(
    val type: AvatarType,
    var pos: JsonArray = Json.decodeFromString(JsonArray.serializer(), "[0,0,100,100]"),
    var posType: AvatarPosType? = AvatarPosType.ZOOM,
    var crop: JsonArray? = null,
    var cropType: CropType? = CropType.NONE,
    var fit: FitType? = FitType.FILL,
    var style: List<AvatarStyle>? = emptyList(),
    var angle: Short? = 0,
    var opacity: Float? = 1.0F,
    var round: Boolean? = false,
    var rotate: Boolean? = false,
    var avatarOnTop: Boolean? = true,
    val antialias: Boolean? = false
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