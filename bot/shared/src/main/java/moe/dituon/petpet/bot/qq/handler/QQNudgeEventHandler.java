package moe.dituon.petpet.bot.qq.handler;

import moe.dituon.petpet.bot.qq.QQBotService;
import moe.dituon.petpet.core.context.RequestContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class QQNudgeEventHandler extends QQMessageEventHandler {
    protected static final Random random = new Random();

    protected QQNudgeEventHandler(QQBotService service) {
        super(service);
    }

    public void handle(NudgeContext context) {
        context.handleCommand();
    }

    public abstract class NudgeContext extends MessageContext {
        protected NudgeContext() {
            rawMessageText = "";
        }

        public void handleNudge() {
            float probability = permission.getNudgeProbability();
            if (probability > 0
                    && probability >= random.nextFloat()
                    && !isInCooldown()
            ) {
                template = service.randomTemplate();
                // 在冷却时间内不会返回提示
                responseTemplate();
                lockCooldown();
            }
        }

        @Override
        protected RequestContext buildRequestContext() {
            List<QQMessageElement.ResizeableImageElement> imageList = List.of(
                    QQMessageElement.AtElement.from(getTargetId(), getTargetName()), // Target
                    QQMessageElement.AtElement.from(getSenderId(), getSenderName()) // Sender
            );

            // 构建 imageUrlMap 和 textMap
            return buildRequestContext(imageList);
        }

        @Override
        protected @Nullable String getMessageTokenByIndex(String key) {
            return null;
        }

        protected abstract String getTargetId();

        protected abstract String getTargetName();
    }
}
