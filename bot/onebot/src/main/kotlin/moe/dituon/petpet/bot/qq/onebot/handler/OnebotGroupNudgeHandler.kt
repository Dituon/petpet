package moe.dituon.petpet.bot.qq.onebot.handler

import cn.evolvefield.onebot.sdk.event.notice.NotifyNoticeEvent
import kotlinx.coroutines.runBlocking
import moe.dituon.petpet.bot.BotSendEvent
import moe.dituon.petpet.bot.qq.handler.QQNudgeEventHandler
import moe.dituon.petpet.bot.qq.onebot.OnebotBotService
import moe.dituon.petpet.bot.qq.onebot.ScriptOnebotSendEvent
import moe.dituon.petpet.bot.qq.onebot.globalBotInstance
import moe.dituon.petpet.bot.qq.onebot.sendGroupForwardMsg
import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.utils.image.EncodedImage
import moe.dituon.petpet.script.PetpetScriptModel

class OnebotGroupNudgeHandler(
    val botService: OnebotBotService
) : QQNudgeEventHandler(botService) {
    fun handle(event: NotifyNoticeEvent) {
        if (event.subType != "poke" || event.groupId == 0L) return
        OnebotGroupNudgeContext(event).handleNudge()
    }

    inner class OnebotGroupNudgeContext(
        private val event: NotifyNoticeEvent
    ) : QQNudgeEventHandler.NudgeContext() {
        init {
            permission = service.getPermission(event.groupId)
        }

        override fun senderHasGroupPermission() = false

        override fun buildBotSendEvent(script: PetpetScriptModel?, context: RequestContext?) =
            ScriptOnebotSendEvent(
                botService,
                true,
                0,
                context,
                script?.basePath
            )

        override fun getBotName() = runBlocking {
            globalBotInstance.getLoginInfo().data?.nickname ?: "Bot"
        }

        override fun getBotId() = globalBotInstance.id.toString()

        override fun getSenderName() = runBlocking {
            globalBotInstance.getGroupMemberInfo(event.groupId, event.realOperatorId, false)
                .data?.nickname ?: "Sender"
        }

        override fun getSenderId() = event.realOperatorId.toString()

        override fun getSubjectName() = runBlocking {
            globalBotInstance.getGroupInfo(event.groupId, false).data?.groupName ?: "Group"
        }

        override fun getSubjectId() = event.groupId.toString()

        override fun replyMessage(text: String?): Unit = runBlocking {
            globalBotInstance.sendGroupMsg(event.groupId, text ?: return@runBlocking, true)
        }

        override fun replyMessage(image: EncodedImage): Unit = runBlocking {
            globalBotInstance.sendGroupMsg(
                event.groupId,
                "[{\"type\": \"image\",\"data\": {\"file\": \"${botService.putImage(image)}\"}}]",
                true
            )
        }

        override fun replyMessage(e: BotSendEvent): Unit = runBlocking {
            val msgs = (e as ScriptOnebotSendEvent).responseMessage ?: return@runBlocking
            if (e.isResponseInForward) {
                globalBotInstance.sendGroupForwardMsg(
                    event.groupId,
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
                globalBotInstance.sendGroupMsg(event.groupId, msg, true)
            }
        }

        override fun getTargetId() = event.targetId.toString()

        override fun getTargetName() = runBlocking {
            globalBotInstance.getGroupMemberInfo(event.groupId, event.targetId, false)
                .data?.nickname ?: "Target"
        }

        override fun inGroupContext(): Boolean {
            return true
        }
    }
}