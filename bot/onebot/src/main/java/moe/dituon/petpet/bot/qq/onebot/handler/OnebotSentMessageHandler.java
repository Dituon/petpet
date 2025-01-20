package moe.dituon.petpet.bot.qq.onebot.handler;

import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent;
import cn.evolvefield.onebot.sdk.event.message.PrivateMessageEvent;
import com.google.gson.JsonArray;
import moe.dituon.petpet.bot.qq.onebot.MainKt;
import moe.dituon.petpet.bot.qq.onebot.OnebotBotService;
import org.jetbrains.annotations.Nullable;

public class OnebotSentMessageHandler {
    protected final OnebotBotService service;

    /**
     * 用于缓存 Bot 自己发送的图像
     */
    public OnebotSentMessageHandler(OnebotBotService service) {
        this.service = service;
    }

    public void handle(PrivateMessageEvent messageEvent) {
        if (messageEvent.getUserId() != MainKt.globalBotInstance.getId()) {
            return;
        }
        service.cacheImage(
                messageEvent.getUserId(),
                messageEvent.getMessageId(),
                getImageUrl(messageEvent.getJson().getAsJsonObject()
                        .getAsJsonArray("message")
                )
        );
    }

    public void handle(GroupMessageEvent groupMessageEvent) {
        if (groupMessageEvent.getUserId() != MainKt.globalBotInstance.getId()) {
            return;
        }
        service.cacheImage(
                groupMessageEvent.getGroupId(),
                groupMessageEvent.getMessageId(),
                getImageUrl(groupMessageEvent.getJson().getAsJsonObject()
                        .getAsJsonArray("message")
                )
        );
    }

    protected @Nullable String getImageUrl(JsonArray messages) {
        for (var ele : messages) {
            var type = ele.getAsJsonObject().get("type").getAsString();
            if (type.equals("image")) {
                return ele.getAsJsonObject().getAsJsonObject("data")
                        .get("url").getAsString();
            }
        }
        return null;
    }
}
