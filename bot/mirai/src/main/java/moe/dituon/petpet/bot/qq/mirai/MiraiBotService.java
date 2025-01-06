package moe.dituon.petpet.bot.qq.mirai;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.bot.qq.QQBotService;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Slf4j
public class MiraiBotService extends QQBotService {
    @Getter
    protected final MiraiPluginConfig miraiConfig;

    public MiraiBotService(MiraiPluginConfig config) {
        super(config.toQQBotConfig());
        this.miraiConfig = config;
    }

    public Image uploadImage(byte @NotNull [] bytes, Contact contact) throws IOException {
        ExternalResource resource = ExternalResource.create(bytes);
        if (contact == null) contact = Bot.getInstances().get(0).getAsFriend();
        Image image = contact.uploadImage(resource);
        resource.close();
        return image;
    }
}
