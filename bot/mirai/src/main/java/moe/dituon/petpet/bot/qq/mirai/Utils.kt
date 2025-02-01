package moe.dituon.petpet.bot.qq.mirai

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.dituon.petpet.bot.TemplateExtraMetadata
import net.mamoe.mirai.contact.Member
import net.mamoe.yamlkt.Yaml

fun getMemberName(member: Member?): String {
    if (member == null) return "未知用户"
    return member.nameCard.ifEmpty { member.nick }
}

object CustomMetadataConfigSerializer: KSerializer<Map<String, TemplateExtraMetadata>> {
    private val mapSerializer = MapSerializer(String.serializer(), TemplateExtraMetadata.serializer())

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Map<String, TemplateExtraMetadata>) {
        mapSerializer.serialize(encoder, value.toSortedMap())
    }

    override fun deserialize(decoder: Decoder): Map<String, TemplateExtraMetadata> {
        return mapSerializer.deserialize(decoder)
    }
}

fun decodeCustomMetadataConfig(str: String): Map<String, TemplateExtraMetadata> =
    Yaml.decodeFromString(CustomMetadataConfigSerializer, str)

fun encodeCustomMetadataConfig(map: Map<String, TemplateExtraMetadata>): String =
    Yaml { encodeDefaultValues = false }.encodeToString(CustomMetadataConfigSerializer, map)
