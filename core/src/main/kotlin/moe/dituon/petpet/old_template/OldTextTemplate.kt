package moe.dituon.petpet.old_template

import kotlinx.serialization.Serializable
import moe.dituon.petpet.core.length.Length
import moe.dituon.petpet.core.length.LengthType
import moe.dituon.petpet.core.length.NumberLength
import moe.dituon.petpet.core.position.TextXYWCoords
import moe.dituon.petpet.core.transform.Offset
import moe.dituon.petpet.template.element.*
import moe.dituon.petpet.template.fields.decodeColor

@Serializable
data class OldTextTemplate @JvmOverloads constructor(
    var text: String,
    var pos: IntArray = intArrayOf(0, 0),
    var angle: Short = 0,
    var color: String = "#191919",
    var font: String = "",
    var size: Int = 16,
    var align: TextAlign = TextAlign.LEFT,
    var baseline: TextBaseline = TextBaseline.TOP,
    var wrap: TextWrap = TextWrap.NONE,
    var style: TextStyle = TextStyle.PLAIN,
    var position: List<Position> = listOf(Position.LEFT, Position.TOP),
    var origin: TransformOrigin = TransformOrigin.DEFAULT,
    var strokeColor: String = "#ffffff",
    var strokeSize: Short = 0,
    var greedy: Boolean = false
) {
    fun toTemplate(index: Int): TextTemplate {
        var text = text
        TEXT_VAR_TOKENS.forEach { s -> text = text.replace("\$${s}", "\${$s:-$s}") }
        // 将第一个参数替换为 raw key 代替旧版的贪婪匹配
        var shouldReplaceRaw = greedy
        text = TEXT_VAR_REGEX.replace(text) {
            var key = if (shouldReplaceRaw) {
                shouldReplaceRaw = false
                "raw"
            } else {
                it.groups[1]?.value!!
            }
            // 旧版的 $txt<n>[] 变量索引从 1 开始, 新版索引从 0 开始
            key = (key.toIntOrNull()?.minus(1) ?: key).toString()
            "\${${key}:${it.groups[2]?.value}}"
        }
        var x: Length =
            NumberLength(pos[0].toFloat(), LengthType.PX)
        var y: Length =
            NumberLength(pos[1].toFloat(), LengthType.PX)
        val w = if (pos.size > 2) NumberLength(
            pos[2].toFloat(),
            LengthType.PX
        ) else null
        when (position[0]) {
            Position.CENTER -> x = xCenterDynamicLength.plus(x)
            Position.RIGHT -> x = rightDynamicLength.plus(x)
            else -> {}
        }
        when (position[1]) {
            Position.CENTER -> y = yCenterDynamicLength.plus(y)
            Position.BOTTOM -> {
                y = bottomDynamicLength.plus(y)
                baseline = TextBaseline.BOTTOM
            }
            else -> {}
        }

        var minSize = size
        var maxSize = 96
        if (wrap == TextWrap.ZOOM) {
            minSize = 16
            maxSize = size
        }

        return TextTemplate(
            id = "text$index",
            text = listOf(text),
            coords = listOf(
                TextXYWCoords(
                    if (w == null) listOf(x, y) else listOf(x, y, w)
                )
            ),
            angle = floatArrayOf(angle.toFloat()),
            color = listOf(decodeColor(color)),
            fontName = if (font.isEmpty()) listOf() else listOf(font),
            size = floatArrayOf(minSize.toFloat()),
            maxSize = floatArrayOf(maxSize.toFloat()),
            align = listOf(align),
            baseline = listOf(
                if (align == TextAlign.CENTER) {
                    TextBaseline.MIDDLE
                } else {
                    TextBaseline.TOP
                }
            ),
            wrap = listOf(wrap),
            style = listOf(style),
            origin = listOf(
                when (origin) {
                    TransformOrigin.DEFAULT -> Offset.LEFT_TOP
                    TransformOrigin.CENTER -> Offset.CENTER
                }
            ),
            strokeColor = listOf(decodeColor(strokeColor)),
            strokeSize = floatArrayOf(strokeSize.toFloat()),
        )
    }
}