package moe.dituon.petpet.template.fields.length

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
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
import moe.dituon.petpet.core.length.PercentageLength
import moe.dituon.petpet.core.utils.image.ImageCropper
import moe.dituon.petpet.core.utils.image.ImagePolygonCropper
import moe.dituon.petpet.core.utils.image.ImageXYWHCropper
import moe.dituon.petpet.uitls.wrapJsonElement

val XYWHCropCoordsSerializer: KSerializer<List<PercentageLength>> = ListSerializer(PercentageLengthSerializer)
val PolygonCropCoordsSerializer: KSerializer<List<List<PercentageLength>>> = ListSerializer(ListSerializer(PercentageLengthSerializer))

object XYWHCropElementSerializer : KSerializer<ImageXYWHCropper> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AvatarXYWHCoords", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ImageXYWHCropper) =
        XYWHCoordsSerializer.serialize(encoder, listOf(value.x1, value.y1, value.x2, value.y2))

    override fun deserialize(decoder: Decoder): ImageXYWHCropper =
        ImageXYWHCropper.create(XYWHCropCoordsSerializer.deserialize(decoder))
}

object PolygonCropElementSerializer : KSerializer<ImagePolygonCropper> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AvatarPolygonCropCoords", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ImagePolygonCropper) =
        PolygonCropCoordsSerializer.serialize(encoder, value.points)

    override fun deserialize(decoder: Decoder): ImagePolygonCropper =
        ImagePolygonCropper(PolygonCropCoordsSerializer.deserialize(decoder))
}

object EmptyCropCoordsSerializer : KSerializer<ImageCropper.EmptyImageCropper> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AvatarEmptyCropCoords", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: ImageCropper.EmptyImageCropper) = encoder.encodeNull()

    override fun deserialize(decoder: Decoder): ImageCropper.EmptyImageCropper =
        ImageCropper.EmptyImageCropper.INSTANCE
}

object AvatarCropperElementSerializer : JsonContentPolymorphicSerializer<ImageCropper>(
    ImageCropper::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ImageCropper> =
        when (element) {
            is JsonNull -> EmptyCropCoordsSerializer
            is JsonArray -> when(element[0]) {
                is JsonPrimitive -> XYWHCropElementSerializer
                is JsonArray -> PolygonCropElementSerializer
                else -> throw Exception()
            }
            else -> throw Exception()
        }
}

object AvatarCropperListSerializer :
    JsonTransformingSerializer<List<ImageCropper>>(ListSerializer(AvatarCropperElementSerializer)) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}

typealias AvatarCropper = @Serializable(AvatarCropperElementSerializer::class) ImageCropper
typealias AvatarCropperList = @Serializable(AvatarCropperListSerializer::class) List<AvatarCropper>
