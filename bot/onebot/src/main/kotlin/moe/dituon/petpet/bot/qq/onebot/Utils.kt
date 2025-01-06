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

val banner = """${"\u001b[35m"}

    ██████╗ ███████╗████████╗██████╗ ███████╗████████╗
    ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔════╝╚══██╔══╝
    ██████╔╝█████╗     ██║   ██████╔╝█████╗     ██║   
    ██╔═══╝ ██╔══╝     ██║   ██╔═══╝ ██╔══╝     ██║   
    ██║     ███████╗   ██║   ██║     ███████╗   ██║   
    ╚═╝     ╚══════╝   ╚═╝   ╚═╝     ╚══════╝   ╚═╝   v${BaseService.VERSION}
"""
