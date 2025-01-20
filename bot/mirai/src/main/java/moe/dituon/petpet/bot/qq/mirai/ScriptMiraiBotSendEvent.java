package moe.dituon.petpet.bot.qq.mirai;

import moe.dituon.petpet.bot.BotSendEvent;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import net.mamoe.mirai.Bot;
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
    public final Bot bot;
    protected List<MessageChainBuilder> messageBuilderList = null;
    protected int messageGroupIndex = 0;

    public ScriptMiraiBotSendEvent(
            Bot bot,
            RequestContext requestContext,
            @Nullable File basePath
    ) {
        super(requestContext, basePath);
        this.bot = bot;
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
        var img = bot.getAsFriend().uploadImage(
                ExternalResource.create(image.bytes)
        );
        getMessageBuilder().add(img);
    }

    /**
     * 当回复转发消息时, 数组仅包含一个转发消息元素; 当回复普通消息时, 数组可能包含多个元素
     */
    public List<MessageChain> getResponseMessage() {
        if (isResponseInForward) {
            var fb = new ForwardMessageBuilder(bot.getAsFriend(), messageBuilderList.size());
            for (MessageChainBuilder mb : messageBuilderList) {
                if (mb.isEmpty()) continue;
                fb.add(bot, mb.build());
            }
            return List.of(new MessageChainBuilder(1).append(fb.build()).build());
        }
        return messageBuilderList.stream()
                .filter(builder -> !builder.isEmpty())
                .map(MessageChainBuilder::build)
                .collect(Collectors.toList());
    }
}
