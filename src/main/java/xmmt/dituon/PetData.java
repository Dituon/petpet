package xmmt.dituon;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import xmmt.dituon.share.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Random;

import static xmmt.dituon.share.ImageSynthesis.getAvatarImage;

public class PetData extends BasePetData {

    public static void sendImage(Group group, Member from, Member to) {
        sendImage(group, from, to, BasePetData.keyList.get(new Random().nextInt(keyList.size())));
    }

    public static void sendImage(Group group, Member from, Member to, boolean random) {
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

    public static void sendImage(Group group, Member from, Member to, String key) {
        BufferedImage fromAvatarImage = getAvatarImage(from.getAvatarUrl());
        BufferedImage toAvatarImage = getAvatarImage(to.getAvatarUrl());

        InputStream generatedImage = generateImage(fromAvatarImage, toAvatarImage, key);

        try {
            if (generatedImage != null) {
                ExternalResource resource = ExternalResource.create(generatedImage);
                Image image = to.uploadImage(resource);
                group.sendMessage(image);
            } else {
                System.out.println("生成图片失败");
            }
        } catch (Exception ex) {
            System.out.println("发送生成的图片时出错：" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static int[] JsonArrayToIntArray(JsonArray ja) {
        return new int[]{
                Integer.parseInt(ja.get(0).toString()),
                Integer.parseInt(ja.get(1).toString()),
                Integer.parseInt(ja.get(2).toString()),
                Integer.parseInt(ja.get(3).toString())
        };
    }
}
