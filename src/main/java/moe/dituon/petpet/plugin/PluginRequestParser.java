package moe.dituon.petpet.plugin;

import moe.dituon.petpet.server.RequestParser;
import moe.dituon.petpet.share.BaseConfigFactory;
import moe.dituon.petpet.share.TextExtraData;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import org.jetbrains.annotations.NotNull;

public class PluginRequestParser extends RequestParser {
    Contact contact;
    public PluginRequestParser(String json, Contact contact) {
        this.contact = contact;
        PluginRequestDTO request = PluginRequestDTO.decodeFromString(json);
        super.imagePair = Petpet.service.generateImage(
                request.getKey(),
                BaseConfigFactory.getGifAvatarExtraDataFromUrls(
                        request.getForm().getURL(),
                        request.getTo().getURL(),
                        request.getGroup().getURL(),
                        request.getBot().getURL(),
                        request.getRandomAvatarList()
                ),
                new TextExtraData(
                        getTargetName(request.getForm(), "from"),
                        getTargetName(request.getTo(), "to"),
                        getTargetName(request.getGroup(), "group"),
                        request.getTextList()
                ), null
        );
    }

    private String getTargetName(@NotNull PluginRequestTargetDTO targetDTO, String defaultStr){
        if (contact instanceof Group) {
            Group group = (Group) contact;
            return targetDTO.getName() != null ? targetDTO.getName() : (
                    targetDTO.getQq() != null ? getNameOrNick(group.get(targetDTO.getQq()), defaultStr) : defaultStr
            );
        }
        return targetDTO.getName() != null ? targetDTO.getName() : ((User) contact).getNick();
    }

    private static String getNameOrNick(Member m, String defaultStr) {
        if (m == null) return defaultStr;
        return m.getNameCard().isEmpty() ? m.getNick() : m.getNameCard();
    }
}
