package xmmt.dituon.plugin;

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
        sendImage(group, from, to, keyList.get(new Random().nextInt(keyList.size())));
    }

    public void sendImage(Group group, Member m, String fromURL, String toURL, String[] info) {
        sendImage(group, m, fromURL, toURL, keyList.get(new Random().nextInt(keyList.size())), info);
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
        String[] info = {
                from.getNameCard().isEmpty() ? from.getNick() : from.getNameCard(),
                to.getNameCard().isEmpty() ? to.getNick() : to.getNameCard(),
                group.getName()
        };
        sendImage(group, from, from.getAvatarUrl(), to.getAvatarUrl(), key, info);
    }

    public void sendImage(Group group, Member m, String fromURL, String toURL, String key, String[] info) {
        BufferedImage fromAvatarImage = ImageSynthesis.getAvatarImage(fromURL);
        BufferedImage toAvatarImage = ImageSynthesis.getAvatarImage(toURL);

        InputStream generatedImage = generateImage(fromAvatarImage, toAvatarImage, key, info);

        try {
            if (generatedImage != null) {
                ExternalResource resource = ExternalResource.create(generatedImage);
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
