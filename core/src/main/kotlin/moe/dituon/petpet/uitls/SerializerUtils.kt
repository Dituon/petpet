package moe.dituon.petpet.uitls

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import moe.dituon.petpet.template.fields.ImageFilterTemplate

val GlobalJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    prettyPrint = true
    serializersModule = SerializersModule {
        polymorphicDefaultDeserializer(ImageFilterTemplate::class, ImageFilterTemplate::getDefaultDeserializer)
    }
}

fun wrapJsonElement(element: JsonElement) = if (element is JsonArray) element else JsonArray(listOf(element))

object FloatArraySerializer : JsonTransformingSerializer<FloatArray>(FloatArraySerializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}

typealias FloatOrArray = @Serializable(FloatArraySerializer::class) FloatArray

object IntArraySerializer : JsonTransformingSerializer<IntArray>(IntArraySerializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}

typealias IntOrArray = @Serializable(IntArraySerializer::class) IntArray

object BooleanArraySerializer : JsonTransformingSerializer<BooleanArray>(BooleanArraySerializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}

typealias BooleanOrArray = @Serializable(BooleanArraySerializer::class) BooleanArray

object StringListSerializer : JsonTransformingSerializer<List<String>>(ListSerializer(String.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}

typealias StringList = @Serializable(StringListSerializer::class) List<String>


object IntAsStringSerializer : JsonTransformingSerializer<String>(String.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        try {
            if (element.jsonPrimitive.isString) element else JsonPrimitive(element.jsonPrimitive.content)
        } catch (e: IllegalArgumentException) {
            JsonPrimitive(element.toString())
        }

}

object IntAsStringListSerializer : JsonTransformingSerializer<List<String>>(ListSerializer(IntAsStringSerializer)) {
    override fun transformDeserialize(element: JsonElement): JsonElement = wrapJsonElement(element)
}

typealias IntAsStringList = @Serializable(IntAsStringListSerializer::class) List<String>

