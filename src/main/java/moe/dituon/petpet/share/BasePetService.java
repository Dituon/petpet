package moe.dituon.petpet.share;

import kotlin.Pair;
import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class BasePetService {
    public static final float VERSION = 4.9F;
    public static final String FONTS_FOLDER = "fonts";
    protected boolean antialias = true;
    protected byte quality = 100;
    private List<Integer> gifMaxSize = null;
    public Encoder encoder = Encoder.ANIMATED_LIB;

    protected File dataRoot;
    protected HashMap<String, KeyData> dataMap = new HashMap<>();
    protected HashMap<String, String[]> aliaMap = new HashMap<>();
    public String keyListString;

    protected int gifMakerThreadPoolSize = Runtime.getRuntime().availableProcessors() + 1;
    protected BaseGifMaker gifMaker = new BaseGifMaker(gifMakerThreadPoolSize);
    protected BaseImageMaker imageMaker = new BaseImageMaker(gifMaker);

    public void readData(File[] files) {
        if (files == null || files.length == 0) {
            System.out.println("无法读取文件，请检查data目录");
            return;
        }
        this.dataRoot = files[0].getParentFile();

        StringBuilder keyListStringBuilder = new StringBuilder();
        for (File file : files) {
            if (file.getName().equals(FONTS_FOLDER)) {
                // load fonts folder
                registerFontsToAwt(file);
                continue;
            }
            // load templates folder
            // TODO 模板应放在data/templates而不是直接data
            File dataFile = new File(file.getPath() + File.separator + "data.json");
            try {
                KeyData data = KeyData.getData(getFileStr(dataFile));
                dataMap.put(file.getName(), data);
                if (Boolean.TRUE.equals(data.getHidden())) continue;

                keyListStringBuilder.append("\n").append(file.getName());
                if (data.getAlias() != null) {
                    keyListStringBuilder.append(" ( ");
                    data.getAlias().forEach((aliasKey) -> {
                        keyListStringBuilder.append(aliasKey).append(" ");
                        if (aliaMap.get(aliasKey) == null) {
                            aliaMap.put(aliasKey, new String[]{file.getName()});
                            return;
                        }
                        String[] oldArray = aliaMap.get(aliasKey);
                        String[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
                        newArray[oldArray.length] = file.getName();
                        aliaMap.put(aliasKey, newArray);
                    });
                    keyListStringBuilder.append(")");
                }
            } catch (Exception ex) {
                System.out.println("无法读取 " + file + "/data.json: \n\n" + ex);
            }
        }
        keyListString = keyListStringBuilder.toString();
    }

    private void registerFontsToAwt(File fontsFolder) {
        if (!fontsFolder.exists() || !fontsFolder.isDirectory()) {
            System.out.println("无fonts");
            return;
        }

        List<String> successNames = new ArrayList<>();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (File fontFile : Objects.requireNonNull(fontsFolder.listFiles())) {
            try {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                boolean success = ge.registerFont(customFont);
                if (success) {
                    successNames.add(fontFile.getName());
                } else {
                    System.out.println("registerFontsToAwt失败: " + fontFile.getName());
                }
            } catch (Exception e) {
                System.out.println("registerFontsToAwt异常: " + e);
            }
        }
        System.out.println("registerFontsToAwt成功: " + successNames);
    }


    public void readBaseServiceConfig(BaseServiceConfig config) {
        antialias = config.getAntialias();
    }

    public static String getFileStr(File file) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }
        br.close();
        return sb.toString();
    }


    /**
     * @return InputStream 及其图片格式（值域：["gif", "png"...]）
     * @deprecated 不支持gif, 没有改用多线程, 不建议使用
     */
    @Deprecated
    public Pair<InputStream, String> generateImage(
            String key, AvatarExtraDataProvider avatarExtraDataProvider,
            TextExtraData textExtraData,
            List<TextData> additionTextDatas
    ) {
        if (!dataMap.containsKey(key) && !aliaMap.containsKey(key)) {
            System.out.println("无效的key: “" + key + "”");
            return null;
        }
        KeyData data = dataMap.containsKey(key) ? dataMap.get(key) : dataMap.get(aliaMap.get(key)[0]);
        key = dataRoot.getAbsolutePath() + File.separator +
                (dataMap.containsKey(key) ? key : aliaMap.get(key)) + File.separator;

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

            ArrayList<AvatarModel> avatarList = new ArrayList<>();

            if (!data.getAvatar().isEmpty()) {
                for (AvatarData avatarData : data.getAvatar()) {
                    avatarList.add(new AvatarModel(avatarData, avatarExtraDataProvider, data.getType()));
                }
            }

            int delay = data.getDelay() != null ? data.getDelay() : 65;
            GifRenderParams renderParams = new GifRenderParams(
                    encoder, delay, gifMaxSize, antialias, quality);

            if (data.getType() == Type.GIF) {
                HashMap<Short, BufferedImage> stickerMap = new HashMap<>();
                short imageNum = 0;
                for (File file : Objects.requireNonNull(new File(key).listFiles())) {
                    if (!file.getName().endsWith(".png")) continue;
                    stickerMap.put(imageNum, ImageIO.read(new File(key + imageNum++ + ".png")));
                }
                if (data.getBackground() != null) { //从配置文件读背景
                    BufferedImage sticker = new BackgroundModel(data.getBackground(), avatarList, textList).getImage();
                    for (short i = 0; i < avatarList.get(0).getPosLength(); i++) {
                        stickerMap.put(i, sticker);
                    }
                }
                InputStream inputStream = gifMaker.makeGIF(
                        avatarList, textList, stickerMap, renderParams);
                return new Pair<>(inputStream, "gif");
            }

            if (data.getType() == Type.IMG) {
                BufferedImage sticker = getBackgroundImage(new File(key), data, avatarList, textList);
                assert sticker != null;
                InputStream inputStream = imageMaker.makeImage(
                        avatarList, textList, sticker, renderParams);
                return new Pair<>(inputStream, "png");
            }
        } catch (Exception ex) {
            System.out.println("解析 " + key + "/data.json 出错");
            ex.printStackTrace();
        }
        throw new RuntimeException();
    }

    /**
     * @return InputStream 及其图片格式（值域：["gif", "png"...]）
     */
    public Pair<InputStream, String> generateImage(
            String key, GifAvatarExtraDataProvider gifAvatarExtraDataProvider,
            TextExtraData textExtraData,
            List<TextData> additionTextDatas
    ) {
        if (!dataMap.containsKey(key) && !aliaMap.containsKey(key)) {
            System.out.println("无效的key: “" + key + "”");
            return null;
        }
        KeyData data = dataMap.containsKey(key) ? dataMap.get(key) : dataMap.get(aliaMap.get(key)[0]);
        key = dataRoot.getAbsolutePath() + File.separator +
                (dataMap.containsKey(key) ? key : aliaMap.get(key)) + File.separator;

        try {
            ArrayList<TextModel> textList = new ArrayList<>();
            // add from KeyData
            if (!data.getText().isEmpty()) {
                data.getText().forEach(textElement ->
                        textList.add(new TextModel(textElement, textExtraData))
                );
            }
            // add from params
            if (additionTextDatas != null) {
                additionTextDatas.forEach(textElement ->
                        textList.add(new TextModel(textElement, textExtraData))
                );
            }

            ArrayList<AvatarModel> avatarList = new ArrayList<>();

            if (!data.getAvatar().isEmpty()) {
                data.getAvatar().forEach(avatarData ->
                        avatarList.add(new AvatarModel(avatarData, gifAvatarExtraDataProvider, data.getType()))
                );
            }

            int delay = data.getDelay() != null ? data.getDelay() : 65;
            GifRenderParams renderParams = new GifRenderParams(
                    encoder, delay, gifMaxSize, antialias, quality);

            if (data.getType() == Type.GIF) {
                HashMap<Short, BufferedImage> stickerMap = new HashMap<>();
                short imageNum = 0;
                for (File file : Objects.requireNonNull(new File(key).listFiles())) {
                    if (!file.getName().endsWith(".png")) continue;
                    stickerMap.put(imageNum, ImageIO.read(new File(key + imageNum++ + ".png")));
                }

                if (data.getBackground() != null) { //从配置文件读背景
                    BufferedImage sticker = new BackgroundModel(data.getBackground(), avatarList, textList).getImage();
                    for (short i = 0; i < avatarList.get(0).getPosLength(); i++) {
                        stickerMap.put(i, sticker);
                    }
                }
                InputStream inputStream = gifMaker.makeGIF(
                        avatarList, textList, stickerMap, renderParams);
                return new Pair<>(inputStream, "gif");
            }

            if (data.getType() == Type.IMG) {
                BufferedImage sticker = getBackgroundImage(new File(key), data, avatarList, textList);
                assert sticker != null;
                InputStream inputStream = imageMaker.makeImage(
                        avatarList, textList, sticker, renderParams);
                return new Pair<>(inputStream, "png");
            }
        } catch (Exception ex) {
            System.out.println("解析 " + key + "/data.json 出错");
            ex.printStackTrace();
        }
        throw new RuntimeException();
    }

    private BufferedImage getBackgroundImage(File path, KeyData data,
                                             ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList) throws IOException {
        if (!path.isDirectory() || !path.exists()) return null;
        File[] files = path.listFiles();
        assert files != null;
        List<File> fileList = Arrays.stream(files).filter(file -> file.getName().endsWith(".png")).collect(Collectors.toList());
        if (fileList.isEmpty() && data.getBackground() == null) { //没有背景图片和背景配置
            throw new FileNotFoundException("找不到" + path.getName() + "背景文件");
        }
        if (fileList.isEmpty() && data.getBackground() != null) { //无背景图片(读取背景配置
            return new BackgroundModel(data.getBackground(), avatarList, textList).getImage();
        }
        if (data.getBackground() == null) { //无背景配置(读取随机背景图片
            return ImageIO.read(fileList.get(new Random().nextInt(fileList.size())));
        }
        //有配置项和图片
        return new BackgroundModel(data.getBackground(), avatarList, textList,
                ImageIO.read(fileList.get(new Random().nextInt(fileList.size())))).getImage();
    }

    public static Color decodeColor(JsonElement jsonElement) {
        return decodeColor(jsonElement, new short[]{255, 255, 255, 255}); //#fff
    }

    public static Color decodeColor(JsonElement jsonElement, short[] defaultRgba) {
        if (jsonElement == null) return new Color(defaultRgba[0], defaultRgba[1], defaultRgba[2], defaultRgba[3]);
        assert defaultRgba.length == 4;
        try { //rgb or rgba
            JsonArray jsonArray = (JsonArray) jsonElement;
            if (jsonArray.getSize() == 3 || jsonArray.getSize() == 4) {
                defaultRgba[0] = Short.parseShort(jsonArray.get(0).toString());
                defaultRgba[1] = Short.parseShort(jsonArray.get(1).toString());
                defaultRgba[2] = Short.parseShort(jsonArray.get(2).toString());
                defaultRgba[3] = jsonArray.getSize() == 4 ? Short.parseShort(jsonArray.get(3).toString()) : 255;
            }
        } catch (Exception ignored) { //hex
            String hex = jsonElement.toString().replace("#", "").replace("\"", "");
            if (hex.length() != 6 && hex.length() != 8) {
                System.out.println("颜色格式有误，请输入正确的16进制颜色\n输入: " + hex);
                return new Color(defaultRgba[0], defaultRgba[1], defaultRgba[2], defaultRgba[3]);
            }
            defaultRgba[0] = Short.parseShort(hex.substring(0, 2), 16);
            defaultRgba[1] = Short.parseShort(hex.substring(2, 4), 16);
            defaultRgba[2] = Short.parseShort(hex.substring(4, 6), 16);
            defaultRgba[3] = hex.length() == 8 ? Short.parseShort(hex.substring(6, 8), 16) : 255;
        }
        return new Color(defaultRgba[0], defaultRgba[1], defaultRgba[2], defaultRgba[3]);
    }

    public HashMap<String, KeyData> getDataMap() {
        return dataMap;
    }

    public HashMap<String, String[]> getAliaMap() {
        return aliaMap;
    }

    public void setGifMaxSize(List<Integer> maxSize) {
        if (maxSize == null || maxSize.isEmpty()) return;
        if (maxSize.size() > 3) {
            System.out.println("GifMaxSize无效: Length Must <= 3");
            return;
        }
        if (maxSize.size() == 1) maxSize.add(maxSize.get(0));
        if (maxSize.size() == 2) maxSize.add(null);
        gifMaxSize = maxSize.stream()
                .map(i -> i = i != null && i <= 0 ? null : i)
                .collect(Collectors.toList());
    }

    public List<Integer> getGifMaxSize() {
        return gifMaxSize;
    }

    public void setGifMakerThreadPoolSize(int size) {
        assert size >= 0;
        gifMakerThreadPoolSize = size == 0 ? gifMakerThreadPoolSize : size;
        gifMaker = new BaseGifMaker(gifMakerThreadPoolSize);
        imageMaker = new BaseImageMaker(gifMaker);
    }

    public int getGifMakerThreadPoolSize() {
        return gifMakerThreadPoolSize;
    }
}
