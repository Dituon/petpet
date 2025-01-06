package moe.dituon.petpet.httpserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.dituon.petpet.uitls.GlobalJson

@Serializable
data class ServerServiceConfig(
    val port: Int = 2333,
    @SerialName("template_path")
    val templatePath: List<String> = listOf("data/xmmt.dituon.petpet/"),
    @SerialName("font_path")
    val fontPath: List<String> = listOf("data/xmmt.dituon.petpet/fonts/"),
    @SerialName("default_font_family")
    val defaultFontFamily: String = "MiSans",
//    val gifMaxSize: List<Int> = emptyList(),
//    val gifQuality: Int = 5,
//    val serviceThreadPoolSize: Int = 0,
    val headless: Boolean = true
) {
    fun toJsonString() = GlobalJson.encodeToString(serializer(), this)

    companion object {
        @JvmStatic
        fun fromJsonString(str: String): ServerServiceConfig {
            return GlobalJson.decodeFromString(str)
        }
    }
}
