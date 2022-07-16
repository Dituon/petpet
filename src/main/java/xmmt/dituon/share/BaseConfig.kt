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
    val alias: List<String>? = null,
    val format: String? = "png",
    val inRandomList: Boolean? = true
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

@Serializable
data class TextData @JvmOverloads constructor(
    val text: String,
    val pos: List<Int>? = null,
    val color: JsonElement? = null,
    val font: String? = null,
    val size: Int? = null,
    val align: TextAlign? = TextAlign.LEFT,
    val wrap: TextWrap? = TextWrap.NONE
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

enum class PosType {
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
    val posType: PosType? = PosType.ZOOM,
    val crop: List<Int>? = null,
    val cropType: CropType? = CropType.NONE,
    val style: List<Style>? = emptyList(),
    val angle: Int? = 0,
    val round: Boolean? = false,
    val rotate: Boolean? = false,
    val avatarOnTop: Boolean? = true,
    val antialias: Boolean? = false
)

data class AvatarExtraDataProvider(
    val fromAvatar: (() -> BufferedImage)? = null,
    val toAvatar: (() -> BufferedImage)? = null,
    val groupAvatar: (() -> BufferedImage)? = null,
    val botAvatar: (() -> BufferedImage)? = null
)