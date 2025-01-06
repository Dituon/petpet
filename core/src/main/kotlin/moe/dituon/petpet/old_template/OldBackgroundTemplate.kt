package moe.dituon.petpet.old_template

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class OldBackgroundTemplate @JvmOverloads constructor(
    var size: JsonArray,
    var color: String = "#ffffff",
    var length: Int = 1
)