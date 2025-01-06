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
import moe.dituon.petpet.core.transform.Offset
import moe.dituon.petpet.uitls.wrapJsonElement

object OffsetSerializer : KSerializer<Offset> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Offset", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Offset) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Offset =
        Offset.fromString(decoder.decodeString())
}

object OffsetListSerializer : JsonTransformingSerializer<List<Offset>>(ListSerializer(OffsetSerializer)) {
    override fun transformDeserialize(element: JsonElement) = wrapJsonElement(element)
}

typealias OffsetElement = @Serializable(OffsetSerializer::class) Offset
typealias OffsetList = @Serializable(OffsetListSerializer::class) List<OffsetElement>
