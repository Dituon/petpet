package moe.dituon.petpet.bot.qq.mirai.handler;

import lombok.Getter;
import lombok.experimental.Delegate;
import moe.dituon.petpet.bot.qq.QQBotService;
import moe.dituon.petpet.bot.qq.handler.QQMessageChainInterface;
import moe.dituon.petpet.bot.qq.handler.QQMessageElement;
import moe.dituon.petpet.bot.qq.mirai.MiraiBotService;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MiraiMessageChainWrapper implements QQMessageChainInterface {
    @Delegate
    protected final List<QQMessageElement> elements;
    @Getter
    protected String replyImage;
    protected final MiraiBotService service;
    @Getter
    protected final String contentText;
    protected boolean hasTarget;

    /**
     * 包装时会自动缓存图像链接
     */
    public MiraiMessageChainWrapper(MiraiBotService service, MessageChain miraiMessageChain) {
        this.service = service;
        // Mirai MessageChain 中一定包含 MessageSource
        MessageSource source = null;

        this.elements = new ArrayList<>(miraiMessageChain.size() - 1);
        boolean firstAtSenderRemoved = false;
        boolean imageCached = false;
        var contentBuilder = new StringBuilder(miraiMessageChain.size() - 1);
        for (var ele : miraiMessageChain) {
            if (ele instanceof MessageSource) {
                source = (MessageSource) ele;
            } else if (ele instanceof Image) {
                var url = Image.queryUrl((Image) ele);
                // 确保只缓存多图像消息的第一张图像
                if (!imageCached && service.cacheImage(source, url)) {
                    imageCached = true;
                }
                this.hasTarget = true;
                this.elements.add(new QQMessageElement.ImageElement(url));
            } else if (ele instanceof PlainText) {
                var text = ((PlainText) ele).getContent();
                contentBuilder.append(' ').append(text);
                this.elements.add(new QQMessageElement.TextElement(text));
            } else if (ele instanceof At) {
                var at = (At) ele;
                // 排除第一个 @sender
                if (!firstAtSenderRemoved && at.getTarget() == source.getFromId()) {
                    firstAtSenderRemoved = true;
                    continue;
                }
                this.elements.add(wrapAtElement(at));
                this.hasTarget = true;
            } else if (ele instanceof QuoteReply) {
                this.replyImage = service.getCachedImage(((QuoteReply) ele).getSource());
                this.hasTarget = true;
            }
        }

        // 移除首个空格并设置内容文本
        this.contentText = contentBuilder.length() > 0 ? contentBuilder.substring(1) : "";
    }

    @Override
    public boolean hasTarget() {
        return this.hasTarget;
    }

    protected QQMessageElement.AtElement wrapAtElement(At at) {
        throw new UnsupportedOperationException("使用 MiraiGroupMessageChainWrapper 以支持包装带有 At 元素的 MessageChain");
    }
}
