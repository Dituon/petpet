package moe.dituon.petpet.share.element.avatar

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import moe.dituon.petpet.share.*
import java.net.URI

object UriSerializer : KSerializer<URI> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("URI", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: URI) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): URI {
        return URI(decoder.decodeString())
    }
}

@Serializable
data class AvatarTemplate @JvmOverloads constructor(
    val type: String,
    @Serializable(with = UriSerializer::class)
    val default: URI? = null,
    val posType: AvatarPosType = AvatarPosType.ZOOM,
    val pos: JsonArray = Json.decodeFromString(JsonArray.serializer(), "[0,0,100,100]"),
    val cropType: CropType = CropType.NONE,
    val crop: IntArray? = null,
    val fit: FitType = FitType.FILL,
    val style: List<AvatarStyle> = emptyList(),
    val filter: List<AvatarFilter> = emptyList(),
    val angle: Short = 0,
    val origin: TransformOrigin = TransformOrigin.DEFAULT,
    val opacity: Float = 1.0F,
    val round: Boolean = false,
    val rotate: Boolean = false,
    val avatarOnTop: Boolean = true,
    val antialias: Boolean? = null,
    val resampling: Boolean? = null
)
