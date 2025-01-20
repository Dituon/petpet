package moe.dituon.petpet.bot.qq.handler;

import moe.dituon.petpet.bot.qq.avatar.QQAvatarRequester;

public interface QQMessageElement {
    enum MessageType {
        TEXT,
        IMAGE,
        AT
    }

    MessageType getType();

    String getContent();

    interface ResizeableImageElement {
        String getUrl(int size);

        default String getName() {
            return QQMessageChain.DEFAULT_IMAGE_NAME;
        }
    }

    class TextElement implements QQMessageElement {
        public final String content;

        public TextElement(String content) {
            this.content = content;
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public MessageType getType() {
            return MessageType.TEXT;
        }
    }

    class ImageElement implements QQMessageElement, ResizeableImageElement {
        public final String url;

        public ImageElement(String url) {
            this.url = url;
        }

        @Override
        public String getContent() {
            return url;
        }

        @Override
        public MessageType getType() {
            return MessageType.IMAGE;
        }

        @Override
        public String getUrl(int size) {
            // TODO: gchat.qpic.cn 貌似可以请求到低分辨率版本
            return getContent();
        }
    }

    abstract class AtElement implements QQMessageElement, ResizeableImageElement {
        @Override
        public String getContent() {
            return getTargetId();
        }

        public abstract String getTargetId();

        public abstract String getTargetName();

        @Override
        public String getUrl(int size) {
            return QQAvatarRequester.getAvatarUrlString(getTargetId(), size);
        }

        @Override
        public String getName() {
            return getTargetName();
        }

        @Override
        public MessageType getType() {
            return MessageType.AT;
        }

        public static AtElement from(String targetId, String targetName) {
            return new AtElement() {
                @Override
                public String getTargetId() {
                    return targetId;
                }

                @Override
                public String getTargetName() {
                    return targetName;
                }
            };
        }
    }
}
