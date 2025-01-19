package moe.dituon.petpet.old_template

import kotlinx.serialization.Serializable
import moe.dituon.petpet.core.length.DynamicLength
import moe.dituon.petpet.core.length.NumberLength
import moe.dituon.petpet.core.transform.OffsetValue
import java.nio.file.Path

val systemPath = Path.of(System.getProperty("user.dir")).toFile()

@Serializable
enum class Type {
    GIF, IMG
}

val rightDynamicLength: DynamicLength =
    DynamicLength("100vw-100cw")
val bottomDynamicLength: DynamicLength =
    DynamicLength("100vh-100ch")
val xCenterDynamicLength: DynamicLength =
    DynamicLength("50vw")
val yCenterDynamicLength: DynamicLength =
    DynamicLength("50vh")

enum class Position {
    LEFT, RIGHT, TOP, BOTTOM, CENTER;

    fun toXOffsetValue(): OffsetValue = when (this) {
        LEFT -> NumberLength.EMPTY
        RIGHT -> rightDynamicLength
        TOP -> NumberLength.EMPTY
        BOTTOM -> bottomDynamicLength
        CENTER -> xCenterDynamicLength
    }

    fun toYOffsetValue(): OffsetValue = when (this) {
        LEFT -> NumberLength.EMPTY
        RIGHT -> rightDynamicLength
        TOP -> NumberLength.EMPTY
        BOTTOM -> bottomDynamicLength
        CENTER -> yCenterDynamicLength
    }
}

val TEXT_VAR_REGEX = "\\\$txt([1-9]\\d*)\\[(.*?)]".toRegex()
val TEXT_VAR_TOKENS = listOf("from", "to", "group", "bot")


enum class AvatarType {
    FROM, TO, GROUP, BOT, RANDOM;

    override fun toString(): String {
        return this.name.lowercase()
    }
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

enum class AvatarStyle {
    MIRROR, FLIP, GRAY, BINARIZATION
}