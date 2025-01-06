package moe.dituon.petpet.template.fields

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import moe.dituon.petpet.uitls.wrapJsonElement
import java.awt.Color

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeString(
            "#${
                value.red.toString(16)
            }${
                value.green.toString(16)
            }${
                value.blue.toString(16)
            }${
                if (value.alpha == 255) "" else value.alpha.toString(16)
            }"
        )
    }

    override fun deserialize(decoder: Decoder): Color {
        return decodeColor(decoder.decodeString())
    }
}

fun decodeColor(str: String): Color {
    var hex = str
    if (hex.startsWith("#")) {
        hex = hex.substring(1)
    }
    return when (hex.length) {
        6 -> { // #RRGGBB
            val rgb = hex.toInt(16)
            val r = (rgb shr 16) and 0xFF
            val g = (rgb shr 8) and 0xFF
            val b = rgb and 0xFF
            Color(r, g, b)
        }

        8 -> { // #RRGGBBAA
            val rgba = hex.toLong(16)
            val r = (rgba shr 24 and 0xFF).toInt()
            val g = (rgba shr 16 and 0xFF).toInt()
            val b = (rgba shr 8 and 0xFF).toInt()
            val a = (rgba and 0xFF).toInt()
            Color(r, g, b, a)
        }

        3 -> { // #RGB
            val r = (hex[0].digitToInt(16) * 0x11)
            val g = (hex[1].digitToInt(16) * 0x11)
            val b = (hex[2].digitToInt(16) * 0x11)
            Color(r, g, b)
        }

        4 -> { // #RGBA
            val r = (hex[0].digitToInt(16) * 0x11)
            val g = (hex[1].digitToInt(16) * 0x11)
            val b = (hex[2].digitToInt(16) * 0x11)
            val a = (hex[3].digitToInt(16) * 0x11)
            Color(r, g, b, a)
        }

        else -> throw IllegalArgumentException("Invalid color format")
    }
}

object ColorListSerializer : JsonTransformingSerializer<List<Color>>(ListSerializer(ColorSerializer)) {
    override fun transformDeserialize(element: JsonElement) = wrapJsonElement(element)
}

typealias ColorElement = @Serializable(ColorSerializer::class) Color
typealias ColorList = @Serializable(ColorListSerializer::class) List<ColorElement>
