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
import java.util.List;
import java.util.Random;

public class PluginPetService extends BasePetService {

    protected String command = "pet";
    private short probability;
    protected String commandHead = "";
    protected boolean respondSelfNudge = false;
    protected boolean respondReply = true;
    protected int cachePoolSize = 10000;
    protected ReplyFormat replyFormat = ReplyFormat.MESSAGE;
    protected boolean fuzzy = false;
    protected boolean strictCommand = true;
    protected boolean messageSynchronized = false;
    protected boolean headless = true;
    protected boolean autoUpdate = true;
    protected List<String> updateIgnore = new ArrayList<>();
    protected String repositoryUrl = "https://dituon.github.io/petpet";
    protected ArrayList<String> disabledKey = new ArrayList<>();
    protected ArrayList<String> randomableList = new ArrayList<>();

    public void readConfigByPluginAutoSave() {
        PluginConfig config = PetPetAutoSaveConfig.INSTANCE.content.get();
//        System.out.println("从AutoSaveConfig中读出：" + ConfigDTOKt.encode(config));
        readPluginConfig(config);
    }

    private void readPluginConfig(PluginConfig config) {
        readBaseServiceConfig(PluginConfigKt.toBaseServiceConfig(config));

        command = config.getCommand();
        antialias = config.getAntialias();
        probability = config.getProbability();
        commandHead = config.getCommandHead();
        respondSelfNudge = config.getRespondSelfNudge();
        respondReply = config.getRespondReply();
        cachePoolSize = config.getCachePoolSize() != null ? config.getCachePoolSize() : 10000;
        replyFormat = config.getKeyListFormat();
        fuzzy = config.getFuzzy();
//        strictCommand = config.getStrictCommand();
        messageSynchronized = config.getSynchronized();
        headless = config.getHeadless();
        autoUpdate = config.getAutoUpdate();
        updateIgnore = config.getUpdateIgnore();
        repositoryUrl = config.getRepositoryUrl();

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
        GifAvatarExtraDataProvider gifAvatarExtraDataProvider = BaseConfigFactory.getGifAvatarExtraDataFromUrls(
                from.getAvatarUrl(), to.getAvatarUrl(), group.getAvatarUrl(), group.getBotAsMember().getAvatarUrl()
        );
        sendImage(group, key, gifAvatarExtraDataProvider, textExtraData);
    }

    /**
     * 发送图片
     * @deprecated 使用GifAvatarExtraDataProvider以保证对Gif格式的解析
     */
    @Deprecated
    public void sendImage(Group group, String key,
                          AvatarExtraDataProvider avatarExtraDataProvider, TextExtraData textExtraData) {

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

    public void sendImage(Group group, String key,
                          GifAvatarExtraDataProvider gifAvatarExtraDataProvider, TextExtraData textExtraData){
        Pair<InputStream, String> generatedImageAndType = generateImage(key, gifAvatarExtraDataProvider,
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
        return keyListString;
    }
}