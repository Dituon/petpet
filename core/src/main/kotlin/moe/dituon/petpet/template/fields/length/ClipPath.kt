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
import moe.dituon.petpet.core.clip.BorderRadius
import moe.dituon.petpet.uitls.wrapJsonElement

object BorderRadiusSerializer : KSerializer<BorderRadius> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BorderRadius", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BorderRadius) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): BorderRadius =
        BorderRadius.fromString(decoder.decodeString())
}

object BorderRadiusListSerializer :
    JsonTransformingSerializer<List<BorderRadius>>(ListSerializer(BorderRadiusSerializer)) {
    override fun transformDeserialize(element: JsonElement) = wrapJsonElement(element)
}

typealias BorderRadiusElement = @Serializable(BorderRadiusSerializer::class) BorderRadius
typealias BorderRadiusList = @Serializable(BorderRadiusListSerializer::class) List<BorderRadiusElement>
