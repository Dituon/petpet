package moe.dituon.petpet.bot

import kotlinx.serialization.Serializable
import moe.dituon.petpet.template.Metadata

@Serializable
data class TemplateExtraMetadata(
    val alias: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val author: String = "",
    val desc: String = "",
    val hidden: Boolean = false,
    val inRandomList: Boolean = true,
    val preview: String? = null
) {
    fun toMetadata(): Metadata {
        return Metadata(
            alias = alias,
            tags = tags,
            author = author,
            desc = desc,
            hidden = hidden,
            inRandomList = inRandomList,
            preview = preview
        )
    }

    companion object {
        @JvmStatic
        fun fromMetadata(metadata: Metadata) = TemplateExtraMetadata(
            alias = metadata.alias,
            tags = metadata.tags,
            author = metadata.author,
            desc = metadata.desc,
            hidden = metadata.hidden,
            inRandomList = metadata.inRandomList,
            preview = metadata.preview
        )
    }
}