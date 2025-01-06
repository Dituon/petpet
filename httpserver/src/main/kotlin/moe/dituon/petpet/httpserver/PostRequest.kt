package moe.dituon.petpet.httpserver

import kotlinx.serialization.Serializable
import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.imgres.ImageResourceMap
import moe.dituon.petpet.uitls.GlobalJson

@Serializable
data class PostRequest(
    var id: String,
    val image: Map<String, String> = emptyMap(),
    val text: Map<String, String> = emptyMap(),
) {
    fun toRequestContext() = RequestContext(
        ImageResourceMap.fromUserStringMap(image), text
    )

    companion object {
        private const val TEXT_PREFIX = "text_"
        private const val IMAGE_PREFIX = "image_"

        @JvmStatic
        fun fromQueryMap(map: Map<String, List<String>>): PostRequest {
            val id = map["id"]?.first()
            requireNotNull(id) {
                throw IllegalArgumentException("Missing id parameter")
            }
            return fromQueryMap(id, map)
        }

        @JvmStatic
        fun fromQueryMap(id: String, map: Map<String, List<String>>): PostRequest {
            require(map.isNotEmpty()) {
                throw IllegalArgumentException("Empty query")
            }
            if (map.size == 1) {
                return PostRequest(id)
            }
            val images = HashMap<String, String>(map.size)
            val texts = HashMap<String, String>(map.size)
            for (entry in map) {
                val key = entry.key
                if (key.startsWith(TEXT_PREFIX)) {
                    texts[key.substring(TEXT_PREFIX.length)] = entry.value.first()
                } else if (key.startsWith(IMAGE_PREFIX)) {
                    images[key.substring(IMAGE_PREFIX.length)] = entry.value.first()
                }
            }
            return PostRequest(id, images, texts)
        }

        @JvmStatic
        fun fromString(str: String): PostRequest = GlobalJson.decodeFromString(str)

        @JvmStatic
        fun fromString(id: String, str: String): PostRequest {
            val req: PostRequest = GlobalJson.decodeFromString(str)
            req.id = id
            return req
        }
    }
}