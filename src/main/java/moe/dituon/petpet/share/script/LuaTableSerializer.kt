package moe.dituon.petpet.share.script

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

public interface LuaTableFormat : SerialFormat {

    public fun <T> encodeToTable(serializer: SerializationStrategy<T>, value: T): LuaTable

    public fun <T> decodeFromTable(deserializer: DeserializationStrategy<T>, table: LuaTable): T
}

public inline fun <reified T> LuaTableFormat.encodeToTable(value: T): LuaTable =
    encodeToTable(serializersModule.serializer(), value)

public inline fun <reified T> LuaTableFormat.decodeFromTable(table: LuaTable): T =
    decodeFromTable(serializersModule.serializer(), table)

@OptIn(ExperimentalSerializationApi::class)
public sealed class LuaTableSerializer(
//    public val configuration: JsonConfiguration,
    override val serializersModule: SerializersModule
) : LuaTableFormat {
    public companion object Default : LuaTableSerializer(EmptySerializersModule)

    public final override fun <T> encodeToTable(serializer: SerializationStrategy<T>, value: T): LuaTable {
        val jsonObject = Json.encodeToJsonElement(serializer, value) as JsonObject
        return LuaTable()
    }

    private fun jsonObjectToLuaTable(jsonObject: JsonObject): LuaTable {
        val table = LuaTable()
        for ((key, value) in jsonObject.entries) {
            table.set(key, jsonElementToLuaValue(value))
        }
        return table
    }

    private fun jsonArrayToLuaTable(jsonArray: JsonArray): LuaTable {
        val table = LuaTable()
        jsonArray.forEachIndexed { index, element ->
            table.set(index + 1, jsonElementToLuaValue(element))
        }
        return table
    }

    private fun jsonElementToLuaValue(element: JsonElement): LuaValue {
        return when (element) {
            is JsonNull -> LuaValue.NIL
            is JsonPrimitive -> when {
                element.isString -> LuaValue.valueOf(element.content)
                element.booleanOrNull != null -> LuaValue.valueOf(element.boolean)
                element.intOrNull != null -> LuaValue.valueOf(element.int)
                element.doubleOrNull != null -> LuaValue.valueOf(element.double)
                else -> LuaValue.NIL
            }

            is JsonArray -> jsonArrayToLuaTable(element)
            is JsonObject -> jsonObjectToLuaTable(element)
        }
    }

    public final override fun <T> decodeFromTable(deserializer: DeserializationStrategy<T>, table: LuaTable): T {
        val jsonObject = luaTableToJsonElement(table)
        return Json.decodeFromJsonElement(deserializer, jsonObject)
    }

    private fun luaTableToJsonElement(table: LuaTable): JsonElement {
        val firstEle = table.get(1)
        if (firstEle.isnil()) {
            val keys = table.keys()
//            if (keys.isEmpty()) {
//                return JsonArray(emptyList());
//            }

            val jsonMap = mutableMapOf<String, JsonElement>()
            for (key in keys) {
                val value = table[key]
                jsonMap[key.tojstring()] = luaValueToJsonElement(value)
            }
            return JsonObject(jsonMap)
        } else {
            val jsonArr = mutableListOf<JsonElement>()
            for (key in table.keys()) {
                val value = table[key]
                jsonArr.add(luaValueToJsonElement(value))
            }
            return JsonArray(jsonArr)
        }
    }

    private fun luaValueToJsonElement(value: LuaValue): JsonElement {
        return when {
            value.isnil() -> JsonNull
            value.isboolean() -> JsonPrimitive(value.toboolean())
            value.isint() -> JsonPrimitive(value.toint())
            value.isnumber() -> JsonPrimitive(value.todouble())
            value.isstring() -> JsonPrimitive(value.tojstring())
            value.istable() -> luaTableToJsonElement(value.checktable())
            else -> JsonNull
        }
    }
}