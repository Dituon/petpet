package moe.dituon.petpet.share.element.text

import kotlinx.serialization.Serializable
import moe.dituon.petpet.share.*
import java.awt.Color

@Serializable
data class TextTemplate(
    var text: String,
    var pos: IntArray = intArrayOf(0, 0),
    var angle: Short = 0,
    var color: String = TextModel.DEFAULT_COLOR_STR,
    var font: String = "simsun",
    var size: Int = 16,
    var align: TextAlign = TextAlign.LEFT,
    var baseline: TextBaseline = TextBaseline.TOP,
    var wrap: TextWrap = TextWrap.NONE,
    var style: TextStyle = TextStyle.PLAIN,
    var position: List<Position>? = listOf(Position.LEFT, Position.TOP),
    var origin: TransformOrigin = TransformOrigin.DEFAULT,
    var strokeColor: String = TextModel.DEFAULT_STROKE_COLOR_STR,
    var strokeSize: Short = 0,
    var greedy: Boolean = false
) {
    fun getAwtColor(): Color {
        if (color == moe.dituon.petpet.share.TextModel.DEFAULT_COLOR_STR) return moe.dituon.petpet.share.TextModel.DEFAULT_COLOR
        return decodeColor(color)
    }

    fun getStrokeAwtColor(): Color {
        if (strokeColor == moe.dituon.petpet.share.TextModel.DEFAULT_STROKE_COLOR_STR) return moe.dituon.petpet.share.TextModel.DEFAULT_STROKE_COLOR
        return decodeColor(strokeColor)
    }
}
