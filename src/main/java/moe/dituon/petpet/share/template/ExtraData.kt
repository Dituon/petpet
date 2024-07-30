package moe.dituon.petpet.share.template

import kotlinx.serialization.Serializable
import moe.dituon.petpet.share.service.ResourceManager
import java.awt.image.BufferedImage
import java.net.URI
import java.nio.file.Path
import java.util.function.Supplier

data class ExtraData(
    val avatar: AvatarExtraData,
    val text: TextExtraData,
)

data class AvatarExtraData(
    val map: Map<String, Supplier<List<BufferedImage>>>,
    val list: List<Supplier<List<BufferedImage>>>,
)

@Serializable
data class AvatarExtraDataUrls(
    val map: Map<String, String>,
    val list: List<String>,
) {
    public fun toAvatarExtraData(base: Path) = AvatarExtraData (
        map = map.mapValues {
            e -> ResourceManager.getDefaultInstance().getImageSupplier(URI(e.value), base)
        },
        list = list.map {
            e -> ResourceManager.getDefaultInstance().getImageSupplier(URI(e), base)
        }
    )
}

@Serializable
data class TextExtraData(
    val map: Map<String, String>,
    val list: List<String>,
)
