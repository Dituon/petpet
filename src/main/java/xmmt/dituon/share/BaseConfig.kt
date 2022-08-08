package xmmt.dituon.share

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import java.awt.image.BufferedImage

@Serializable
data class BaseServiceConfig(
    val antialias: Boolean = true
)

enum class Type {
    GIF, IMG
}

@Serializable
data class KeyData(
    val type: Type,
    val avatar: List<AvatarData>,
    val text: List<TextData>,
    val background: BackgroundData? = null,
    val alias: List<String>? = null,
    val format: String? = "png", //未实装
    val inRandomList: Boolean? = true,
    val hidden: Boolean? = false
) {
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

enum class TextStyle{
    PLAIN, BOLD, ITALIC
}

@Serializable
data class TextData @JvmOverloads constructor(
    val text: String,
    val pos: List<Int>? = null,
    val color: JsonElement? = null,
    val font: String? = null,
    val size: Int? = null,
    val align: TextAlign? = TextAlign.LEFT,
    val wrap: TextWrap? = TextWrap.NONE,
    val style: TextStyle? = TextStyle.PLAIN
)

@Serializable
data class TextExtraData(
    val fromReplacement: String,
    val toReplacement: String,
    val groupReplacement: String,
    val textList: List<String>
)

enum class AvatarType {
    FROM, TO, GROUP, BOT
}

enum class AvatarPosType {
    ZOOM, DEFORM
}

enum class CropType {
    NONE, PIXEL, PERCENT
}

enum class Style {
    MIRROR, FLIP, GRAY, BINARIZATION
}

@Serializable
data class AvatarData @JvmOverloads constructor(
    val type: AvatarType,
    val pos: JsonArray? = null,
    val posType: AvatarPosType? = AvatarPosType.ZOOM,
    val crop: JsonArray? = null,
    val cropType: CropType? = CropType.NONE,
    val style: List<Style>? = emptyList(),
    val angle: Int? = 0,
    val round: Boolean? = false,
    val rotate: Boolean? = false,
    val avatarOnTop: Boolean? = true,
    val antialias: Boolean? = false
)

@Deprecated("使用GifAvatarExtraDataProvider以保证对GIF格式的解析")
data class AvatarExtraDataProvider(
    val fromAvatar: (() -> BufferedImage)? = null,
    val toAvatar: (() -> BufferedImage)? = null,
    val groupAvatar: (() -> BufferedImage)? = null,
    val botAvatar: (() -> BufferedImage)? = null
)

data class GifAvatarExtraDataProvider(
    val fromAvatar: (() -> List<BufferedImage>)? = null,
    val toAvatar: (() -> List<BufferedImage>)? = null,
    val groupAvatar: (() -> List<BufferedImage>)? = null,
    val botAvatar: (() -> List<BufferedImage>)? = null
)

@Serializable
data class BackgroundData @JvmOverloads constructor(
    val size: JsonArray,
    val color: JsonElement? = null
)