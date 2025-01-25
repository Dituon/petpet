package moe.dituon.petpet.template

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import moe.dituon.petpet.core.length.Length
import moe.dituon.petpet.core.length.NumberLength
import moe.dituon.petpet.template.fields.ColorList
import moe.dituon.petpet.template.fields.length.LengthElement
import java.awt.Color

typealias TemplateCanvasBuilder = TemplateCanvas.Builder

@Serializable
data class TemplateCanvas(
    val width: LengthElement? = null,
    val height: LengthElement? = null,
    val length: LengthElement = NumberLength.px(1),
    val color: ColorList = emptyList(),
    val reverse: Boolean = false,
) {
    @Transient
    val isSizeDefined: Boolean = width != null && height != null

    constructor(builder: Builder): this(
        width = builder.width,
        height = builder.height,
        length = builder.length,
        color = builder.color,
        reverse = builder.reverse
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        var width: Length? = null
            private set
        var height: Length? = null
            private set
        var length: Length = NumberLength.px(1)
            private set
        var color: MutableList<Color> = mutableListOf()
            private set
        var reverse: Boolean = false

        fun width(width: Length?) = apply { this.width = width }
        fun width(width: Int) = apply { this.width = NumberLength.px(width) }
        fun height(height: Length?) = apply { this.height = height }
        fun height(height: Int) = apply { this.height = NumberLength.px(height) }
        fun length(length: Length) = apply { this.length = length }
        fun length(length: Int) = apply { this.length = NumberLength.px(length) }

        fun color(color: List<Color>) = apply { this.color = color.toMutableList() }
        fun color(color: Color) = apply { this.color = mutableListOf(color) }
        fun addColor(color: Color) = apply { this.color.add(color) }
        fun reverse(reverse: Boolean) = apply { this.reverse = reverse }

        fun build() = TemplateCanvas(this)
    }

}