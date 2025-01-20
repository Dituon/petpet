package moe.dituon.petpet.bot.qq.handler;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface QQMessageChainInterface extends List<QQMessageElement> {
    String DEFAULT_IMAGE_NAME = "这个";

    @Nullable
    String getReplyImage();

    /**
     * 获取消息中的纯文本
     */
    String getContentText();

    /**
     * 获取消息中是否有目标: 例如 at 某人, 发送或回复图像等
     */
    default boolean hasTarget() {
        return getReplyImage() != null || this.stream().anyMatch(ele ->
                ele.getType() == QQMessageElement.MessageType.AT || ele.getType() == QQMessageElement.MessageType.IMAGE
        );
    }
}
