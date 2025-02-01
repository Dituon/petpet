package moe.dituon.petpet.bot.qq.mirai.handler;

import moe.dituon.petpet.bot.qq.handler.QQMessageElement;
import moe.dituon.petpet.bot.qq.mirai.MiraiBotService;
import moe.dituon.petpet.bot.qq.mirai.UtilsKt;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;

public class MiraiGroupMessageChainWrapper extends MiraiMessageChainWrapper {
    protected final Group group;

    public MiraiGroupMessageChainWrapper(MiraiBotService service, MessageChain miraiMessageChain, Group group) {
        super(service, miraiMessageChain);
        this.group = group;
    }

    protected class MiraiMessageAtElement extends QQMessageElement.AtElement {
        protected final At at;

        public MiraiMessageAtElement(At at) {
            this.at = at;
        }

        @Override
        public String getTargetId() {
            return String.valueOf(at.getTarget());
        }

        @Override
        public String getTargetName() {
            return UtilsKt.getMemberName(group.get(at.getTarget()));
        }
    }

    @Override
    protected QQMessageElement.AtElement wrapAtElement(At at) {
        return new MiraiMessageAtElement(at);
    }
}
