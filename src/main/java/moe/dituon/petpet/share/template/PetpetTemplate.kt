package moe.dituon.petpet.share.template

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moe.dituon.petpet.share.BackgroundData
import moe.dituon.petpet.share.element.avatar.AvatarTemplate
import moe.dituon.petpet.share.element.text.TextTemplate
import moe.dituon.petpet.share.script.LuaTableSerializer
import org.luaj.vm2.LuaTable

enum class TemplateType {
    GIF, IMG
}

@Serializable
data class PetpetTemplate(
    val type: TemplateType,
    val avatar: List<AvatarTemplate> = emptyList(),
    val text: List<TextTemplate> = emptyList(),
    val background: BackgroundData? = null,
    val delay: Int = 65,
    val alias: List<String> = emptyList(),
    val inRandomList: Boolean = true,
    val reverse: Boolean = false,
    val hidden: Boolean = false
) {
    companion object {
        @JvmStatic
        fun fromString(str: String): PetpetTemplate {
            return Json.decodeFromString(str)
        }

        @JvmStatic
        fun fromLuaTable(table: LuaTable): PetpetTemplate {
            return LuaTableSerializer.decodeFromTable(serializer(), table)
        }
    }
}
