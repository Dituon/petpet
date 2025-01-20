package moe.dituon.petpet.bot.qq.mirai.handler;

import moe.dituon.petpet.bot.BotSendEvent;
import moe.dituon.petpet.bot.qq.mirai.MiraiBotService;
import moe.dituon.petpet.bot.qq.mirai.ScriptMiraiBotSendEvent;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.script.PetpetScriptModel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.QuoteReply;

import java.io.IOException;

public class MiraiGroupMessageHandler extends MiraiMessageHandler {

    public MiraiGroupMessageHandler(MiraiBotService service) {
        super(service);
    }

    public void handle(GroupMessageEvent event) {
        var handler = new MiraiGroupMessageContext(event);
        handler.handleCommand();
    }

    protected class MiraiGroupMessageContext extends MiraiMessageHandler.MiraiMessageContext {
        protected final GroupMessageEvent groupMessageEvent;

        protected MiraiGroupMessageContext(GroupMessageEvent event) {
            super(event, new MiraiGroupMessageChainWrapper(service, event.getMessage(), event.getGroup()));
            this.groupMessageEvent = event;
        }

        @Override
        protected boolean senderHasGroupPermission() {
            return groupMessageEvent.getPermission().getLevel() > 0;
        }

        @Override
        protected String getSubjectName() {
            return groupMessageEvent.getGroup().getName();
        }

        @Override
        protected void replyMessage(String text) {
            groupMessageEvent.getGroup().sendMessage(new QuoteReply(groupMessageEvent.getMessage()).plus(text));
        }

        @Override
        protected void replyMessage(EncodedImage image) {
            try {
                var img = service.uploadImage(image.bytes, groupMessageEvent.getGroup());
                groupMessageEvent.getGroup().sendMessage(new QuoteReply(groupMessageEvent.getMessage()).plus(img));
            } catch (IOException ex) {
                replyMessage("上传图像时出错: " + ex.getMessage());
                throw new IllegalStateException(ex);
            }
        }

        @Override
        protected BotSendEvent buildBotSendEvent(PetpetScriptModel script, RequestContext context) {
            return new ScriptMiraiBotSendEvent(groupMessageEvent.getBot(), context, script.getBasePath());
        }

        @Override
        protected void replyMessage(BotSendEvent e) {
            for (var msg : ((ScriptMiraiBotSendEvent) e).getResponseMessage()) {
                this.groupMessageEvent.getGroup().sendMessage(new QuoteReply(groupMessageEvent.getMessage()).plus(msg));
            }
        }

        @Override
        protected boolean inGroupContext() {
            return true;
        }
    }
}
