package moe.dituon.petpet.bot.qq.mirai.handler;

import moe.dituon.petpet.bot.qq.mirai.MiraiBotService;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.MessagePostSendEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;

public class MiraiSentMessageHandler {
    protected final MiraiBotService service;

    /**
     * 用于缓存 Bot 自己发送的图像
     */
    public MiraiSentMessageHandler(MiraiBotService service) {
        this.service = service;
    }

    protected void handle(MessageChain messages) {
        Image image = null;
        MessageSource source = null;
        for (var ele : messages) {
            if (ele instanceof MessageSource) {
                source = (MessageSource) ele;
                break;
            } else if (ele instanceof Image) {
                image = (Image) ele;
                break;
            }
        }
        if (image != null) {
            service.cacheImage(source, Image.queryUrl(image));
        }
    }

    public void handle(MessageEvent e) {
        handle(e.getMessage());
    }

    public void handle(MessagePostSendEvent<?> e) {
        handle(e.getMessage());
    }
}
