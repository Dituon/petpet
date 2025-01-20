package moe.dituon.petpet.bot.qq.mirai.handler;

import moe.dituon.petpet.bot.BotSendEvent;
import moe.dituon.petpet.bot.qq.handler.QQNudgeEventHandler;
import moe.dituon.petpet.bot.qq.mirai.MiraiBotService;
import moe.dituon.petpet.bot.qq.mirai.MiraiUtils;
import moe.dituon.petpet.bot.qq.mirai.ScriptMiraiBotSendEvent;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.script.PetpetScriptModel;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberKt;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.NudgeEvent;

import java.io.IOException;

public class MiraiGroupNudgeHandler extends QQNudgeEventHandler {
    protected final MiraiBotService service;
    public MiraiGroupNudgeHandler(MiraiBotService service) {
        super(service);
        this.service = service;
    }

    public void handle(NudgeEvent event) {
        new MiraiNudgeContext(event).handleNudge();
    }

    public class MiraiNudgeContext extends NudgeContext {
        protected final NudgeEvent event;
        public MiraiNudgeContext(NudgeEvent event) {
            this.event = event;
            super.permission = service.getPermission(event.getSubject().getId());
        }

        @Override
        protected String getTargetId() {
            return String.valueOf(event.getTarget().getId());
        }

        @Override
        protected String getTargetName() {
            return event.getTarget().getNick();
        }

        @Override
        protected boolean senderHasGroupPermission() {
            return false;
        }

        @Override
        protected BotSendEvent buildBotSendEvent(PetpetScriptModel script, RequestContext context) {
            return new ScriptMiraiBotSendEvent(event.getBot(), context, script.getBasePath());
        }

        @Override
        protected String getBotName() {
            return event.getBot().getNick();
        }

        @Override
        protected String getBotId() {
            return String.valueOf(event.getBot().getId());
        }

        @Override
        protected String getSenderName() {
            return event.getFrom().getNick();
        }

        @Override
        protected String getSenderId() {
            return String.valueOf(event.getFrom().getId());
        }

        @Override
        protected String getSubjectName() {
            var subject = event.getSubject();
            if (subject instanceof Group) {
                return ((Group) subject).getName();
            } else if (subject instanceof User) {
                return ((User) subject).getNick();
            }
            return getSubjectId();
        }

        @Override
        protected String getSubjectId() {
            return String.valueOf(event.getSubject().getId());
        }

        @Override
        protected void replyMessage(String text) {
            event.getSubject().sendMessage(text);
        }

        @Override
        protected void replyMessage(EncodedImage image) {
            try {
                service.uploadImage(image.bytes, event.getSubject());
            } catch (IOException e) {
                replyMessage("上传图像时出错: " + e.getMessage());
                throw new IllegalStateException(e);
            }
        }

        @Override
        protected void replyMessage(BotSendEvent e) {
            for (var msg : ((ScriptMiraiBotSendEvent) e).getResponseMessage()) {
                this.event.getSubject().sendMessage(msg);
            }
        }

        @Override
        protected boolean inGroupContext() {
            return true;
        }
    }
}
