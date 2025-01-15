package moe.dituon.petpet.service

import moe.dituon.petpet.core.GlobalContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EnvironmentChecker {
    companion object {
        @JvmStatic
        val log: Logger = LoggerFactory.getLogger("Petpet")
        val infoHead =  "=============== Petpet 环境检查 ==============="
        val infoSplit = "=============================================="

        @JvmStatic
        fun check() {
            val infos = ArrayList<String>(4)
            val vendor = System.getProperty("java.vendor")
            if (vendor == null || !vendor.contains("JetBrains")) {
                infos.add(
                    """
                    推荐使用 JetBrainsRuntime JVM 运行 Petpet:
                    https://github.com/JetBrains/JetBrainsRuntime

                    使用 $vendor 提供的 JVM 可能不支持以下功能:
                    - 彩色 Emoji 渲染
                    - 全局字体回退
                    - 基于 jcef 的 HTML 渲染
                    """.trimIndent())
            }
            val unsupportedLanguages = GlobalContext.getInstance().fontManager
                .supportedLanguageFontMap.filter { it.value.isEmpty() }.keys
            if (unsupportedLanguages.isNotEmpty()) {
                val fonts = unsupportedLanguages.map {
                    "- ${it.name} (${it.desc})"
                }
                infos.add(
                    """
                    无法找到支持以下语言的字体:
                    ${fonts.joinToString("\n")}

                    请在系统环境中安装字体, 或在 Petpet 字体目录中添加字体文件;
                    未安装字体可能导致无法渲染对应语言的文本
                    """.trimIndent()
                )
            }

            if (infos.isNotEmpty()) {
                log.info("\n$infoHead\n${infos.joinToString("\n$infoSplit\n")}\n$infoSplit")
            }
        }
    }
}