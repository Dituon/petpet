package moe.dituon.petpet.core

import kotlinx.serialization.Serializable

@Serializable
open class BaseRenderConfig (
    open val gifQuality: Int = 10,
)