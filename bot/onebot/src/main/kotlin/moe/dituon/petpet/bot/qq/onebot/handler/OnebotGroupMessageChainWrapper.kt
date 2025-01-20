package moe.dituon.petpet.bot.qq.onebot.handler

import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent
import kotlinx.coroutines.runBlocking
import moe.dituon.petpet.bot.qq.handler.QQMessageElement
import moe.dituon.petpet.bot.qq.onebot.OnebotBotService
import moe.dituon.petpet.bot.qq.onebot.getMemberName

class OnebotGroupMessageChainWrapper(
    service: OnebotBotService,
    messageEvent: GroupMessageEvent,
    private val groupId: Long
) : OnebotMessageChainWrapper(service, messageEvent) {

    private inner class MiraiMessageAtElement(private val id: Long) : QQMessageElement.AtElement() {
        override fun getTargetId(): String = id.toString()

        override fun getTargetName(): String = runBlocking {
            // TODO: suspend
            getMemberName(groupId, id)
        }
    }

    override fun wrapAtElement(id: Long): QQMessageElement.AtElement {
        return MiraiMessageAtElement(id)
    }
}
