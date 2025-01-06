package moe.dituon.petpet.template.fields.transition

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import moe.dituon.petpet.core.transition.EasingFunction
import moe.dituon.petpet.core.transition.EasingKeyword
import moe.dituon.petpet.core.transition.RotateTransition

@Serializable
data class RotateTransitionSurrogate(
    val easing: EasingElement = EasingKeyword.LINEAR.function,
    val start: Float = 0f,
    val end: Float = 360f,
    @SerialName("rotate_count")
    val rotateCount: Int = 1,
)

object RotateTransitionSerializer : KSerializer<RotateTransition> {
    override val descriptor: SerialDescriptor = RotateTransitionSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: RotateTransition) {
        val surrogate = RotateTransitionSurrogate(
            start = value.start,
            end = value.end,
            rotateCount = value.rotateCount,
            easing = value.easing
        )
        encoder.encodeSerializableValue(RotateTransitionSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): RotateTransition =
        when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonPrimitive -> if (element.isString) when (element.content) {
                EasingKeyword.LINEAR.name -> RotateTransition.LINER
                EasingKeyword.EASE_IN.name -> RotateTransition.EASE_IN
                EasingKeyword.EASE_OUT.name -> RotateTransition.EASE_OUT
                EasingKeyword.EASE_IN_OUT.name -> RotateTransition.EASE_IN_OUT
                else -> RotateTransition.createDefaultInstance(EasingFunction.fromString(element.content))
            } else when (element.booleanOrNull) {
                true -> RotateTransition.DEFAULT
                false -> RotateTransition.empty()
                null -> throw IllegalArgumentException("Invalid rotate transition value: ${element.content}")
            }

            is JsonArray -> throw IllegalArgumentException("Rotate transition object can't decode via array")
            is JsonObject -> {
                val s = decoder.decodeSerializableValue(RotateTransitionSurrogate.serializer())
                RotateTransition.builder()
                    .rotateCount(s.rotateCount)
                    .start(s.start)
                    .end(s.end)
                    .easing(s.easing)
                    .build()
            }
        }
}

typealias RotateTransitionElement = @Serializable(RotateTransitionSerializer::class) RotateTransition