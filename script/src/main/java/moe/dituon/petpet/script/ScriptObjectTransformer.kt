package moe.dituon.petpet.script
import kotlinx.serialization.json.*
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror

fun scriptObjectToJsonElement(obj: ScriptObjectMirror): JsonElement {
    return when {
        obj.isArray -> {
            JsonArray(obj.values.map { convertToJsonElement(it) })
        }
        else -> {
            JsonObject(obj.keys.associateWith { key ->
                convertToJsonElement(obj[key])
            })
        }
    }
}

private fun convertToJsonElement(value: Any?): JsonElement {
    return when (value) {
        null -> JsonNull
        is String -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is ScriptObjectMirror -> scriptObjectToJsonElement(value)
        else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
    }
}
