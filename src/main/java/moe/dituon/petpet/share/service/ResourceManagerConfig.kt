package moe.dituon.petpet.share.service

import kotlinx.serialization.Serializable

@Serializable
data class ResourceManagerConfig(
    val backgroundResourceCacheSize: Int = 8,
    val enableWebResource: Boolean = true,
    val avatarWebResourceCacheSize: Int = 4,
    val avatarLocalResourceCacheSize: Int = 4,
)
