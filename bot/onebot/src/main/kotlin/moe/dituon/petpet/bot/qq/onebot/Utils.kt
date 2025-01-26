package moe.dituon.petpet.bot.qq.onebot

import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.sdk.action.ActionData
import cn.evolvefield.onebot.sdk.entity.MsgId
import cn.evolvefield.onebot.sdk.enums.ActionPathEnum
import cn.evolvefield.onebot.sdk.util.withToken
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import moe.dituon.petpet.service.BaseService

fun encodeJsonString(str: String) = Json.encodeToString(String.serializer(), str)

suspend fun Bot.sendGroupForwardMsg(groupId: Long, msg: String): ActionData<MsgId> {
    val action = ActionPathEnum.SEND_GROUP_FORWARD_MSG
    val params = JsonObject()
    params.addProperty("group_id", groupId)
    params.add("messages", JsonParser.parseString(msg))

    val result = actionHandler.action(this, action, params)
    return result.withToken()
}

suspend fun Bot.sendPrivateForwardMsg(userId: Long, msg: String): ActionData<MsgId> {
    val action = ActionPathEnum.SEND_PRIVATE_FORWARD_MSG
    val params = JsonObject()
    params.addProperty("user_id", userId)
    params.add("messages", JsonParser.parseString(msg))

    val result = actionHandler.action(this, action, params)
    return result.withToken()
}

suspend fun getMemberName(groupId: Long, userId: Long): String {
    val data = globalBotInstance.getGroupMemberInfo(groupId, userId, false).data ?: return "Member"
    return data.card.ifEmpty {
        data.nickname
    }
}

suspend fun sendNudge(userId: Long) {
    when (globalBotInstance.appName.lowercase()) {
        "llonebot", "napcat" -> {
            // 等待 overflow onebot 1.0.3 版本新增的 extPoke 系列方法
            return
        }
    }
    val msg = "[{\"type\":\"poke\",\"data\":{\"id\":${userId}}}]"
    globalBotInstance.sendPrivateMsg(userId, msg, false)
}

suspend fun sendNudge(groupId: Long, userId: Long) {
    when (globalBotInstance.appName.lowercase()) {
        "llonebot", "napcat" -> {
            // 等待 overflow onebot 1.0.3 版本新增的 extPoke 系列方法
            return
        }
    }
    val msg = "[{\"type\":\"poke\",\"data\":{\"id\":${userId}}}]"
    globalBotInstance.sendGroupMsg(groupId, msg, false)
}

const val banner = """${"\u001b[35m"}

    ██████╗ ███████╗████████╗██████╗ ███████╗████████╗
    ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔════╝╚══██╔══╝
    ██████╔╝█████╗     ██║   ██████╔╝█████╗     ██║   
    ██╔═══╝ ██╔══╝     ██║   ██╔═══╝ ██╔══╝     ██║   
    ██║     ███████╗   ██║   ██║     ███████╗   ██║   
    ╚═╝     ╚══════╝   ╚═╝   ╚═╝     ╚══════╝   ╚═╝   v${BaseService.VERSION}
"""
