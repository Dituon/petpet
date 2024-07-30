package moe.dituon.petpet.share.template

import java.io.File

data class ResultImage(
    val blob: ByteArray,
    val width: Int,
    val height: Int,
    val suffix: String,
    val mime: String,
) {
    fun saveAs(filePath: String) {
        val file = File(filePath)
        file.parentFile?.mkdirs()
        file.writeBytes(blob)
    }
}