import moe.dituon.petpet.bot.BotSendEvent
import moe.dituon.petpet.bot.qq.QQBotConfig
import moe.dituon.petpet.bot.qq.QQBotService
import moe.dituon.petpet.bot.qq.avatar.QQAvatarRequester
import moe.dituon.petpet.bot.qq.handler.QQMessageChain
import moe.dituon.petpet.bot.qq.handler.QQMessageElement.AtElement
import moe.dituon.petpet.bot.qq.handler.QQMessageElement.ImageElement
import moe.dituon.petpet.bot.qq.handler.QQMessageElement.TextElement
import moe.dituon.petpet.bot.qq.handler.QQMessageEventHandler
import moe.dituon.petpet.bot.qq.permission.ContactPermission
import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.element.PetpetModel
import moe.dituon.petpet.core.utils.image.EncodedImage
import moe.dituon.petpet.script.PetpetScriptModel
import org.junit.jupiter.api.Test
import java.nio.file.Path

val testResourcePath: Path = Path.of("src/test/resources")

class QQMessageEventHandlerTest {
    private val command = "pet"
    private val commandHead = "#"
    private val handler = TestHandler(
        QQBotConfig(
            command,
            commandHead,
        )
    ).apply {
        service.addTemplates(testResourcePath.resolve("test-templates").toFile())
    }
    private val defaultTemplate = handler.service.defaultTemplate

    @Test
    fun testDefault() {
        handler.handle("other")
        assert(handler.replyTemplate == null && handler.replyText == null)
        handler.handle("pet")
        assert(handler.replyTemplate == defaultTemplate)
    }

    @Test
    fun testPermission() {
        handler.senderHasGroupPermission = true
        handler.handle("pet off")
        assert(
            handler.replyText?.startsWith(
                "$command ${String.format(ContactPermission.DISABLE_MESSAGE, "")}"
            ) ?: false
        )

        handler.senderHasGroupPermission = false
        handler.handle("pet off")
        // 无权限不返回模板或提示消息
        assert(handler.replyTemplate == null && handler.replyText == null)
    }

    @Test
    fun testTemplate() {
        handler.handle("#alia0")
        assert(handler.replyTemplate == handler.service.getTemplate("alia0"))

        handler.handle("pet alia1")
        assert(handler.replyTemplate == handler.service.getTemplate("alia1"))

        handler.handle("alia0")
        assert(handler.replyTemplate == null)

        handler.handle("pet other")
        assert(handler.replyTemplate == defaultTemplate)
    }

    @Test
    fun testRequest() {
        val imageName = QQMessageChain.DEFAULT_IMAGE_NAME
        val atTarget0 = AtElement.from("10010", "atTarget0")
        val atTarget1 = AtElement.from("10011", "atTarget1")
        val imageTarget0 = ImageElement("https://picsum.photos/seed/0/200")
        val imageTarget1 = ImageElement("https://picsum.photos/seed/1/200")
        val replyImageSrc = "https://picsum.photos/seed/100/200"

        // pet @target0
        val petAt = handler.newTestMessageContext(
            QQMessageChain(
                null,
                "pet",
                listOf(
                    TextElement("pet"),
                    atTarget0
                )
            )
        )
        handler.handle(petAt)   // from: sender, to: target0
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.TO_KEY) == atTarget0.targetName)
        assert(handler.isAvatarEqual(QQMessageEventHandler.TO_KEY, atTarget0.targetId))
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.FROM_KEY) == petAt.senderName)
        assert(handler.isAvatarEqual(QQMessageEventHandler.FROM_KEY, petAt.senderId))

        // pet @target0 @target1
        val petAtAt = handler.newTestMessageContext(
            QQMessageChain(
                null,
                "pet",
                listOf(
                    TextElement("pet"),
                    atTarget0,
                    atTarget1
                )
            )
        )
        handler.handle(petAtAt) // from: target0, to: target1
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.TO_KEY) == atTarget1.targetName)
        assert(handler.isAvatarEqual(QQMessageEventHandler.TO_KEY, atTarget1.targetId))
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.FROM_KEY) == atTarget0.targetName)
        assert(handler.isAvatarEqual(QQMessageEventHandler.FROM_KEY, atTarget0.targetId))

        // pet [image0]
        val petImage = handler.newTestMessageContext(
            QQMessageChain(
                null,
                "pet",
                listOf(
                    TextElement("pet"),
                    imageTarget0
                )
            )
        )
        handler.handle(petImage) // from: sender, to: image0
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.TO_KEY) == imageName)
        assert(handler.requestContext?.imageResourceMap?.get(QQMessageEventHandler.TO_KEY)?.src == imageTarget0.url)
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.FROM_KEY) == petImage.senderName)
        assert(handler.isAvatarEqual(QQMessageEventHandler.FROM_KEY, petImage.senderId))

        // pet @target0 [image0] @target1 [image1]
        val petAtImageAtImage = handler.newTestMessageContext(
            QQMessageChain(
                null,
                "pet",
                listOf(
                    TextElement("pet"),
                    atTarget0,
                    imageTarget0,
                    atTarget1,
                    imageTarget1
                )
            )
        )
        handler.handle(petAtImageAtImage)   // from: target1, to: image1
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.TO_KEY) == imageName)
        assert(handler.requestContext?.imageResourceMap?.get(QQMessageEventHandler.TO_KEY)?.src == imageTarget1.url)
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.FROM_KEY) == atTarget1.targetName)
        assert(handler.isAvatarEqual(QQMessageEventHandler.FROM_KEY, atTarget1.targetId))

        // [reply] pet @target0
        val petReplyAt = handler.newTestMessageContext(
            QQMessageChain(
                replyImageSrc,
                "pet",
                listOf(
                    TextElement("pet"),
                    atTarget0
                )
            )
        )
        handler.handle(petReplyAt)  // from: target0, to: replyImage
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.TO_KEY) == imageName)
        assert(handler.requestContext?.imageResourceMap?.get(QQMessageEventHandler.TO_KEY)?.src == replyImageSrc)
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.FROM_KEY) == atTarget0.targetName)
        assert(handler.isAvatarEqual(QQMessageEventHandler.FROM_KEY, atTarget0.targetId))

        // [reply] pet [image0] @target0
        val petReplyImageAt = handler.newTestMessageContext(
            QQMessageChain(
                replyImageSrc,
                "pet",
                listOf(
                    TextElement("pet"),
                    imageTarget0,
                    atTarget0
                )
            )
        )
        handler.handle(petReplyImageAt) // from: target0, to: replyImage
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.TO_KEY) == imageName)
        assert(handler.requestContext?.imageResourceMap?.get(QQMessageEventHandler.TO_KEY)?.src == replyImageSrc)
        assert(handler.requestContext?.textDataMap?.get(QQMessageEventHandler.FROM_KEY) == atTarget0.targetName)
        assert(handler.isAvatarEqual(QQMessageEventHandler.FROM_KEY, atTarget0.targetId))
    }

    @Test
    fun testTextSplit() {
        handler.handle("pet other texts")
        assert(handler.messageContext?.getRawMessageText() == "other texts")

        handler.handle("pet alia0 other   texts")
        assert(handler.messageContext?.getRawMessageText() == "other   texts")

        handler.handle("#alia0 other \n texts")
        assert(handler.messageContext?.getRawMessageText() == "other \n texts")
    }
}

class TestHandler(
    private val config: QQBotConfig,
    var senderHasGroupPermission: Boolean = true,

    val service: QQBotService = QQBotService(config).apply {
        permissionConfigPath = testResourcePath.resolve("test-permission-config")
    }
) : QQMessageEventHandler(service) {
    var replyText: String? = null
    var replyTemplate: PetpetModel? = null
    var requestContext: RequestContext? = null
    var messageContext: TestMessageContext? = null

    fun handle(text: String) {
        cleanVars()
        messageContext = TestMessageContext(QQMessageChain.fromText(text))
        handle(messageContext)
    }

    fun handle(testMessageContext: TestMessageContext) {
        cleanVars()
        messageContext = testMessageContext
        super.handle(testMessageContext)
    }

    private fun cleanVars() {
        replyText = null
        replyTemplate = null
        requestContext = null
        messageContext = null
    }

    fun isAvatarEqual(key: String, id: String, size: Int = 100) =
        requestContext?.imageResourceMap?.get(key)?.src ==
                QQAvatarRequester.getAvatarUrlString(id, size)

    fun newTestMessageContext(
        messageChain: QQMessageChain
    ) = TestMessageContext(messageChain)

    inner class TestMessageContext(
        messageChain: QQMessageChain
    ) : QQMessageEventHandler.MessageContext(messageChain) {
        public override fun senderHasGroupPermission() = senderHasGroupPermission

        override fun buildBotSendEvent(script: PetpetScriptModel?, context: RequestContext?): BotSendEvent {
            TODO("Not yet implemented")
        }

        public override fun getBotName() = "botName"

        public override fun getBotId() = "10001"

        public override fun getSenderName() = "senderName"

        public override fun getSenderId() = "10002"

        public override fun getSubjectName() = "subjectName"

        public override fun getSubjectId() = "10003"

        fun getRawMessageText(): String = rawMessageText

        override fun replyMessage(text: String?) {
            replyText = text
        }

        override fun responseTemplate() {
            replyTemplate = template
            requestContext = buildRequestContext()
        }

        override fun replyMessage(image: EncodedImage?) {
        }

        override fun replyMessage(event: BotSendEvent?) {
        }
    }
}