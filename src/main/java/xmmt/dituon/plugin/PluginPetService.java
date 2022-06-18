package xmmt.dituon.plugin;

import kotlin.Pair;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import xmmt.dituon.share.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class PluginPetService extends BasePetService {

    protected String command = "pet";
    protected int randomMax = 40;
    protected boolean keyCommand = false;
    protected boolean commandMustAt = true;
    protected boolean respondImage = false;
    protected boolean respondSelfNudge = false;
    protected boolean headless = false;
    protected ArrayList<String> disabledKey = new ArrayList<>();
    protected ArrayList<String> randomableList = new ArrayList<>();

    public void readConfigByPluginAutoSave() {
        PluginConfig config = PetPetAutoSaveConfig.INSTANCE.content.get();
//        System.out.println("从AutoSaveConfig中读出：" + ConfigDTOKt.encode(config));
        readPluginConfig(config);
    }

    private void readPluginConfig(PluginConfig config) {
        if (config.getVersion() != Petpet.VERSION) {
            System.out.println("配置文件可能已经过时，当前版本: " + Petpet.VERSION);
        }

        readBaseServiceConfig(PluginConfigKt.toBaseServiceConfig(config));

        command = config.getCommand();
        antialias = config.getAntialias();
        randomMax = config.getProbability();
        keyCommand = config.getKeyCommand();
        commandMustAt = config.getCommandMustAt();
        respondImage = config.getRespondImage();
        respondSelfNudge = config.getRespondSelfNudge();
        headless = config.getHeadless();

        for (String path : config.getDisabled()) {
            disabledKey.add(path.replace("\"", ""));
        }

        System.out.println("Petpet 初始化成功，使用 " + command + " 以生成GIF。");
    }

    @Override
    public void readData(File dir) {
        // 1. 所有key加载到dataMap
        readData(dir);
        // 2. 其中某些key加入randomableList
        dataMap.forEach((path, keyData) -> {
            if (!disabledKey.contains(path)
                    && !disabledKey.contains("Type." + keyData.getType())
                    && !disabledKey.contains("Avatar." + keyData.getAvatar())) {
                randomableList.add(path);
            }
        });

        randomMax = (int) (randomableList.size() / (randomMax * 0.01));
        System.out.println("Petpet 加载完毕 (共 " + randomableList.size() + " 素材，已排除 " + disabledKey.size() + " )");

    }


    public void sendImage(Group group, Member from, Member to) { //发送随机图片
        try {
            sendImage(group, from, to, randomableList.get(new Random().nextInt(randomableList.size())));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("图片素材应位于 Mirai/data/xmmt.dituon.petpet 目录下, 请检查路径");
        }
    }

    public void sendImage(Group group, Member from, Member to, boolean random) { //有概率发送随机图片
        if (!random) {
            sendImage(group, from, to);
            return;
        }
        int r = new Random().nextInt(randomMax);
        if (r >= randomableList.size()) {
            return;
        }
        sendImage(group, from, to, randomableList.get(r));
    }

    public void sendImage(Group group, Member from, Member to, String key) { //用key发送图片(无otherText)
        TextExtraData textExtraData = new TextExtraData(
                from.getNameCard().isEmpty() ? from.getNick() : from.getNameCard(),
                to.getNameCard().isEmpty() ? to.getNick() : to.getNameCard(),
                group.getName(), new ArrayList<>()
        );
        sendImage(group, from, from.getAvatarUrl(), to.getAvatarUrl(), key, textExtraData);
    }

    public void sendImage(Group group, Member from, Member to, String key, String otherText) { //用key发送图片，指定otherText
        TextExtraData textExtraData = new TextExtraData(
                from.getNameCard().isEmpty() ? from.getNick() : from.getNameCard(),
                to.getNameCard().isEmpty() ? to.getNick() : to.getNameCard(),
                group.getName(),
                otherText == null || otherText.equals("") ? new ArrayList<>() :
                        new ArrayList<>(Arrays.asList(otherText.split("\\s+")))
        );
        sendImage(group, from, from.getAvatarUrl(), to.getAvatarUrl(), key, textExtraData);
    }

    //发送图片
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
            System.out.println("发送图片时出错：" + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
