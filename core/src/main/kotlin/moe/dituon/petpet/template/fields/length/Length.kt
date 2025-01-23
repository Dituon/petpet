package moe.dituon.petpet.template.fields.length

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import moe.dituon.petpet.core.length.Length
import moe.dituon.petpet.core.length.LengthType
import moe.dituon.petpet.core.length.NumberLength
import moe.dituon.petpet.core.length.NumberPercentageLength
import moe.dituon.petpet.core.length.PercentageLength

object LengthAsStringSerializer : KSerializer<Length> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Length", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Length) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Length =
        Length.fromString(decoder.decodeString())
}

object PercentageLengthAsStringSerializer : KSerializer<PercentageLength> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Length", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PercentageLength) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): PercentageLength =
        PercentageLength.fromString(decoder.decodeString())
}

object LengthAsNumberSerializer : KSerializer<Length> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Length", PrimitiveKind.FLOAT)

    override fun serialize(encoder: Encoder, value: Length) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder) =
        NumberLength(decoder.decodeFloat(), LengthType.PX)
}

object PercentageLengthAsNumberSerializer : KSerializer<PercentageLength> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Length", PrimitiveKind.FLOAT)

    override fun serialize(encoder: Encoder, value: PercentageLength) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder) =
        NumberPercentageLength(NumberLength.px(decoder.decodeFloat()))
}


object LengthSerializer : JsonContentPolymorphicSerializer<Length>(Length::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        element is JsonPrimitive -> if (element.jsonPrimitive.isString)
            LengthAsStringSerializer else LengthAsNumberSerializer

        else -> throw Exception()
    }
}

object PercentageLengthSerializer : JsonContentPolymorphicSerializer<PercentageLength>(PercentageLength::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        element is JsonPrimitive -> if (element.jsonPrimitive.isString)
            PercentageLengthAsStringSerializer else PercentageLengthAsNumberSerializer

        else -> throw Exception()
    }
}

typealias LengthElement = @Serializable(LengthSerializer::class) Length
typealias PercentageLengthElement = @Serializable(PercentageLengthSerializer::class) PercentageLength