package moe.dituon.petpet.bot.qq.permission

import kotlinx.serialization.Serializable
import lombok.Builder
import moe.dituon.petpet.uitls.GlobalJson

@Serializable
@Builder
data class GroupPermissionConfig(
    val commandPermission: String?,
    val editPermission: String?,
    val disabledTemplates: Set<String>?,
    val nudgeProbability: Float?,
    val cooldownTime: Long?,
) {
    private constructor(builder: Builder) : this(
        commandPermission = builder.commandPermission,
        editPermission = builder.editPermission,
        disabledTemplates = builder.disabledTemplates,
        nudgeProbability = builder.nudgeProbability,
        cooldownTime = builder.cooldownTime
    )

    fun toJsonString(): String {
        return GlobalJson.encodeToString(serializer(), this)
    }

    companion object {
        @JvmStatic
        fun fromJsonString(str: String): GroupPermissionConfig =
            GlobalJson.decodeFromString(str)

        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        var commandPermission: String? = null
            private set
        var editPermission: String? = null
            private set
        var disabledTemplates: Set<String>? = null
            private set
        var nudgeProbability: Float? = null
            private set
        var cooldownTime: Long? = null
            private set

        fun commandPermission(commandPermission: String?) = apply {
            this.commandPermission = commandPermission
        }

        fun editPermission(editPermission: String?) = apply {
            this.editPermission = editPermission
        }

        fun disabledTemplates(disabledTemplates: Set<String>?) = apply {
            if (disabledTemplates.isNullOrEmpty()) {
                this.disabledTemplates = null
            } else {
                this.disabledTemplates = disabledTemplates
            }
        }

        fun nudgeProbability(nudgeProbability: Float?) = apply {
            this.nudgeProbability = nudgeProbability
        }

        fun cooldownTime(cooldownTime: Long?) = apply {
            this.cooldownTime = cooldownTime
        }

        fun build() = GroupPermissionConfig(this)
    }
}
