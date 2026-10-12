import moe.dituon.petpet.template.PetpetTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class BomTest {
    /**
     * 验证以 UTF-8 with BOM 保存的模板文件可以被正常加载 (例如 ganma 模板)
     */
    @Test
    fun testFromJsonFileWithBom(@TempDir tempDir: Path) {
        val json = """
            {
              "type": "image",
              "elements": []
            }
        """.trimIndent()
        val templateFile = tempDir.resolve("template.json").toFile()
        templateFile.writeBytes(
            byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + json.toByteArray(StandardCharsets.UTF_8)
        )

        val template = PetpetTemplate.fromJsonFile(templateFile)
        assertEquals(tempDir.toFile(), template.basePath)
    }
}
