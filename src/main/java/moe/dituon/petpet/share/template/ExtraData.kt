package moe.dituon.petpet.share.template

import kotlinx.serialization.Serializable
import java.awt.image.BufferedImage
import java.util.function.Supplier

data class ExtraData(
    val avatar: Map<String, Supplier<List<BufferedImage>>>,
    val text: TextExtraData,
)

@Serializable
data class TextExtraData(
    val map: Map<String, String>,
    val list: List<String>,
)
