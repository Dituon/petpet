package moe.dituon.petpet.template.fields.length

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
import moe.dituon.petpet.core.length.Length
import moe.dituon.petpet.core.utils.image.ImageCropper
import moe.dituon.petpet.uitls.wrapJsonElement

val CropCoordsSerializer: KSerializer<List<Length>> = ListSerializer(LengthSerializer)

object AvatarCropSerializer : KSerializer<ImageCropper> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AvatarXYWHCoords", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ImageCropper) =
        XYWHCoordsSerializer.serialize(encoder, listOf(value.x1, value.y1, value.x2, value.y2))

    override fun deserialize(decoder: Decoder): ImageCropper =
        ImageCropper.create(CropCoordsSerializer.deserialize(decoder))
}

object AvatarCropperListSerializer :
    JsonTransformingSerializer<List<ImageCropper>>(ListSerializer(AvatarCropSerializer)) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}

typealias AvatarCropper = @Serializable(AvatarCropSerializer::class) ImageCropper
typealias AvatarCropperList = @Serializable(AvatarCropperListSerializer::class) List<AvatarCropper>
