package moe.dituon.petpet.template.fields.transition

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.dituon.petpet.core.transition.EasingFunction

object EasingFunctionSerializer : KSerializer<EasingFunction> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Easing", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EasingFunction) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): EasingFunction =
        EasingFunction.fromString(decoder.decodeString())
}

typealias EasingElement = @Serializable(EasingFunctionSerializer::class) EasingFunction