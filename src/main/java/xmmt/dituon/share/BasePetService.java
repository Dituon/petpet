package xmmt.dituon.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonObject;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
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
    public HashMap<String, DataJSON> dataMap = new HashMap<>();

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
                DataJSON data = ConfigDTOKt.getData(getFileStr(dataFile));
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

    public InputStream generateImage(BufferedImage fromAvatarImage, BufferedImage toAvatarImage, String key){
        return generateImage(fromAvatarImage, toAvatarImage, key, new String[]{"我","你","你群"});
    }

    public InputStream generateImage(BufferedImage fromAvatarImage, BufferedImage toAvatarImage, String[] info) {
        return generateImage(fromAvatarImage, toAvatarImage, keyList.get(new Random().nextInt(keyList.size())), info);
    }

    public InputStream generateImage(BufferedImage fromAvatarImage, BufferedImage toAvatarImage, String key, String[] info) {
        if (!dataMap.containsKey(key)) {
            System.out.println("无效的key: " + key);
            return generateImage(fromAvatarImage, toAvatarImage ,info);
        }
        DataJSON data = dataMap.get(key);
        key = dataRoot.getAbsolutePath() + File.separator + key + File.separator;

        try {
            ArrayList<Text> textList = null;
            if (!data.getText().isEmpty()) {
                textList = new ArrayList<>();
                for (JsonElement textElement : data.getText()) {
                    JsonObject textObj = (JsonObject) textElement;
                    textList.add(new Text(textObj, info));
                }
            }

            if (data.getType() == Type.GIF) {
                if (data.getAvatar() == Avatar.SINGLE) {
                    int[][] pos = new int[data.getPos().getSize()][4];

                    int i = 0;
                    for (JsonElement je : data.getPos()) {
                        pos[i++] = JsonArrayToIntArray((JsonArray) je);
                    }

                    return gifMaker.makeOneAvatarGIF(toAvatarImage, key, pos,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound(), antialias, textList);
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

                    return gifMaker.makeTwoAvatarGIF(fromAvatarImage, toAvatarImage, key, fromPos, toPos,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound(), antialias, textList);
                }
            }

            if (data.getType() == Type.IMG) {
                if (data.getAvatar() == Avatar.SINGLE) {
                    int[] pos = JsonArrayToIntArray(data.getPos());

                    return imageMaker.makeOneAvatarImage(toAvatarImage, key, pos,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound(), antialias, textList);
                }
                if (data.getAvatar() == Avatar.DOUBLE) {
                    int[] pos1 = JsonArrayToIntArray((JsonArray) data.getPos().get(0));
                    int[] pos2 = JsonArrayToIntArray((JsonArray) data.getPos().get(1));

                    return imageMaker.makeTwoAvatarImage(fromAvatarImage, toAvatarImage, key, pos1, pos2,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound(), antialias, textList);
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
