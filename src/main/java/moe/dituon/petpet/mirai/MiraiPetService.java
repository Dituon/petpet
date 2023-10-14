package moe.dituon.petpet.mirai;

import moe.dituon.petpet.plugin.Cooler;
import moe.dituon.petpet.plugin.PluginPetService;
import moe.dituon.petpet.server.WebServer;
import moe.dituon.petpet.share.BaseConfigFactory;
import moe.dituon.petpet.share.GifAvatarExtraDataProvider;
import moe.dituon.petpet.share.TextExtraData;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class MiraiPetService extends PluginPetService {

    public byte probability = 30;
    public boolean respondSelfNudge = false;
    public boolean respondFriend = false;
    public Long coolDown = Cooler.DEFAULT_USER_COOLDOWN;
    public Long groupCoolDown = Cooler.DEFAULT_GROUP_COOLDOWN;
    public String inCoolDownMessage = Cooler.DEFAULT_MESSAGE;
    public boolean inCoolDownNudge = false;
    public boolean devMode = false;
    public boolean messageHook = false;
    public boolean nudgeCanBeDisabled = true;
    public boolean messageCanBeDisabled = false;

    public void readConfigByPluginAutoSave() {
        MiraiPluginConfig config = MiraiPluginConfig.INSTANCE;

        probability = (byte) config.getProbability();
        respondSelfNudge = config.getRespondSelfNudge();
        respondFriend = config.getRespondFriend();

        disabledGroups = config.getDisabledGroups();
        coolDown = config.getCoolDown();
        groupCoolDown = config.getGroupCoolDown();
        inCoolDownMessage = config.getInCoolDownMessage().isBlank() ?
                null : config.getInCoolDownMessage();
        if ("[nudge]".equals(inCoolDownMessage)) inCoolDownNudge = true;

        devMode = config.getDevMode();
        messageHook = config.getMessageHook();

        switch (config.getDisablePolicy()) {
            case NONE:
                nudgeCanBeDisabled = false;
                messageCanBeDisabled = false;
                break;
            case NUDGE:
                nudgeCanBeDisabled = true;
                messageCanBeDisabled = false;
                break;
            case MESSAGE:
                nudgeCanBeDisabled = false;
                messageCanBeDisabled = true;
                break;
            case FULL:
                nudgeCanBeDisabled = true;
                messageCanBeDisabled = true;
                break;
        }

        if (config.getStartWebServer()) new WebServer();

        super.readPluginServiceConfig(config.toPluginServiceConfig());
    }

    @Override
    public void readData(File dir) {
        if (dir.listFiles() == null) {
            MiraiPetpet.LOGGER.info(autoUpdate ?
                    "(ﾟДﾟ*)ﾉ 找不到 Petpet 模板, 开始尝试自动下载" :
                    "(T_T) 找不到 Petpet 模板"
            );
            return;
        }
        super.readData(dir);
    }

    /**
     * 发送随机图片
     */
    public void sendImage(Group group, Member from, Member to) { //发送随机图片
        sendImage(group, from, to, randomableList.get(MiraiPetpet.random.nextInt(randomableList.size())));
    }

    /**
     * 有概率发送随机图片
     */
    public void sendImage(Group group, Member from, Member to, boolean random) {
        if (!random) {
            sendImage(group, from, to);
            return;
        }
        int r = MiraiPetpet.random.nextInt(99) + 1; //不能为0
        if (r >= probability) return;
        sendImage(group, from, to);
    }

    /**
     * 用key发送图片(无otherText)
     */
    public void sendImage(Group group, Member from, Member to, String key) {
        sendImage(group, from, to, key, null);
    }

    /**
     * 用key发送图片，指定otherText
     */
    public void sendImage(Group group, Member from, Member to, String key, String otherText) {
        TextExtraData textExtraData = new TextExtraData(
                from.getNameCard().isEmpty() ? from.getNick() : from.getNameCard(),
                to.getNameCard().isEmpty() ? to.getNick() : to.getNameCard(),
                group.getName(),
                otherText == null || otherText.equals("") ?
                        Collections.emptyList() :
                        Arrays.asList(otherText.split("\\s+"))
        );
        GifAvatarExtraDataProvider gifAvatarExtraDataProvider = BaseConfigFactory.getGifAvatarExtraDataFromUrls(
                from.getAvatarUrl(), to.getAvatarUrl(), group.getAvatarUrl(), group.getBotAsMember().getAvatarUrl(),
                group.getMembers().stream().map(NormalMember::getAvatarUrl).collect(Collectors.toList())
        );
        sendImage(group, key, gifAvatarExtraDataProvider, textExtraData);
    }

    public void sendImage(Contact group, String key,
                          GifAvatarExtraDataProvider gifAvatarExtraDataProvider, TextExtraData textExtraData) {
        try {
            group.sendMessage(
                    inputStreamToImage(generateImage(
                            key,
                            gifAvatarExtraDataProvider,
                            textExtraData,
                            null
                    ).getFirst(), group)
            );
        } catch (Exception ex) {
            MiraiPetpet.LOGGER.error("发送图片时出错: " + ex.getMessage(), ex);
        }
    }

    public Image inputStreamToImage(@NotNull InputStream input, Contact contact) throws IOException {
        ExternalResource resource = ExternalResource.create(input);
        if (contact == null) contact = Bot.getInstances().get(0).getAsFriend();
        Image image = contact.uploadImage(resource);
        resource.close();
        return image;
    }
}