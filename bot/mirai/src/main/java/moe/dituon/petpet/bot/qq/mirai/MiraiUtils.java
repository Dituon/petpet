package moe.dituon.petpet.bot.qq.mirai;

import net.mamoe.mirai.contact.Member;
import org.jetbrains.annotations.Nullable;

public class MiraiUtils {
    public static String getMemberName(@Nullable Member member) {
        if (member == null) return "未知用户";
        return member.getNameCard().isEmpty() ? member.getNick() : member.getNameCard();
    }
}
