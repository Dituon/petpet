package moe.dituon.petpet.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import moe.dituon.petpet.share.encodeDefaultsIgnoreUnknownKeysJson

@Serializable
data class UpdateIndex(
    val version: Float = 0f,
    val dataList: List<String> = emptyList(),
    val fontList: List<String> = emptyList(),
    val dataPath: String = DataUpdater.DEFAULT_REPO_DATA_PATH,
) {
    companion object {
        @JvmStatic
        fun fromString(str: String): UpdateIndex {
            return encodeDefaultsIgnoreUnknownKeysJson.decodeFromString(str)
        }
    }
}

@Serializable
data class UpdateIndexMap(
    val length: Map<String, Int> = emptyMap()
) {
    companion object {
        @JvmStatic
        fun fromString(str: String): UpdateIndexMap {
            return encodeDefaultsIgnoreUnknownKeysJson.decodeFromString(str)
        }
    }
}

