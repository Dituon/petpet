package moe.dituon.petpet.bot.qq.onebot.handler

import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.dituon.petpet.bot.BotSendEvent
import moe.dituon.petpet.bot.qq.onebot.OnebotBotService
import moe.dituon.petpet.bot.qq.onebot.ScriptOnebotSendEvent
import moe.dituon.petpet.bot.qq.onebot.globalBotInstance
import moe.dituon.petpet.bot.qq.onebot.sendGroupForwardMsg
import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.utils.image.EncodedImage
import moe.dituon.petpet.script.PetpetScriptModel

class OnebotGroupMessageHandler(
    val onebotService: OnebotBotService
) : OnebotMessageHandler(onebotService) {

    fun handle(event: GroupMessageEvent) {
        if (event.userId == globalBotInstance.id) return
        OnebotGroupMessageContext(event).handleCommand()
    }

    private inner class OnebotGroupMessageContext(
        private val groupMessageEvent: GroupMessageEvent
    ) : OnebotMessageHandler.OnebotMessageContext(
        OnebotGroupMessageChainWrapper(onebotService, groupMessageEvent, groupMessageEvent.groupId)
    ) {
        override fun senderHasGroupPermission(): Boolean {
            return groupMessageEvent.sender?.role != "member"
        }

        override fun getSubjectName(): String {
            return runBlocking {
                globalBotInstance.getGroupInfo(groupMessageEvent.groupId, false)
                    .data?.groupName ?: ""
            }
        }

        override fun getSubjectId(): String {
            return groupMessageEvent.groupId.toString()
        }

        override fun replyMessage(text: String) {
            val message = """
                [
                    {"type": "reply", "data": {"id": "${groupMessageEvent.messageId}"}},
                    {"type": "text", "data": {"text": ${Json.encodeToString(text)}}}
                ]
            """.trimIndent()
            runBlocking {
                globalBotInstance.sendGroupMsg(groupMessageEvent.groupId, message, true)
            }
        }

        override fun replyMessage(image: EncodedImage) {
            val imageUrl = onebotService.putImage(image)
            val message = """
                [
                    {"type": "reply", "data": {"id": "${groupMessageEvent.messageId}"}},
                    {"type": "image", "data": {"file": "$imageUrl"}}
                ]
            """.trimIndent()
            runBlocking {
                globalBotInstance.sendGroupMsg(groupMessageEvent.groupId, message, true)
            }
        }

        override fun buildBotSendEvent(
            script: PetpetScriptModel,
            context: RequestContext
        ): BotSendEvent {
            return ScriptOnebotSendEvent(onebotService, groupMessageEvent, context, script.basePath)
        }

        override fun replyMessage(e: BotSendEvent) {
            val msgs = (e as ScriptOnebotSendEvent).responseMessage ?: return
            runBlocking {
                if (e.isResponseInForward) {
                    val forwardMessage = msgs.joinToString(",") {
                        """
                        {
                            "type": "node",
                            "data": {
                                "user_id": "${globalBotInstance.id}",
                                "nickname": "Petpet!",
                                "content": $it
                            }
                        }
                    """.trimIndent()
                    }
                    globalBotInstance.sendGroupForwardMsg(groupMessageEvent.groupId, "[$forwardMessage]")
                } else {
                    msgs.forEach { msg ->
                        globalBotInstance.sendGroupMsg(groupMessageEvent.groupId, msg, true)
                    }
                }
            }
        }

        override fun inGroupContext(): Boolean {
            return true
        }
    }
}
