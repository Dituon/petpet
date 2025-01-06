package utils

import moe.dituon.petpet.core.element.PetpetTemplateModel
import moe.dituon.petpet.core.length.NumberLength
import moe.dituon.petpet.core.position.AvatarXYWHCoords
import moe.dituon.petpet.template.PetpetTemplate
import moe.dituon.petpet.template.TemplateCanvas
import moe.dituon.petpet.template.element.AvatarTemplateBuilder
import java.io.File
import java.io.FileNotFoundException
import java.util.function.Function

class ImageCssComparator(private val name: String) {
    private val outputPath = "test-output/$name"
    private val htmlFile = File("$outputPath/index.html")
    private val outputImagesDirectoryPath = "$outputPath/images"
    private val parts: MutableList<String> = ArrayList()

    fun addImagePart(
        partName: String,
        cssTokens: List<String>,
        imageBuilderFun: Function<AvatarTemplateBuilder, AvatarTemplateBuilder>
    ) {
        val builder = AvatarTemplateBuilder()
            .default(TEST_INPUT_IMAGE.toString())
            .coords(
                AvatarXYWHCoords(
                    listOf(
                        NumberLength.EMPTY,
                        NumberLength.EMPTY,
                        NumberLength.px(outputWidth),
                        NumberLength.px(outputHeight)
                    )
                )
            )
        val imageEle = imageBuilderFun.apply(builder).build()
        val template = PetpetTemplate(
            elements = listOf(imageEle),
            canvas = TemplateCanvas(
                width = NumberLength.px(outputWidth),
                height = NumberLength.px(outputHeight)
            )
        )
        val image = PetpetTemplateModel(template).draw()
        val imageFile = File("$outputImagesDirectoryPath/${partName}.${image.format}").absoluteFile
        image.save(imageFile)
        val cssText = cssTokens.joinToString("; ")
        parts.add("<div class=\"image-pair\">")
        parts.add(
            figure(
                cssTokens.joinToString("<br/>"),
                "<image style=\"$cssText\" src=\"$TEST_INPUT_IMAGE\" />"
            )
        )
        parts.add(figure("petpet render", "<image src=\"$imageFile\" />"))
        parts.add("</div>")
    }

    fun finish() {
        if (!htmlFile.parentFile.exists()) {
            htmlFile.parentFile.mkdirs()
        }
        htmlFile.writeText(
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Petpet $name Test</title>
                <style>
                    .image-pair {
                        display: flex;
                    }
                    .image-pair>figure {
                        display: inline-block;
                    }
                    .image-pair>figure>img {
                        width: ${outputWidth}px;
                        height: ${outputHeight}px;
                        border: 1px solid black;
                        background-color: #6e6e6e;
                    }
                </style>
            </head>
            <body>
                ${parts.joinToString("\n")}
            </body>
            </html>
        """.trimIndent()
        )
    }

    private fun figure(text: String, imageEleStr: String) =
        "<figure>$imageEleStr<figcaption>$text</figcaption></figure>"

    companion object {
        val outputWidth = 300
        val outputHeight = 300

        val TEST_INPUT_IMAGE: File = File("src/test/resources/test-images/input.png").absoluteFile

        init {
            if (!TEST_INPUT_IMAGE.exists()) {
                throw FileNotFoundException("Test input image not found: $TEST_INPUT_IMAGE")
            }
        }
    }
}