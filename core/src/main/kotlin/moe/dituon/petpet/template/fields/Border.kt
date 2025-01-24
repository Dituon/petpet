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
import moe.dituon.petpet.core.utils.image.ImageBorder
import moe.dituon.petpet.uitls.wrapJsonElement

object BorderSerializer : KSerializer<ImageBorder> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Border", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ImageBorder) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): ImageBorder =
        ImageBorder.fromString(decoder.decodeString())
}

object BorderListSerializer : JsonTransformingSerializer<List<ImageBorder>>(ListSerializer(BorderSerializer)) {
    override fun transformDeserialize(element: JsonElement) = wrapJsonElement(element)
}

typealias BorderElement = @Serializable(BorderSerializer::class) ImageBorder
typealias BorderList = @Serializable(BorderListSerializer::class) List<BorderElement>