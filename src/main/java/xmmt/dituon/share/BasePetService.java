package xmmt.dituon.share;

import kotlin.Pair;
import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class BasePetService {

    public boolean antialias = false;
    public String command = "pet";
    public int randomMax = 40;
    public boolean keyCommand = false;
    public boolean respondImage = false;
    public File dataRoot;
    public ArrayList<String> disabledKey = new ArrayList<>();
    public ArrayList<String> keyList = new ArrayList<>();
    public HashMap<String, KeyData> dataMap = new HashMap<>();

    protected BaseImageMaker imageMaker;
    protected BaseGifMaker gifMaker;

    public BasePetService() {
        this.imageMaker = new BaseImageMaker();
        this.gifMaker = new BaseGifMaker();
    }

    public void readData(File dir) {
        this.dataRoot = dir;
        String[] children = dir.list();

        if (children == null) {
            System.out.println("无法读取文件，请检查data目录");
            return;
        }

        for (String path : children) {

            File dataFile = new File(dir.getAbsolutePath() + File.separator + path + "/data.json");
            try {
                KeyData data = ConfigDTOKt.getData(getFileStr(dataFile));
                if (!disabledKey.contains(path)
                        && !disabledKey.contains("Type." + data.getType())
                        && !disabledKey.contains("Avatar." + data.getAvatar())) {
                    keyList.add(path);
                }
                dataMap.put(path, data);
            } catch (Exception ex) {
                System.out.println("无法读取 " + path + "/data.json: \n\n" + ex);
            }
        }

        randomMax = (int) (keyList.size() / (randomMax * 0.01));
        System.out.println("Petpet 加载完毕 (共 " + keyList.size() + " 素材，已排除 " + disabledKey.size() + " )");
    }


    public void readConfig(ConfigDTO config) {
        command = config.getCommand();
        antialias = config.getAntialias();
        randomMax = config.getProbability();
        keyCommand = config.getKeyCommand();
        respondImage = config.getRespondImage();

        for (String path : config.getDisabled()) {
            disabledKey.add(path.replace("\"", ""));
        }

        System.out.println("Petpet 初始化成功，使用 " + command + " 以生成GIF。");
    }

    public String getFileStr(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }
        br.close();
        return sb.toString();
    }

    public Pair<InputStream, String> generateImage(BufferedImage fromAvatarImage, BufferedImage toAvatarImage, String key){
        return generateImage(fromAvatarImage, toAvatarImage, key, new TextExtraData("我","你","你群"), null);
    }

    public Pair<InputStream, String> generateImage(BufferedImage fromAvatarImage, BufferedImage toAvatarImage, TextExtraData textExtraData) {
        return generateImage(fromAvatarImage, toAvatarImage, keyList.get(new Random().nextInt(keyList.size())), textExtraData, null);
    }

    /**
     * @return InputStream 及其图片格式（值域：["gif", "png"...]）
     */
    public Pair<InputStream, String> generateImage(
            BufferedImage fromAvatarImage, BufferedImage toAvatarImage,
            String key,
            TextExtraData textExtraData,
            List<TextData> additionTextDatas
    ) {
        if (!dataMap.containsKey(key)) {
            System.out.println("无效的key: " + key);
            return generateImage(fromAvatarImage, toAvatarImage, textExtraData);
        }
        KeyData data = dataMap.get(key);
        key = dataRoot.getAbsolutePath() + File.separator + key + File.separator;

        try {
            ArrayList<TextModel> textList = new ArrayList<>();
            // add from KeyData
            if (!data.getText().isEmpty()) {
                for (TextData textElement : data.getText()) {
                    textList.add(new TextModel(textElement, textExtraData));
                }
            }
            // add from params
            if (additionTextDatas != null) {
                for (TextData textElement : additionTextDatas) {
                    textList.add(new TextModel(textElement, textExtraData));
                }
            }

            if (data.getType() == Type.GIF) {
                if (data.getAvatar() == Avatar.SINGLE) {
                    int[][] pos = new int[data.getPos().getSize()][4];

                    int i = 0;
                    for (JsonElement je : data.getPos()) {
                        pos[i++] = JsonArrayToIntArray((JsonArray) je);
                    }

                    InputStream inputStream = gifMaker.makeOneAvatarGIF(toAvatarImage, key, pos,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound(), antialias, textList);
                    return new Pair<>(inputStream, "gif");
                }
                if (data.getAvatar() == Avatar.DOUBLE) {
                    JsonArray fromJa = (JsonArray) data.getPos().get(0);
                    JsonArray toJa = (JsonArray) data.getPos().get(1);

                    int[][] fromPos = new int[fromJa.getSize()][4];
                    int[][] toPos = new int[toJa.getSize()][4];

                    int i = 0;
                    for (JsonElement fromJe : fromJa) {
                        fromPos[i++] = JsonArrayToIntArray((JsonArray) fromJe);
                    }
                    i = 0;
                    for (JsonElement toJe : toJa) {
                        toPos[i++] = JsonArrayToIntArray((JsonArray) toJe);
                    }

                    InputStream inputStream = gifMaker.makeTwoAvatarGIF(fromAvatarImage, toAvatarImage, key, fromPos, toPos,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound(), antialias, textList);
                    return new Pair<>(inputStream, "gif");
                }
            }

            if (data.getType() == Type.IMG) {
                if (data.getAvatar() == Avatar.NONE) {
                    InputStream inputStream = imageMaker.makeNoneAvatarImage(key, textList);
                    return new Pair<>(inputStream, "png");
                }
                if (data.getAvatar() == Avatar.SINGLE) {
                    int[] pos = JsonArrayToIntArray(data.getPos());

                    InputStream inputStream = imageMaker.makeOneAvatarImage(toAvatarImage, key, pos,
                            data.getAvatarOnTop(), data.getRound(), antialias, textList);
                    return new Pair<>(inputStream, "png");
                }
                if (data.getAvatar() == Avatar.DOUBLE) {
                    int[] pos1 = JsonArrayToIntArray((JsonArray) data.getPos().get(0));
                    int[] pos2 = JsonArrayToIntArray((JsonArray) data.getPos().get(1));

                    InputStream inputStream = imageMaker.makeTwoAvatarImage(fromAvatarImage, toAvatarImage, key, pos1, pos2,
                            data.getAvatarOnTop(), data.getRound(), antialias, textList);
                    return new Pair<>(inputStream, "png");
                }
            }
        } catch (Exception ex) {
            System.out.println("解析 " + key + "/data.json 出错");
            ex.printStackTrace();
        }
        return null;
    }

    private int[] JsonArrayToIntArray(JsonArray ja) {
        return new int[]{
                Integer.parseInt(ja.get(0).toString()),
                Integer.parseInt(ja.get(1).toString()),
                Integer.parseInt(ja.get(2).toString()),
                Integer.parseInt(ja.get(3).toString())
        };
    }
}
