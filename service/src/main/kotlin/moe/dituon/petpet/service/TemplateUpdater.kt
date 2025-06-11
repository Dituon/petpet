package moe.dituon.petpet.service

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import moe.dituon.petpet.core.FontManager
import moe.dituon.petpet.core.GlobalContext
import moe.dituon.petpet.core.element.PetpetModel
import moe.dituon.petpet.core.utils.io.FileMD5Utils
import moe.dituon.petpet.uitls.GlobalJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Font
import java.io.File
import java.net.URI
import java.net.URL
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class TemplateUpdater(
    val config: TemplateUpdaterConfig,
    val service: UpdatableBaseService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: Logger = LoggerFactory.getLogger(TemplateUpdater::class.java),
) {
    constructor(
        config: TemplateUpdaterConfig,
        service: UpdatableBaseService
    ) : this(
        config = config,
        service = service,
        logger = LoggerFactory.getLogger(TemplateUpdater::class.java),
    )

    constructor(
        config: TemplateUpdaterConfig,
        service: UpdatableBaseService,
        logger: Logger
    ) : this(
        config = config,
        service = service,
        logger = logger,
        dispatcher = Dispatchers.IO
    )

    companion object {
        const val INDEX_FILE_NAME = "petpet-index.json"
    }

    private val downloadDispatcher = Executors.newFixedThreadPool(config.downloadThreadCount).asCoroutineDispatcher()
    private var indexUrlList = config.repositoryUrls.map { URI("$it/").resolve(INDEX_FILE_NAME).toURL() }
    private var templateSavePath: File? = null
    private var fontSavePath: File? = null

    /**
     * @return 更新成功为 true, 无需更新为 false
     */
    fun startUpdate(): Boolean = runBlocking {
        if (!config.enabled) {
            logger.info("Petpet 更新检查已禁用")
            return@runBlocking false
        }
        val list = checkUpdateResource(service.staticModelMap)
        return@runBlocking if (list.isEmpty()) {
            logger.info("Petpet 资源更新检查完毕, 无新增资源")
            false
        } else {
            logger.info(
                """Petpet 共有 ${list.size} 个文件更新, 正在开始下载...
                |> 新增模板保存路径: ${templateSavePath ?: "N/A"}
                |> 新增字体保存路径: ${fontSavePath ?: "N/A"}
            """.trimMargin()
            )
            val elapsed = measureTimeMillis {
                downloadResources(list)
            }
            logger.info("Petpet 资源更新完毕, 共耗时 ${formatMillis(elapsed)}")
            true
        }
    }

    private suspend fun checkUpdateResource(
        templateMap: Map<String, PetpetModel>,
        fontManager: FontManager = GlobalContext.getInstance().fontManager
    ): Map<String, List<Pair<File, URL>>> = coroutineScope {
        val (templateIndex, fontsIndex) = downloadIndexedList()
        val (templateResource, dependentFonts) = checkTemplateUpdate(templateMap, templateIndex)
        val fontResource = checkFontsUpdate(fontManager, fontsIndex.filter { it.key in dependentFonts })
        templateResource.toMutableMap().apply {
            putAll(fontResource)
        }
    }

    /**
     * 检查模板更新
     * @return Pair<Map<模板ID, 资源列表>, 依赖字体ID>
     */
    private suspend fun checkTemplateUpdate(
        templateMap: Map<String, PetpetModel>,
        templateIndex: Map<String, UpdateIndexTemplateElement>
    ): Pair<Map<String, List<Pair<File, URL>>>, Set<String>> = coroutineScope {
        val deprecatedFontSet = mutableSetOf<String>()
        templateSavePath = if (config.updateTemplateSavePath == null) {
            service.localTemplateDirectory
        } else {
            File(config.updateTemplateSavePath)
        }
        val updateHintList: MutableList<String> = mutableListOf()
        val deferredResults = templateIndex.map { (key, remote) ->
            async(dispatcher) {
                val local = templateMap[key]
                    ?.takeIf { it.directory != null && remote.source != null }
                    ?: return@async if (templateSavePath == null || remote.source == null) {
                        null
                    } else {
                        deprecatedFontSet.addAll(remote.dependentFonts)
                        key to remote.resource.map {
                            templateSavePath!!.resolve(key).resolve(it.key) to remote.source!!.toURI().resolve(it.key)
                                .toURL()
                        }
                    }

                if (config.updateMode == UpdateMode.NEW_ONLY_UPDATE) return@async null

                deprecatedFontSet.addAll(remote.dependentFonts)
                val diffs = compareRemoteTemplate(local, remote)
                if (diffs.isEmpty()) return@async null

                val baseUri = remote.source!!.toURI()
                if (config.updateMode == UpdateMode.FULL_CHECK) {
                    updateHintList.add("$key (${diffs.size} files changed)")
                    null
                } else {
                    key to diffs.map { relativePath ->
                        local.directory!!.resolve(relativePath) to baseUri.resolve(relativePath).toURL()
                    }
                }
            }
        }
        val result = deferredResults.awaitAll().filterNotNull().toMap() to deprecatedFontSet
        if (updateHintList.isNotEmpty()) {
            logger.info(
                """${"\n"}
                以下模板未与远程仓库同步:
                ${updateHintList.joinToString("\n")}
                删除模板以进行手动同步, 亦可更改配置项中的更新策略以自动同步或跳过本项检查
            """.trimIndent()
            )
        }
        result
    }

    private suspend fun checkFontsUpdate(
        fontsManager: FontManager,
        fontsIndex: Map<String, UpdateIndexFontElement>
    ): Map<String, List<Pair<File, URL>>> = coroutineScope {
        fontSavePath = if (config.updateFontSavePath == null) {
            fontsManager.localFontDirectory
        } else {
            File(config.updateFontSavePath)
        }
        val localFonts: Map<Font, File> = fontsManager.localFontMap

        val localMd5ToFile: Map<String, File> = localFonts.values.associateBy {
            FileMD5Utils.calculateMD5(it)
        }

        val deferredResults = fontsIndex.mapNotNull { (fontId, remoteFont) ->
            val sourceUrl = remoteFont.source ?: return@mapNotNull null  // 跳过无 source 条目

            async(dispatcher) {
                val localFile = localMd5ToFile[remoteFont.md5]
                if (localFile == null && !fontsManager.isFontAvailable(remoteFont.name)) {
                    if (localFonts.values.any { it.name.equals(fontId, ignoreCase = true) }) {
                        logger.warn("字体 $fontId 已存在但与远程仓库不同, 可能需要重启 JVM 以彻底应用更新")
                    }
                    val file = fontSavePath!!.resolve(fontId)
                    fontId to listOf(file to sourceUrl)
                } else {
                    null
                }
            }
        }

        deferredResults.awaitAll().filterNotNull().toMap()
    }


    private fun compareRemoteTemplate(local: PetpetModel, remote: UpdateIndexTemplateElement): Set<String> {
        return remote.resource.filter { (fileName, remoteMd5) ->
            val localMd5 = local.resourceMD5Map[fileName]
            localMd5 == null || localMd5 != remoteMd5
        }.keys
    }

    private suspend fun downloadIndexedList(): Pair<
            Map<String, UpdateIndexTemplateElement>,
            Map<String, UpdateIndexFontElement>
            > = coroutineScope {
        val indexList = indexUrlList.map { url ->
            async(downloadDispatcher) {
                try {
                    downloadIndex(url).apply {
                        templates.map {
                            it.value.source = url.toURI().resolve("$templatesPath/${it.key}/").toURL()
                        }
                        fonts.map { it.value.source = url.toURI().resolve("$fontsPath/${it.key}").toURL() }
                    }
                } catch (e: Exception) {
                    logger.warn("无法下载远程仓库索引: $url (${e.message})")
                    null
                }
            }
        }.awaitAll().filterNotNull()

        if (!indexList.isEmpty()) {
            logger.info("Petpet 已成功读取 ${indexList.size} 远程仓库索引, 开始检查本地资源...")
        }
        val templatesIndex = mutableMapOf<String, UpdateIndexTemplateElement>()
        val fontsIndex = mutableMapOf<String, UpdateIndexFontElement>()
        // 反向合并索引以确保优先级
        indexList.asReversed().forEach {
            templatesIndex.putAll(it.templates)
            fontsIndex.putAll(it.fonts)
        }
        templatesIndex to fontsIndex
    }

    private suspend fun downloadIndex(repoUrl: URL): UpdateIndex {
        val indexFileUrl = repoUrl.toURI().resolve(INDEX_FILE_NAME).toURL()
        return GlobalJson.decodeFromString(
            UpdateIndex.serializer(),
            withContext(downloadDispatcher) { indexFileUrl.readText() }
        )
    }

    private suspend fun downloadResources(
        resources: Map<String, List<Pair<File, URL>>>,
    ) = supervisorScope {
        val semaphore = Semaphore(config.downloadThreadCount)

        val groupJobs = resources.map { (id, fileUrlList) ->
            launch {
                try {
                    coroutineScope {
                        var time: Long = 0
                        val fileJobs = fileUrlList.map { (file, url) ->
                            async(dispatcher) {
                                semaphore.withPermit {
                                    time = System.currentTimeMillis()
                                    downloadFile(url, file)
                                }
                            }
                        }
                        fileJobs.awaitAll()
                        logger.info(
                            "$id 下载完成 (共 ${fileUrlList.size} 个文件, 耗时 ${
                                formatMillis(System.currentTimeMillis() - time)
                            })"
                        )
                    }
                } catch (e: Exception) {
                    logger.warn("$id 下载失败, 任务已取消：${e.message}", e)
                }
            }
        }

        groupJobs.joinAll()
    }


    private fun formatMillis(ms: Long): String {
        val seconds = ms / 1000
        val millis = ms % 1000
        return if (seconds > 0) "${seconds}s ${millis}ms" else "${millis}ms"
    }

    private fun downloadFile(url: URL, outputFile: File) {
        try {
            url.openStream().use { input ->
                outputFile.parentFile?.mkdirs()
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("${e.message}: $url -> ${outputFile.path}", e)
        }
    }
}