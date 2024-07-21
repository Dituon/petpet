package moe.dituon.petpet.share.script

import kotlinx.serialization.Serializable
import moe.dituon.petpet.share.service.ResourceManager
import moe.dituon.petpet.share.template.ExtraData
import moe.dituon.petpet.share.template.TextExtraData
import org.luaj.vm2.LuaTable
import java.net.URI
import java.nio.file.Path

@Serializable
data class LuaExtraData(
    val avatar: LuaAvatarExtraData? = null,
    val text: LuaTextExtraData? = null,
) {
    companion object {
        @JvmStatic
        fun fromLuaTable(table: LuaTable): LuaExtraData {
            return LuaTableSerializer.decodeFromTable(serializer(), table)
        }
    }

    fun toExtraData(base: Path): ExtraData {
        return ExtraData(
            avatar = avatar?.map?.mapValues { e ->
                ResourceManager.getDefaultInstance().getImageSupplier(URI(e.value), base)
            } ?: emptyMap(),
            text = TextExtraData(
                text?.map ?: emptyMap(),
                text?.list ?: emptyList()
            )
        )
    }
}

@Serializable
data class LuaAvatarExtraData(
    val map: Map<String, String>? = null,
    val list: List<String>? = null,
)

@Serializable
data class LuaTextExtraData(
    val map: Map<String, String>? = null,
    val list: List<String>? = null,
)
