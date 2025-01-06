import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.element.PetpetTemplateModel
import moe.dituon.petpet.core.imgres.ImageResourceMap
import moe.dituon.petpet.template.PetpetTemplate
import org.junit.jupiter.api.Test

class DependTest {
    @Test
    fun test() {
        val template = PetpetTemplate.fromJson(json)
        val model = PetpetTemplateModel(template)
        model.draw(
            RequestContext(
                ImageResourceMap.fromStringMap(
                    mapOf(
                        "t0" to "https://avatars.githubusercontent.com/u/68615161?v=4",
                        "t1" to "https://avatars.githubusercontent.com/u/68615161?v=4"
                    )
                ),
                mapOf()
            )
        )
    }
}

val json = """
    {
        "type": "image",
        "version": 100,
        "elements": [
            {
                "type": "image",
                "id": "e0",
                "key": "t1",
                "coords": [0, 0, "t0_width", "e1_height"]
            }, {
                "type": "text",
                "id": "e1",
                "coords": [100, 100],
                "text": "test"
            }, {
                "type": "image",
                "id": "e2",
                "key": "t1",
                "coords": [0, 0, "100vw", "100vh"]
            }
        ],
        "canvas": {
            "width": "e0_width",
            "height": "element_0_height",
            "length": "t0_length"
        }
    }
    """.trimIndent()
