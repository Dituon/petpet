package moe.dituon.petpet.bot.qq.handler;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QQMessageChain implements QQMessageChainInterface {
    @Getter
    protected final String replyImage;
    @Getter
    protected final String contentText;
    @Delegate
    protected final List<QQMessageElement> elements;

    public QQMessageChain(@Nullable String replyImage, String contentText, List<QQMessageElement> elements) {
        this.replyImage = replyImage;
        this.contentText = contentText;
        this.elements = elements;
    }

    @Override
    public String toString() {
        return contentText;
    }

    public static QQMessageChain fromText(String text) {
        return new QQMessageChain(null, text, List.of(new QQMessageElement.TextElement(text)));
    }
}
