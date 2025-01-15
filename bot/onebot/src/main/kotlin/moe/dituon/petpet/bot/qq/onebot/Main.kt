package moe.dituon.petpet.bot.qq.onebot

import cn.evolvefield.onebot.client.connection.ConnectFactory
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent
import moe.dituon.petpet.service.EnvironmentChecker
import net.mamoe.yamlkt.Yaml
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.GraphicsEnvironment
import java.io.File
import kotlin.io.path.Path

lateinit var globalBotInstance: Bot
val log: Logger = LoggerFactory.getLogger("Petpet-Onebot")

val config: OnebotConfig by lazy {
    var config: OnebotConfig? = null
    val configFile = File("onebot.yml")
    if (configFile.exists()) try {
        config = Yaml.decodeFromString(OnebotConfig.serializer(), configFile.readText())
    } catch (t: Throwable) {
        val bak = File(configFile.parentFile, "${configFile.name}.old_${System.currentTimeMillis()}.bak")
        configFile.copyTo(bak, true)
        log.warn("读取配置文件错误，已保存旧文件到 ${bak.name}", t)
    }
    (config ?: OnebotConfig()).apply {
        configFile.writeText(Yaml.encodeToString(OnebotConfig.serializer(), this))
    }
}

private val systemPath = Path(System.getProperty("user.dir")).toFile()

suspend fun main() {
    val bot = ConnectFactory.create(
        config = config.toClientConfig(),
        logger = log
    ).createProducer().awaitNewBotConnection()
    if (bot == null || !bot.channel.isOpen) {
        log.error("无法连接到 Onebot")
        return
    }
    if (bot.onebotVersion == 12) {
        log.error("暂不支持 Onebot v12")
        return
    }
    globalBotInstance = bot
    val service = OnebotBotService(bot, config)
    for (templatePath in config.templatePath) {
        service.addTemplates(systemPath.resolve(templatePath))
    }
    for (fontPath in config.fontPath) {
        service.addFonts(systemPath.resolve(fontPath).toPath())
    }
    val defaultFont = service.updateDefaultFont()
    service.updateScriptService()
    EnvironmentChecker.check()
    log.info(banner)
    log.info("Petpet Onebot 客户端启动成功:")
    log.info("已加载 ${service.staticModelMap.size} 模板; 随机表列包含 ${service.randomIdList.size} 模板;")
    log.info("已注册 ${service.scriptModelMap.size} 脚本; 默认模板为 ${service.defaultTemplateId};")
    log.info(
        "已加载 ${
            GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.size
        } 字体; 默认字体为 ${defaultFont};"
    )
    val handler = GroupMessageEventHandler(service)
    bot.getLoginInfo().data ?: throw IllegalStateException("获取机器人信息失败")
    EventBus.addListener(object : EventListener<GroupMessageEvent> {
        override suspend fun onMessage(e: GroupMessageEvent) {
            log.info(e.rawMessage)
            handler.handle(e)
        }
    })
}
