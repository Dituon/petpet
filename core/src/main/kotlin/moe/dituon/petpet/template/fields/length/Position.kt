package moe.dituon.petpet.template.fields.length

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import moe.dituon.petpet.core.length.Length
import moe.dituon.petpet.core.position.*
import moe.dituon.petpet.uitls.wrapJsonElement

object AvatarXYWHCoordsSerializer : KSerializer<AvatarXYWHCoords> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AvatarXYWHCoords", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: AvatarXYWHCoords) =
        XYWHCoordsSerializer.serialize(encoder, value.coordsList)

    override fun deserialize(decoder: Decoder) =
        AvatarXYWHCoords(XYWHCoordsSerializer.deserialize(decoder))
}

object AvatarP4ACoordsSerializer : KSerializer<AvatarP4ACoords> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AvatarP4ACoords", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: AvatarP4ACoords) =
        P4ACoordsSerializer.serialize(encoder, value.coordsList)

    override fun deserialize(decoder: Decoder) =
        AvatarP4ACoords(P4ACoordsSerializer.deserialize(decoder))
}

object TextXYWCoordsSerializer : KSerializer<TextXYWCoords> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TextXYWCoords", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TextXYWCoords) =
        TextCoordsSerializer.serialize(encoder, value.coordsList)

    override fun deserialize(decoder: Decoder) =
        TextXYWCoords(TextCoordsSerializer.deserialize(decoder))
}

val XYWHCoordsSerializer: KSerializer<List<Length>> = ListSerializer(LengthSerializer)
val P4ACoordsSerializer: KSerializer<List<List<Length>>> = ListSerializer(ListSerializer(LengthSerializer))
val TextCoordsSerializer: KSerializer<List<Length>> = ListSerializer(LengthSerializer)

object AvatarCoordsElementSerializer : JsonContentPolymorphicSerializer<AvatarCoords>(
    AvatarCoords::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AvatarCoords> =
        when (element.jsonArray.size) {
            4 -> AvatarXYWHCoordsSerializer
            5 -> AvatarP4ACoordsSerializer
            else -> throw Exception()
        }
}

object AvatarCoordsListSerializer :
    JsonTransformingSerializer<List<AvatarCoords>>(
        ListSerializer(
            AvatarCoordsElementSerializer
        )
    ) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val array = element.jsonArray
        if (array.isEmpty()) throw IllegalArgumentException("Coords list can not be empty")
        val firstEle = array[0]
        if (firstEle is JsonPrimitive || firstEle.jsonArray.size == 2) return JsonArray(listOf(element))
        return element
    }
}

object TextXYWCoordsListSerializer : JsonTransformingSerializer<List<TextXYWCoords>>(
    ListSerializer(TextXYWCoordsSerializer)
) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element.jsonArray[0] is JsonPrimitive) JsonArray(listOf(element)) else element
    }
}

typealias AvatarCoordsElement = @Serializable(AvatarCoordsElementSerializer::class) AvatarCoords
typealias AvatarCoordsList = @Serializable(AvatarCoordsListSerializer::class) List<AvatarCoordsElement>

typealias TextCoordsElement = @Serializable(TextXYWCoordsSerializer::class) TextXYWCoords
typealias TextCoordsList = @Serializable(TextXYWCoordsListSerializer::class) List<TextCoordsElement>

object FitTypeSerializer : KSerializer<FitType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FitType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FitType) =
        encoder.encodeString(value.keyword)

    override fun deserialize(decoder: Decoder): FitType =
        FitType.fromString(decoder.decodeString())
}

object FitTypeListSerializer : JsonTransformingSerializer<List<FitType>>(ListSerializer(FitTypeSerializer)) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}

typealias FitTypeElement = @Serializable(FitTypeSerializer::class) FitType
typealias FitTypeList = @Serializable(FitTypeListSerializer::class) List<FitTypeElement>
