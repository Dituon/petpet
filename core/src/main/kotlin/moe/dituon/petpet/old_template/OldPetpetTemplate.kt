package moe.dituon.petpet.old_template

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.jsonPrimitive
import moe.dituon.petpet.core.length.DynamicLength
import moe.dituon.petpet.core.length.Length
import moe.dituon.petpet.core.length.NumberLength
import moe.dituon.petpet.template.Metadata
import moe.dituon.petpet.template.PetpetTemplate
import moe.dituon.petpet.template.TemplateCanvas
import moe.dituon.petpet.template.TemplateType
import moe.dituon.petpet.template.element.BackgroundTemplate
import moe.dituon.petpet.template.element.ElementTemplate
import moe.dituon.petpet.template.fields.decodeColor
import moe.dituon.petpet.uitls.GlobalJson
import java.awt.Color
import java.io.File

@Serializable
data class OldPetpetTemplate(
    val type: Type,
    var avatar: List<OldAvatarTemplate> = emptyList(),
    var text: List<OldTextTemplate> = emptyList(),
    var background: OldBackgroundTemplate? = null,
    var delay: Int = 65,
    var alias: List<String>? = null,
    val inRandomList: Boolean? = true,
    var reverse: Boolean? = false,
    val hidden: Boolean? = false
) {
    @Transient
    var basePath: File = systemPath

    constructor(type: Type) : this(type, emptyList(), emptyList())

    fun toTemplate(): PetpetTemplate {
        val elements = mutableListOf<ElementTemplate>()
        var avatarIndex = 0
        var textIndex = 0
        for (avatarData in avatar) {
            if (!avatarData.avatarOnTop) elements.add(avatarData.toTemplate(avatarIndex++))
        }
        if (basePath.list()?.any { it.matches(bgRegex) } != false) {
            elements.add(BackgroundTemplate())
        }
        for (avatarData in avatar) {
            if (avatarData.avatarOnTop) elements.add(avatarData.toTemplate(avatarIndex++))
        }
        for (textData in text) {
            elements.add(textData.toTemplate(textIndex++))
        }

        fun replaceElementToken(text: String): String {
            return text.replace("Width", DynamicLength.ELEMENT_WIDTH_SUFFIX)
                .replace("Height", DynamicLength.ELEMENT_HEIGHT_SUFFIX)
        }

        val backgroundColor: List<Color> = background?.color?.takeIf { it.isNotEmpty() }
            ?.let { listOf(decodeColor(it)) } ?: emptyList()

        return PetpetTemplate(
            type = when (type) {
                Type.GIF -> TemplateType.GIF
                Type.IMG -> TemplateType.IMAGE
            },
            metadata = Metadata(
                alias = alias.orEmpty(),
                inRandomList = inRandomList ?: true,
            ),
            elements = elements,
            canvas = if (background == null) TemplateCanvas() else TemplateCanvas(
                width = Length.fromString(replaceElementToken(background!!.size[0].jsonPrimitive.content)),
                height = Length.fromString(replaceElementToken(background!!.size[1].jsonPrimitive.content)),
                color = backgroundColor,
                length = NumberLength.px(background!!.length),
            ),
            delay = intArrayOf(delay)
        ).apply {
            basePath = this@OldPetpetTemplate.basePath
        }
    }

    companion object {
        val bgRegex = Regex("\\d+.png$")

        @JvmStatic
        fun fromJson(str: String): OldPetpetTemplate {
            return GlobalJson.decodeFromString(str)
        }

        @JvmStatic
        fun fromJsonFile(file: File): OldPetpetTemplate {
            val template = fromJson(file.readText())
            template.basePath = file.parentFile
            return template
        }
    }
}