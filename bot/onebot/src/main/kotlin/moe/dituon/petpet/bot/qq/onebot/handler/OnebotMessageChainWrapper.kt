package moe.dituon.petpet.bot.qq.onebot.handler

import cn.evolvefield.onebot.sdk.event.message.MessageEvent
import cn.evolvefield.onebot.sdk.util.CQCode
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import moe.dituon.petpet.bot.qq.handler.QQMessageChainInterface
import moe.dituon.petpet.bot.qq.handler.QQMessageElement
import moe.dituon.petpet.bot.qq.onebot.OnebotBotService

open class OnebotMessageChainWrapper(
    val service: OnebotBotService,
    val event: MessageEvent,
    val eventObject: JsonObject = event.json.asJsonObject,
    val elements: ArrayList<QQMessageElement> = ArrayList()
) : QQMessageChainInterface, MutableList<QQMessageElement> by elements {
    constructor(service: OnebotBotService, messageEvent: MessageEvent) : this(
        service,
        messageEvent,
        messageEvent.json.asJsonObject,
        ArrayList()
    )

    private var replyImage: String? = null
    private var contentText: String = ""
    private var hasTarget = false
    val subjectId = getSubjectId(eventObject)

    companion object {
        private const val TYPE_IMAGE = "image"
        private const val TYPE_TEXT = "text"
        private const val TYPE_AT = "at"
        private const val TYPE_REPLY = "reply"
        private const val FIELD_MESSAGE = "message"
        private const val FIELD_TYPE = "type"
        private const val FIELD_URL = "url"
        private const val FIELD_TEXT = "text"
        private const val FIELD_QQ = "qq"
        private const val FIELD_USER_ID = "user_id"
        private const val FIELD_MESSAGE_ID = "message_id"
        private const val AT_ALL = "all"
    }

    init {
        val msg = eventObject[FIELD_MESSAGE]
        val messages = if (msg is JsonPrimitive) {
            CQCode.toJson(msg.toString())
        } else {
            msg as JsonArray
        }
        val contentBuilder = StringBuilder()
        var firstAtSenderRemoved = false
        var imageCached = false

        messages.forEach { ele ->
            val msgObj = ele.asJsonObject
            val type = msgObj[FIELD_TYPE].asString
            val data = msgObj["data"].asJsonObject

            when (type) {
                TYPE_IMAGE -> {
                    val url = data[FIELD_URL].asString
                    if (!imageCached && service.cacheImage(
                            subjectId,
                            eventObject[FIELD_MESSAGE_ID].asInt,
                            url
                        )
                    ) {
                        imageCached = true
                    }
                    hasTarget = true
                    elements.add(QQMessageElement.ImageElement(url))
                }

                TYPE_TEXT -> {
                    val text = data[FIELD_TEXT].asString
                    contentBuilder.append(' ').append(text)
                    elements.add(QQMessageElement.TextElement(text))
                }

                TYPE_AT -> {
                    val atTarget = data[FIELD_QQ].asString
                    if (atTarget == AT_ALL) return@forEach

                    if (!firstAtSenderRemoved && eventObject[FIELD_USER_ID].asString == atTarget) {
                        firstAtSenderRemoved = true
                        return@forEach
                    }
                    elements.add(wrapAtElement(atTarget.toLong()))
                    hasTarget = true
                }

                TYPE_REPLY -> {
                    replyImage = service.getCachedImage(subjectId, data["id"].asInt)
                    hasTarget = true
                }
            }
        }

        contentText = contentBuilder.trimStart().toString()
    }

    private fun getSubjectId(messageEvent: JsonObject): Long {
        return messageEvent["group_id"]?.asLong
            ?: messageEvent["user_id"].asLong
    }

    override fun getReplyImage() = replyImage
    override fun getContentText() = contentText

    override fun hasTarget(): Boolean = hasTarget

    protected open fun wrapAtElement(id: Long): QQMessageElement.AtElement {
        throw UnsupportedOperationException("Use OnebotGroupMessageChainWrapper to support wrapping At elements in MessageChain")
    }
}
