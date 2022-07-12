package xmmt.dituon.plugin;

import kotlin.Pair;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import xmmt.dituon.share.*;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class PluginPetService extends BasePetService {

    protected String command = "pet";
    private int probability;
    protected boolean keyCommand = true;
    protected String keyCommandHead = "";
    protected boolean commandMustAt = false;
    protected boolean respondImage = true;
    protected boolean respondSelfNudge = false;
    protected boolean fuzzy = false;
    protected boolean headless = true;
    protected boolean autoUpdate = true;
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
        probability = config.getProbability();
        keyCommand = config.getKeyCommand();
        keyCommandHead = config.getKeyCommandHead();
        commandMustAt = config.getCommandMustAt();
        respondImage = config.getRespondImage();
        respondSelfNudge = config.getRespondSelfNudge();
        fuzzy = config.getFuzzy();
        headless = config.getHeadless();
        autoUpdate = config.getAutoUpdate();

        for (String path : config.getDisabled()) {
            disabledKey.add(path.replace("\"", ""));
        }

        System.out.println("Petpet 初始化成功，使用 " + command + " 以生成GIF。");
    }

    @Override
    public void readData(File dir) {
        // 1. 所有key加载到dataMap
        super.readData(dir);
        // 2. 其中某些key加入randomableList
        dataMap.forEach((path, keyData) -> {
            if (!disabledKey.contains(path)
                    && !disabledKey.contains("Type." + keyData.getType())
                    && Boolean.TRUE.equals(super.dataMap.get(path).getInRandomList())) {
                randomableList.add(path);
            }
        });

        System.out.println("Petpet 加载完毕 (共 " + dataMap.size() + " 素材，已排除 " +
                (dataMap.size() - randomableList.size()) + " )");
    }


    /**
     * 发送随机图片
     */
    public void sendImage(Group group, Member from, Member to) { //发送随机图片
        sendImage(group, from, to, randomableList.get(new Random().nextInt(randomableList.size())));
    }

    /**
     * 有概率发送随机图片
     */
    public void sendImage(Group group, Member from, Member to, boolean random) {
        if (!random) {
            sendImage(group, from, to);
            return;
        }
        int r = new Random().nextInt(99) + 1; //不能为0
        if (r >= probability) return;
        sendImage(group, from, to);
    }

    /**
     * 用key发送图片(无otherText)
     */
    @Deprecated
    public void sendImage(Group group, Member from, Member to, String key) {
        sendImage(group, from, to, key, null);
    }

    /**
     * 用key发送图片，指定otherText
     */
    @Deprecated
    public void sendImage(Group group, Member from, Member to, String key, String otherText) {
        TextExtraData textExtraData = new TextExtraData(
                from.getNameCard().isEmpty() ? from.getNick() : from.getNameCard(),
                to.getNameCard().isEmpty() ? to.getNick() : to.getNameCard(),
                group.getName(),
                otherText == null || otherText.equals("") ? new ArrayList<>() :
                        new ArrayList<>(Arrays.asList(otherText.split("\\s+")))
        );
        AvatarExtraDataProvider avatarExtraDataProvider = BaseConfigFactory.getAvatarExtraDataFromUrls(
                from.getAvatarUrl(), to.getAvatarUrl(), group.getAvatarUrl(), group.getBotAsMember().getAvatarUrl()
        );
        sendImage(group, key, avatarExtraDataProvider, textExtraData);
    }

    /**
     * 发送图片
     */
    public void sendImage(Group group, String key, AvatarExtraDataProvider avatarExtraDataProvider, TextExtraData textExtraData) {

        Pair<InputStream, String> generatedImageAndType = generateImage(key, avatarExtraDataProvider,
                textExtraData, null);

        try {
            if (generatedImageAndType != null) {
                ExternalResource resource = ExternalResource.create(generatedImageAndType.getFirst());
                Image image = group.uploadImage(resource);
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

    public String getKeyAliasListString() {
        return super.keyListString;
    }
}