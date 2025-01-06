package moe.dituon.petpet.template.fields

import kotlinx.serialization.Serializable

@Serializable
enum class SpeedMode {
    FASTEST,
    SLOWEST,
    AVERAGE,
    CUSTOM;
}