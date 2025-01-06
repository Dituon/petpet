import moe.dituon.petpet.core.context.CanvasContext
import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.element.text.GraphicsParagraph
import moe.dituon.petpet.core.element.text.TextDynamicModel
import moe.dituon.petpet.core.element.text.TextModel
import moe.dituon.petpet.core.length.NumberLength
import moe.dituon.petpet.core.position.TextXYWCoords
import moe.dituon.petpet.template.element.*
import moe.dituon.petpet.template.element.TextAlign.*
import moe.dituon.petpet.template.element.TextBaseline.*
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

class TextTest {
    @Test
    @Suppress("UNCHECKED_CAST")
    fun testDynamicVars() {
        val template = TextTemplateBuilder("test \${k1:-default} \${k2:-default}").build()
        val textMap = mapOf(
            "k1" to "1"
        )
        val model = TextModel.createTextModel(template)
        assert(model is TextDynamicModel)
        val rendered = (model as TextDynamicModel).render(
            CanvasContext(1),
            RequestContext.fromTextMap(textMap)
        ) as TextDynamicModel.RenderedElement
        val paragraphList: List<GraphicsParagraph> =
            rendered::class.java.getDeclaredField("paragraphList").apply {
                isAccessible = true
            }.get(rendered) as List<GraphicsParagraph>
        assert(paragraphList[0].string.text == "test 1 default")
    }

    @Test
    @Throws(IOException::class)
    fun testWrapNone() {
        val list = ArrayList<BufferedImage>()
        for (align in testAligns) {
            for (baseline in testBaselines) {
                val template = TextTemplateBuilder(
                    """
                         TextWrap.NONE
                         段落测试
                         (align = $align, baseline = $baseline)
                    """.trimIndent()
                )
                    .wrap(TextWrap.NONE)
                    .align(align)
                    .baseline(baseline)
                list.add(testTextModel(template))
            }
        }
        val img: BufferedImage = createImageGroup(list)
        saveImage(img, "text-wrap-none")
    }

    @Test
    @Throws(IOException::class)
    fun testWrapBreak() {
        val list = ArrayList<BufferedImage>()
        for (align in testAligns) {
            for (baseline in testBaselines) {
                val template = TextTemplateBuilder(
                    """
                         TextWrap.BREAK
                         段落测试
                         (align = $align, baseline = $baseline)
                    """.trimIndent()
                )
                    .wrap(TextWrap.BREAK)
                    .align(align)
                    .baseline(baseline)
                list.add(testTextModel(template))
            }
        }
        val img: BufferedImage = createImageGroup(list)
        saveImage(img, "text-wrap-break")
    }

    @Test
    @Throws(IOException::class)
    fun testWrapZoom() {
        val list = ArrayList<BufferedImage>()
        for (align in testAligns) {
            for (baseline in testBaselines) {
                val template = TextTemplateBuilder(
                    """
                         TextWrap.ZOOM
                         段落测试
                         (align = $align, baseline = $baseline)
                    """.trimIndent()
                )
                    .wrap(TextWrap.ZOOM)
                    .align(align)
                    .baseline(baseline)
                list.add(testTextModel(template))
            }
        }
        val img: BufferedImage = createImageGroup(list)
        saveImage(img, "text-wrap-zoom")
    }

    companion object {
        const val outputDir: String = "./test-output/text/"
        val defaultWidth = 200
        val testAligns: Array<TextAlign> = TextAlign.entries.toTypedArray()
        val testBaselines = arrayOf(
            TOP,
            MIDDLE,
            BOTTOM
        )

        fun getTestImage(width: Int, height: Int): BufferedImage {
            return BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
        }

        fun testTextModel(template: TextTemplateBuilder): BufferedImage {
            return testTextModel(template, defaultWidth)
        }

        fun testTextModel(template: TextTemplateBuilder, width: Int): BufferedImage {
            val img: BufferedImage =
                getTestImage(width, width)

            val coords = ArrayList(Collections.nCopies(2, NumberLength.EMPTY))

            when (template.align[0]) {
                CENTER -> coords[0] = NumberLength.px(img.width / 2)
                RIGHT -> coords[0] = NumberLength.px(img.width)
                else -> {}
            }
            when (template.baseline[0]) {
                MIDDLE -> coords[1] = NumberLength.px(img.height / 2)
                BOTTOM -> coords[1] = NumberLength.px(img.height)
                else -> {}
            }
            template.coords(TextXYWCoords(listOf(coords[0], coords[1], NumberLength.px(width))))

            val g2d = img.createGraphics()
            g2d.color = Color.RED
            g2d.drawRect(0, 0, img.width - 1, img.height - 1)

            g2d.color = Color.BLUE
            val x = coords[0].rawValue.toInt()
            val y = coords[1].rawValue.toInt()

            g2d.drawLine(x, 0, x, img.height)
            g2d.drawLine(0, y, img.width, y)
            g2d.fillRect(x - 5, y - 5, 10, 10)

            val model = TextModel(template.build())
            model.draw(
                CanvasContext(img, mutableMapOf(), null, false),
                null
            )
            return img
        }

        fun createImageGroup(images: List<BufferedImage>): BufferedImage {
            val col = 3
            val width = images[0].width * col
            val height = images[0].height * (images.size / col)
            val img = BufferedImage(width, height, images[0].type)
            val g2d = img.createGraphics()
            g2d.color = Color.WHITE
            g2d.fillRect(0, 0, width, height)
            for (i in images.indices) {
                g2d.drawImage(images[i], i % col * images[0].width, i / col * images[0].height, null)
            }
            return img
        }

        @Throws(IOException::class)
        fun saveImage(image: BufferedImage?, name: String) {
            val path = "$outputDir$name.png"
            val file = File(path)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            ImageIO.write(image, "png", file)
        }
    }
}
