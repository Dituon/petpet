package moe.dituon.petpet.bot.qq.mirai.handler;


import moe.dituon.petpet.bot.BotSendEvent;
import moe.dituon.petpet.bot.qq.handler.QQMessageEventHandler;
import moe.dituon.petpet.bot.qq.mirai.MiraiBotService;
import moe.dituon.petpet.bot.qq.mirai.ScriptMiraiBotSendEvent;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.script.PetpetScriptModel;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.action.Nudge;
import net.mamoe.mirai.message.data.Image;

import java.io.IOException;

public class MiraiMessageHandler extends QQMessageEventHandler {
    protected final MiraiBotService service;

    public MiraiMessageHandler(MiraiBotService service) {
        super(service);
        this.service = service;
    }

    public void handle(MessageEvent event) {
        var handler = new MiraiMessageContext(event);
        handler.handleCommand();
    }

    protected class MiraiMessageContext extends QQMessageEventHandler.MessageContext {
        protected final MessageEvent messageEvent;

        protected MiraiMessageContext(MessageEvent event) {
            this(event, new MiraiMessageChainWrapper(service, event.getMessage()));
        }

        protected MiraiMessageContext(MessageEvent event, MiraiMessageChainWrapper message) {
            this.messageEvent = event;
            this.message = message;
            this.messageText = message.getContentText();
            this.permission = service.getPermission(getSubjectId());
        }

        @Override
        protected boolean senderHasGroupPermission() {
            return false;
        }

        @Override
        protected BotSendEvent buildBotSendEvent(PetpetScriptModel script, RequestContext context) {
            return new ScriptMiraiBotSendEvent(messageEvent.getBot(), context, script.getBasePath());
        }

        @Override
        protected String getBotName() {
            return messageEvent.getBot().getNick();
        }

        @Override
        protected String getBotId() {
            return String.valueOf(messageEvent.getBot().getId());
        }

        @Override
        protected String getSenderName() {
            return messageEvent.getSenderName();
        }

        @Override
        protected String getSenderId() {
            return String.valueOf(messageEvent.getSender().getId());
        }

        @Override
        protected String getSubjectName() {
            var subject = messageEvent.getSubject();
            if (subject instanceof User) {
                return ((User) subject).getNick();
            }
            return getSubjectId();
        }

        @Override
        protected String getSubjectId() {
            return String.valueOf(messageEvent.getSubject().getId());
        }

        @Override
        protected void replyMessage(String text) {
            messageEvent.getSubject().sendMessage(text);
        }

        @Override
        protected void replyMessage(EncodedImage image) {
            try {
                var img = service.uploadImage(image.bytes, messageEvent.getSubject());
                messageEvent.getSubject().sendMessage(img);
            } catch (IOException ex) {
                replyMessage("上传图像时出错: " + ex.getMessage());
                throw new IllegalStateException(ex);
            }
        }

        @Override
        protected void replyMessage(BotSendEvent e) {
            for (var msg : ((ScriptMiraiBotSendEvent) e).getResponseMessage()) {
                this.messageEvent.getSubject().sendMessage(msg);
            }
        }

        @Override
        protected boolean inGroupContext() {
            return false;
        }

        @Override
        protected void replyNudge() {
            messageEvent.getSender().nudge().sendTo(messageEvent.getSender());
        }
    }
}
