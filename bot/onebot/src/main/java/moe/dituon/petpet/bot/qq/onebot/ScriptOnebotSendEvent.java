package moe.dituon.petpet.bot.qq.onebot;

import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent;
import cn.evolvefield.onebot.sdk.event.message.MessageEvent;
import moe.dituon.petpet.bot.BotSendEvent;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptOnebotSendEvent extends BotSendEvent {
    public final MessageEvent event;
    public final OnebotBotService service;
    protected ArrayList<List<String>> messageGroupList = null;
    protected int messageGroupIndex = 0;

    public ScriptOnebotSendEvent(
            OnebotBotService service,
            MessageEvent event,
            RequestContext requestContext,
            @Nullable File basePath
    ) {
        super(requestContext, basePath);
        this.service = service;
        this.event = event;
    }

    @Override
    public void responseNewParagraph() {
        nextMessageGroup();
    }

    @Override
    public void responseInForward(boolean flag) {
        this.isResponseInForward = flag;
    }

    @Override
    public void response(String text) {
        getMessageGroup().add(
                "{\"type\": \"text\",\"data\": {\"text\": "
                        + UtilsKt.encodeJsonString(text)
                        + "}}"
        );
    }

    @Override
    public void responseImage(EncodedImage image) {
        getMessageGroup().add(
                "{\"type\": \"image\",\"data\": {\"file\": \""
                        + service.putImage(image)
                        + "\"}}"
        );
    }

    protected List<String> getMessageGroup() {
        if (messageGroupList == null) {
            messageGroupList = new ArrayList<>(8);
            messageGroupList.add(new ArrayList<>(8));
        }
        return messageGroupList.get(messageGroupIndex);
    }

    protected void nextMessageGroup() {
        messageGroupIndex++;
        messageGroupList.add(new ArrayList<>(8));
    }

    /**
     * 需要在调用段实现转发消息
     */
    public @Nullable List<String> getResponseMessage() {
        if (messageGroupList.isEmpty()) {
            return null;
        }
        if (!isResponseInForward && event instanceof GroupMessageEvent) {
            return messageGroupList.stream()
                    .map(msgs ->
                            "[{\"type\": \"reply\",\"data\": {\"id\": \""
                                    + ((GroupMessageEvent) event).getMessageId()
                                    + "\"}},"
                                    + String.join(",", msgs)
                                    + "]"
                    ).collect(Collectors.toList());
        }
        return messageGroupList.stream()
                .map(msgs -> '[' + String.join(",", msgs) + ']')
                .collect(Collectors.toList());
    }

    public boolean isResponseInForward() {
        return isResponseInForward;
    }
}
