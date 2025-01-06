package moe.dituon.petpet.bot.qq.mirai;


import moe.dituon.petpet.bot.MessageEventHandler;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;

public abstract class MiraiMessageEventHandler extends MessageEventHandler {
    protected boolean hasGroupPermission(Member user) {
        return user.getPermission() != MemberPermission.MEMBER;
    }

    protected String getNameOrNick(Member m) {
        return m.getNameCard().isEmpty() ? m.getNick() : m.getNameCard();
    }
}
