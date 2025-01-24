package moe.dituon.petpet.bot.qq.onebot.handler

import cn.evolvefield.onebot.sdk.event.message.MessageEvent
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import moe.dituon.petpet.bot.BotSendEvent
import moe.dituon.petpet.bot.qq.handler.QQMessageEventHandler
import moe.dituon.petpet.bot.qq.onebot.OnebotBotService
import moe.dituon.petpet.bot.qq.onebot.ScriptOnebotSendEvent
import moe.dituon.petpet.bot.qq.onebot.globalBotInstance
import moe.dituon.petpet.bot.qq.onebot.sendPrivateForwardMsg
import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.utils.image.EncodedImage
import moe.dituon.petpet.script.PetpetScriptModel

open class OnebotMessageHandler(
    val onebotBotService: OnebotBotService
) : QQMessageEventHandler(onebotBotService) {

    fun handle(event: MessageEvent) {
        if (event.userId == globalBotInstance.id) return
        val handler = OnebotMessageContext(event)
        handler.handleCommand()
    }

    protected open inner class OnebotMessageContext : MessageContext {
        private val messageEvent: MessageEvent
        private val messageEventObject: JsonObject

        constructor(event: MessageEvent) : this(OnebotMessageChainWrapper(onebotBotService, event))

        constructor(message: OnebotMessageChainWrapper) : super() {
            this.messageEvent = message.event
            this.messageEventObject = message.eventObject
            this.message = message
            this.messageText = message.contentText
            this.permission = onebotBotService.getPermission(message.subjectId)
        }

        override fun senderHasGroupPermission(): Boolean = false

        override fun buildBotSendEvent(script: PetpetScriptModel, context: RequestContext): BotSendEvent {
            return ScriptOnebotSendEvent(onebotBotService, messageEvent, context, script.basePath)
        }

        override fun getBotName(): String = runBlocking {
            globalBotInstance.getLoginInfo().data?.nickname ?: "Bot"
        }

        override fun getBotId(): String {
            return globalBotInstance.id.toString()
        }

        override fun getSenderName(): String {
            val senderObject = messageEventObject["sender"].asJsonObject
            val card = senderObject["card"]?.asString
            val nickname = senderObject["nickname"]?.asString
            return if (!card.isNullOrBlank()) card else nickname ?: ""
        }

        override fun getSenderId(): String {
            return userId.toString()
        }

        private val userId: Long
            get() = messageEventObject["user_id"].asLong

        override fun getSubjectName(): String {
            return senderName
        }

        override fun getSubjectId(): String {
            return senderId
        }

        override fun replyMessage(text: String): Unit = runBlocking {
            globalBotInstance.sendPrivateMsg(userId, text, true)
        }

        override fun replyMessage(image: EncodedImage): Unit = runBlocking {
            globalBotInstance.sendPrivateMsg(
                userId,
                "[{\"type\": \"image\",\"data\": {\"file\": \"${onebotBotService.putImage(image)}\"}}]",
                true
            )
        }

        override fun replyMessage(e: BotSendEvent): Unit = runBlocking {
            val msgs = (e as ScriptOnebotSendEvent).responseMessage ?: return@runBlocking
            if (e.isResponseInForward) {
                globalBotInstance.sendPrivateForwardMsg(
                    userId,
                    "[${
                        msgs.joinToString(",") {
                            """{
                                "type": "node",
                                "data": {
                                "user_id": "${globalBotInstance.id}",
                                "nickname": "Petpet!",
                                "content": $it
                                }
                            }""".trimIndent()
                        }
                    }]"
                )
                return@runBlocking
            }
            msgs.forEach { msg ->
                globalBotInstance.sendPrivateMsg(userId, msg, true)
            }
        }

        /**
         * 缓存 Bot 自己发送的图片, 部分 Onebot 实现没有 message_sent 事件, 在发送时进行缓存
         */
        protected fun cacheSelfImage() {
            // TODO
        }

        override fun inGroupContext(): Boolean {
            return false
        }
    }
}
