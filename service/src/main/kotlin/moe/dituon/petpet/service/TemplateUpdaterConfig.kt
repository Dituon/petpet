package moe.dituon.petpet.service

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class TemplateUpdaterConfig(
    val enabled: Boolean = true,
    val repositoryUrls: List<String> = listOf(
        // TODO: 改为独立的文件储存此仓库列表
        "https://raw.githubusercontent.com/Dituon/petpet-templates/main/",
        "https://gh-proxy.com/https://raw.githubusercontent.com/Dituon/petpet-templates/main/",
        "https://gh.d2n.moe/https://raw.githubusercontent.com/Dituon/petpet-templates/main/",
    ),
    val excludeTemplate: Set<String> = emptySet(),
    val downloadThreadCount: Int = 8,
    val updateTemplateSavePath: String? = null,
    val updateFontSavePath: String? = null,
    val updateMode: UpdateMode = UpdateMode.FULL_CHECK,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
enum class UpdateMode {
    /**
     * 检查所有资源并自动更新
     */
    @JsonNames("full_update")
    FULL_UPDATE,

    /**
     * 检查所有资源, 仅更新新增的模板, 已有模板有更新时在日志中记录
     */
    @JsonNames("full_check")
    FULL_CHECK,

    /**
     * 仅更新新增的模板, 不会检查所有资源
     */
    @JsonNames("new_only_update")
    NEW_ONLY_UPDATE
}
