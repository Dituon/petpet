import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.imgres.ImageResourceMap
import moe.dituon.petpet.script.event.ScriptRequestContextWrapper
import org.junit.jupiter.api.Test
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Compilable
import kotlin.io.path.Path

class ScriptRequestContextWrapperTest {
    @Test
    fun testUpdate() {
        val context0 = ScriptRequestContextWrapper(
            RequestContext(
                ImageResourceMap.fromStringMap(
                    mapOf(
                        "from" to "./input.png"
                    )
                ), HashMap()
            )
        )
        val context1 =
            ScriptRequestContextWrapper(RequestContext.newEmpty())

        val scriptEngine = NashornScriptEngineFactory().scriptEngine
        val script = (scriptEngine as Compilable).compile(
            """
            context0.image.to = "./input.png"
            context0.text.to = "to"
            context1.image = {
                to: "./input.png"
            }
        """.trimIndent()
        )
        val bindings = scriptEngine.createBindings()
        bindings["context0"] = context0
        bindings["context1"] = context1
        script.eval(bindings)
        val c0 = context0.toRequestContext()
        val c1 = context1.toRequestContext()
        assert(c0.imageResourceMap["from"]?.src?.let { Path(it) } == Path("./input.png"))
        assert(c0.imageResourceMap["to"]?.src?.let { Path(it) } == Path("./input.png"))
        assert(c0.textDataMap["to"] == "to")
        assert(c1.imageResourceMap["to"]?.src?.let { Path(it) } == Path("./input.png"))
    }
}