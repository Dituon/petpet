package moe.dituon.petpet.bot.qq.mirai;

import moe.dituon.petpet.bot.BotSendEvent;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptMiraiBotSendEvent extends BotSendEvent {
    public final MessageEvent event;
    protected List<MessageChainBuilder> messageBuilderList = null;
    protected int messageGroupIndex = 0;

    public ScriptMiraiBotSendEvent(
            MessageEvent event,
            RequestContext requestContext,
            @Nullable File basePath
    ) {
        super(requestContext, basePath);
        this.event = event;
    }

    protected MessageChainBuilder getMessageBuilder() {
        if (messageBuilderList == null) {
            messageBuilderList = new ArrayList<>(8);
            messageBuilderList.add(new MessageChainBuilder(8));
        }
        return messageBuilderList.get(messageGroupIndex);
    }

    protected void nextMessageGroup() {
        messageGroupIndex++;
        messageBuilderList.add(new MessageChainBuilder(8));
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
        getMessageBuilder().add(text);
    }

    @Override
    public void responseImage(EncodedImage image) {
        var img = event.getSubject().uploadImage(
                ExternalResource.create(image.bytes)
        );
        getMessageBuilder().add(img);
    }

    public List<MessageChain> getResponseMessage() {
        if (isResponseInForward) {
            var fb = new ForwardMessageBuilder(event.getBot().getAsFriend(), messageBuilderList.size());
            for (MessageChainBuilder mb : messageBuilderList) {
                fb.add(event.getBot(), mb.build());
            }
            return List.of(new MessageChainBuilder(1).append(fb.build()).build());
        }
        return messageBuilderList.stream()
                .map(MessageChainBuilder::build)
                .collect(Collectors.toList());
    }
}
