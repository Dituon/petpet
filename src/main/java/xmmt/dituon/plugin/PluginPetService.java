package xmmt.dituon.plugin;

import kotlin.Pair;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import xmmt.dituon.share.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Random;

public class PluginPetService extends BasePetService {

    public void readConfigByPluginAutoSave() {
        ConfigDTO config = PetPetAutoSaveConfig.INSTANCE.content.get();
//        System.out.println("从AutoSaveConfig中读出：" + ConfigDTOKt.encode(config));
        readConfig(config);
    }


    public void sendImage(Group group, Member from, Member to) {
        try {
            sendImage(group, from, to, keyList.get(new Random().nextInt(keyList.size())));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("图片素材应位于 Mirai/data/xmmt.dituon.petpet 目录下, 请检查路径");
        }
    }

    public void sendImage(Group group, Member from, Member to, boolean random) {
        if (!random) {
            sendImage(group, from, to);
            return;
        }
        int r = new Random().nextInt(randomMax);
        if (r >= keyList.size()) {
            return;
        }
        sendImage(group, from, to, keyList.get(r));
    }

    public void sendImage(Group group, Member from, Member to, String key) {
        TextExtraData textExtraData = new TextExtraData(
                from.getNameCard().isEmpty() ? from.getNick() : from.getNameCard(),
                to.getNameCard().isEmpty() ? to.getNick() : to.getNameCard(),
                group.getName()
        );
        sendImage(group, from, from.getAvatarUrl(), to.getAvatarUrl(), key, textExtraData);
    }

    public void sendImage(Group group, Member m, String fromURL, String toURL, String key, TextExtraData textExtraData) {
        BufferedImage fromAvatarImage = ImageSynthesis.getAvatarImage(fromURL);
        BufferedImage toAvatarImage = ImageSynthesis.getAvatarImage(toURL);

        Pair<InputStream, String> generatedImageAndType = generateImage(fromAvatarImage, toAvatarImage, key, textExtraData, null);

        try {
            if (generatedImageAndType != null) {
                ExternalResource resource = ExternalResource.create(generatedImageAndType.getFirst());
                Image image = m.uploadImage(resource);
                resource.close();
                group.sendMessage(image);
            } else {
                System.out.println("生成图片失败");
            }
        } catch (Exception ex) {
            System.out.println("发送生成的图片时出错：" + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
