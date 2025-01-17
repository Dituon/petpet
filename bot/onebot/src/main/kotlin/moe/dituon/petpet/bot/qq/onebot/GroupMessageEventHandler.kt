package moe.dituon.petpet.bot.qq.onebot

import cn.evolvefield.onebot.sdk.entity.GroupSender
import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent
import cn.evolvefield.onebot.sdk.util.CQCode
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import lombok.extern.slf4j.Slf4j
import moe.dituon.petpet.bot.MessageEventHandler
import moe.dituon.petpet.bot.qq.ReplyType
import moe.dituon.petpet.bot.qq.avatar.QQAvatarRequester
import moe.dituon.petpet.bot.qq.permission.ContactPermission
import moe.dituon.petpet.bot.utils.Cooler
import moe.dituon.petpet.core.context.RequestContext
import moe.dituon.petpet.core.element.PetpetModel
import moe.dituon.petpet.core.element.PetpetTemplateModel
import moe.dituon.petpet.core.imgres.ImageResource
import moe.dituon.petpet.core.imgres.ImageResourceMap
import moe.dituon.petpet.core.utils.image.EncodedImage
import moe.dituon.petpet.script.PetpetJsScriptModel
import moe.dituon.petpet.script.PetpetScriptModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.MalformedURLException
import java.util.*
import kotlin.collections.set
import kotlin.streams.toList


@Slf4j
open class GroupMessageEventHandler(private val service: OnebotBotService): MessageEventHandler() {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    protected val command: String = service.getOnebotConfig().command
    protected val respondNudgeWhenCooldown: Boolean = "[nudge]" == service.onebotConfig.inCoolDownMessage

    suspend fun handle(event: GroupMessageEvent) {
        val handler = Handler(event)
        handler.handle()
    }

    open inner class Handler(
        protected val event: GroupMessageEvent
    ) {
        protected lateinit var message: String
        protected val permission: ContactPermission = service.getPermission(event.groupId)
        protected var templateModel: PetpetModel? = null
        protected var resultDefault: Boolean = false

        init {
            init()
        }

        protected fun init() {
            if (event.sender == null) {
                message = ""
                return
            }
            val messageBuilder = StringBuilder()
            val messages = if (event.isJsonMessage) {
                event.json.asJsonObject.getAsJsonArray("message")
            } else {
                CQCode.toJson(event.message)
            }
            for (ele in messages) {
                val msg = ele.asJsonObject
                val type = msg["type"].asString

                when (type) {
                    "text" -> messageBuilder.append(
                        msg["data"].asJsonObject["text"].asString
                    ).append(' ')

                    "image" -> {
                        // id = group id + message id
                        val id = event.groupId + event.messageId
                        service.imageCachePool[id] = msg["data"].asJsonObject["url"].asString
                    }
                }
            }
            this.message = messageBuilder.toString()
        }

        suspend fun handle() {
            if (message.isBlank() || event.sender!!.userId == globalBotInstance.id.toString()) return
            if (handleOpCommand()) return
            handleCommand()
        }

        protected fun hasGroupPermission(user: GroupSender): Boolean {
            return user.role != "member"
        }

        protected suspend fun handleOpCommand(): Boolean {
            if (!hasGroupPermission(event.sender!!)) return false

            if (!message.startsWith(command)) {
                return false
            }

            val commandOperation = message.substring(command.length)
            var result: String?
            try {
                result = permission.handleEditCommand(commandOperation)
                if (result == null) {
                    return false
                }
            } catch (e: IllegalArgumentException) {
                result = e.message
            } catch (e: IllegalStateException) {
                result = e.message
            }
            this.sendMessage("$command $result")

            return true
        }

        protected suspend fun handleCommand() {
            val commandHead = service.onebotConfig.commandHead
            if (message.startsWith(command)) { // /pet (id?) (param?)
                if ((permission.commandPermission and ContactPermission.COMMAND) == 0) {
                    // no permission
                    return
                }
                val idAndParam = message.substring(command.length).trim { it <= ' ' }
                if (idAndParam.isBlank()) { // /pet
                    val isUseDefaultTemplate = service.config.defaultReplyType != ReplyType.RANDOM
                    handleIdAndParam(idAndParam, false, isUseDefaultTemplate)
                    return
                }
                handleIdAndParam(idAndParam, false, false) // (id?) (param?)
            } else if (commandHead.isEmpty() || message.startsWith(commandHead)) { // #(id?) (param?)
                if ((permission.commandPermission and ContactPermission.COMMAND_HEAD) == 0) {
                    // no permission
                    return
                }
                val idAndParam: String = message.substring(commandHead.length).trim { it <= ' ' }
                handleIdAndParam(idAndParam, true, false)
            }
        }

        /**
         * @param idAndParam   (id?) (param?)
         * @param idIsRequired if id is undefined, use random id or default template
         */
        protected suspend fun handleIdAndParam(idAndParam: String, idIsRequired: Boolean, useDefaultTemplate: Boolean) {
            val tokens = idAndParam  // [id, ...params]
                .split(" +".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val idOrAlias = if (tokens.isNotEmpty()) tokens[0].replace("\n", "") else ""
            val model = resolveTemplateModel(idOrAlias, idIsRequired, useDefaultTemplate) ?: return

            if (Cooler.isLocked(event.groupId) || Cooler.isLocked(event.sender!!.userId)) {
                if (respondNudgeWhenCooldown) {
                    // nudge
                    sendJsonMessage("[{\"type\":\"touch\",\"data\":{\"id\":${event.sender!!.userId}}}]")
                    return
                }
                if (service.onebotConfig.inCoolDownMessage.isBlank()) return
                sendMessage(service.onebotConfig.inCoolDownMessage)
                return
            }

            this.templateModel = model

            // TODO: cache request map
            val context = buildRequestContext(
                if (tokens.isNotEmpty()) Arrays.copyOfRange(tokens, 1, tokens.size) else emptyArray(),
                if (tokens.isNotEmpty()) idAndParam.substring(idOrAlias.length).trim { it <= ' ' } else ""
            )
            sendImage(model, context)

            if (!resultDefault) {
                Cooler.lock(event.groupId, permission.cooldownTime)
                Cooler.lock(event.sender!!.userId, service.onebotConfig.userCooldownTime)
            }
        }

        /**
         * 根据 ID 或别名解析模板模型，如果找不到则根据参数选择默认模板或随机模板。
         */
        private fun resolveTemplateModel(
            idOrAlias: String,
            idIsRequired: Boolean,
            useDefaultTemplate: Boolean
        ): PetpetModel? {
            if (idOrAlias.isEmpty()) {
                if (idIsRequired) return null
                return if (useDefaultTemplate) {
                    this.resultDefault = true
                    service.defaultTemplate
                } else service.randomTemplate()
            }

            var model = service.getTemplate(idOrAlias)
            if (model == null && !idIsRequired) {
                model = if (useDefaultTemplate) {
                    this.resultDefault = true
                    service.defaultTemplate
                } else service.randomTemplate()
            }
            return model
        }

        fun getNameOrNick(sender: GroupSender) = sender.card.ifBlank {
            sender.nickname
        }

        /**
         * @param params [id, ...params]
         */
        protected fun buildRequestContext(params: Array<String>, rawParams: String): RequestContext {
            val textMap = HashMap<String, String>(params.size + 8)
            textMap["raw"] = rawParams
            for (i in params.indices) {
                // text variable e.g. text${1}
                textMap[(i).toString()] = params[i]
            }

            var ignoreAt = false
            // default subject is BOT
            var fromName: String? = null
            var fromUrl: String? = null
            var fromId: String? = null
            // default target is sender
            var toName: String? = null
            var toUrl: String? = null
            var toId: String? = null
            val messages = event.json.asJsonObject["message"].asJsonArray
            val customImageList: ArrayList<String> = ArrayList<String>(messages.size())
            for (ele in messages) {
                val msg = ele.asJsonObject
                val type = msg["type"].asString
                when (type) {
                    "at" -> {
                        if (ignoreAt || (permission.commandPermission and ContactPermission.AT) == 0) {
                            continue
                        }

                        val target = msg["data"].asJsonObject["qq"].asString
                        // ignored at all or self
                        if (target == "all" || target == event.sender!!.userId) {
                            continue
                        }

                        // form = sender; to = at target
                        fromName = getNameOrNick(event.sender!!)
                        fromId = event.sender!!.userId
                        fromUrl = getAvatarUrl(fromId, "from")
                        toName = target //TODO
                        toUrl = getAvatarUrl(target, "to")
                        toId = target
                    }

                    "reply" -> {
                        if ((permission.commandPermission and ContactPermission.IMAGE) == 0) {
                            continue
                        }

                        val id = msg["data"].asJsonObject["id"].asString.toLong()
                        val sourceImgSrc = service.imageCachePool[event.groupId + id] ?: continue
                        customImageList.add(sourceImgSrc)

                        fromName = getNameOrNick(event.sender!!)
                        fromUrl = getAvatarUrl(event.sender!!.userId, "from")
                        fromId = event.sender!!.userId
                        ignoreAt = true
                    }

                    "image" -> {
                        if ((permission.commandPermission and ContactPermission.IMAGE) == 0) {
                            continue
                        }
                        customImageList.add(msg["data"].asJsonObject["url"].asString)

                        fromName = getNameOrNick(event.sender!!)
                        fromId = event.sender!!.userId
                        fromUrl = getAvatarUrl(fromId, "from")
                        ignoreAt = true
                    }
                }
            }

            // customImageList: [to, form, ...random]
            if (customImageList.isNotEmpty()) {
                if (customImageList.size > 1) {
                    // stay from sender name
                    fromUrl = customImageList[1]
                    fromId = fromUrl
                    if (customImageList.size > 2) {
                        // TODO: RandomImageResource
//                        randomImageList = customImageList.subList(2, customImageList.size());
                    }
                }
                toUrl = customImageList[0]
                toId = toUrl
                toName = "这个"
            }

            fromName = fromName ?: "bot" //TODO
            fromUrl = fromUrl ?: getAvatarUrl(globalBotInstance.id.toString(), "from")
            fromId = fromId ?: event.sender!!.userId
            toName = toName ?: event.sender!!.card
            toUrl = toUrl ?: getAvatarUrl(event.sender!!.userId, "to")
            toId = toId ?: event.sender!!.userId

            // TODO: 按需获取数据
            textMap[FROM_KEY] = fromName
            textMap[TO_KEY] = toName
            textMap[GROUP_KEY] = event.groupId.toString() //TODO
            textMap[BOT_KEY] = "bot" //TODO
            textMap[FROM_ID_KEY] = fromId
            textMap[TO_ID_KEY] = toId
            textMap[GROUP_ID_KEY] = event.groupId.toString()
            textMap[BOT_ID_KEY] = globalBotInstance.id.toString()

            try {
                return RequestContext(
                    ImageResourceMap(
                        mapOf<String, ImageResource>(
                            FROM_KEY to QQAvatarRequester.getAvatarResource(fromUrl),
                            TO_KEY to QQAvatarRequester.getAvatarResource(toUrl),
                            GROUP_KEY to QQAvatarRequester.getAvatarResource(
                                getAvatarUrl(event.groupId, GROUP_KEY)
                            ),
                            BOT_KEY to QQAvatarRequester.getAvatarResource(
                                getAvatarUrl(globalBotInstance.id, BOT_KEY)
                            )
                        )
                    ),
                    textMap
                )
            } catch (e: MalformedURLException) {
                // never
                throw IllegalStateException(e)
            }
        }

        protected fun getAvatarUrl(qqId: Long, key: String?) = getAvatarUrl(qqId.toString(), key)
        protected fun getAvatarUrl(qqId: String, key: String?): String {
            return QQAvatarRequester.getAvatarUrlString(
                qqId,
                service.getTemplateExpectedSize(templateModel)
                    .getOrDefault(key, 640)
            )
        }

        protected suspend fun sendImage(model: PetpetModel?, context: RequestContext?) {
            if (model is PetpetScriptModel) {
                val basePath = if (model is PetpetJsScriptModel) model.basePath else null
                val scriptEvent = ScriptOnebotSendEvent(service, this.event, context, basePath)
                if (model.eventManager.has("bot_send")) {
                    model.eventManager.trigger("bot_send", scriptEvent)
                }
                val msgList = scriptEvent.responseMessage ?: return
                if (scriptEvent.isResponseInForward) {
                    globalBotInstance.sendGroupForwardMsg(event.groupId, "[" + msgList.stream().map {
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
                    }.toList().joinToString(",") + "]")
                } else {
                    for (msgGroup in msgList) {
                        sendJsonMessage(msgGroup)
                    }
                }
            } else if (model is PetpetTemplateModel) {
                val img = model.draw(context)
                try {
                    sendMessage(img)
                } catch (ex: Exception) {
                    sendMessage("上传图像时出错: " + ex.message)
                    log.warn("上传图像时出错", ex)
                }
            }
        }

        protected suspend fun sendJsonMessage(json: String) {
            globalBotInstance.sendGroupMsg(event.groupId, json, false)
        }

        protected suspend fun sendMessage(msg: String) {
            globalBotInstance.sendGroupMsg(
                event.groupId, """[
                {"type": "reply","data": {"id": "${event.messageId}"}},
                {"type": "text","data": {"text": ${Json.encodeToString(String.serializer(), msg)}}}
            ]""".trimIndent(), false
            )
        }

        protected suspend fun sendMessage(image: EncodedImage) {
            globalBotInstance.sendGroupMsg(
                event.groupId, """[
                {"type": "reply","data": {"id": "${event.messageId}"}},
                {"type": "image","data": {"file": "${service.putImage(image)}"}}
                ]""".trimIndent(), false
            )
        }
    }
}

