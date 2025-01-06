package moe.dituon.petpet.core.imgres

import kotlinx.serialization.Serializable

@Serializable
data class ResourceManagerConfig(
    val backgroundResourceCacheSize: Int = 8,
    val enableWebResource: Boolean = true,
    val avatarWebResourceCacheSize: Int = 8,
    val avatarLocalResourceCacheSize: Int = 8,
    val blockList: List<String> = emptyList(),
    val allowList: List<String> = emptyList(),
    val enableURLFileProtocol: Boolean = false,
    val enableUserLocalFileAccess: Boolean = false,
)
