package moe.dituon.petpet.share;

import kotlin.Pair;
import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;


public class BasePetService {
    public static final float VERSION = 5.2F;
    public static final String FONTS_FOLDER = "fonts";
    protected boolean antialias = true;
    protected int quality = 10;
    private List<Integer> gifMaxSize = null;
    public Encoder encoder = Encoder.ANIMATED_LIB;

    public File dataRoot = null;
    protected HashMap<String, KeyData> dataMap = new HashMap<>();
    protected HashMap<String, String[]> aliaMap = new HashMap<>();
    protected HashMap<String, Callable<Map<Short, BufferedImage>>> backgroundLambdaMap = new HashMap<>();
    public String keyListString = "";

    protected int gifMakerThreadPoolSize = Runtime.getRuntime().availableProcessors() + 1;
    protected BaseGifMaker gifMaker = new BaseGifMaker(gifMakerThreadPoolSize);
    protected BaseImageMaker imageMaker = new BaseImageMaker(gifMaker);
    public static final Random random = new Random();

    /**
     * 从文件中读取KeyData模板到dataMap中
     *
     * @param files KeyData目录
     */
    public void readData(File[] files) {
        if (files == null || files.length == 0) {
            System.out.println("无法读取文件，请检查data目录");
            return;
        }
        this.dataRoot = files[0].getParentFile();

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
                putKeyData(file.getName(), data);
            } catch (Exception ex) {
                System.out.println("无法读取 " + file + "/data.json: \n\n" + ex);
            }
        }
    }

    /**
     * 将KeyData添加到dataMap中, 并更新aliaMap, keyListString
     * <br/>
     * <b>使用前 必须调用 readData() 方法指定父目录以读取背景图片</b>
     *
     * @param key 索引
     */
    public void putKeyData(String key, KeyData data) {
        dataMap.put(key, data);

        assert dataRoot != null;
        backgroundLambdaMap.put(key, () -> { //使用Lambda实现按需加载, 减少内存占用
            String path = dataRoot.getAbsolutePath() + File.separator +
                    (dataMap.containsKey(key) ? key : aliaMap.get(key)) + File.separator;
            File[] files = new File(path).listFiles();
            if (files == null) throw new RuntimeException("无法读取 " + path + " 目录");

            Map<Short, BufferedImage> backgroundMap = new HashMap<>(files.length);
            short imageNum = 0;
            for (File file : files) {
                if (!file.getName().endsWith(".png")) continue;
                backgroundMap.put(imageNum, ImageIO.read(new File(path + imageNum++ + ".png")));
            }
            return backgroundMap;
        });

        if (!Boolean.TRUE.equals(data.getHidden())) putAlia(key, data);
    }

    /**
     * 将KeyData添加到dataMap中, 指定背景, 并更新 aliaMap&keyListString
     *
     * @param key 索引
     */
    public void putKeyData(String key, KeyData data, BufferedImage background) {
        putKeyData(key, data, List.of(background));
    }

    /**
     * 将KeyData添加到dataMap中, 指定背景表列, 并更新 aliaMap&keyListString
     *
     * @param key 索引
     */
    public void putKeyData(String key, KeyData data, List<BufferedImage> backgroundList) {
        dataMap.put(key, data);

        Map<Short, BufferedImage> backgroundMap = new HashMap<>(backgroundList.size() + 1);
        short imageNum = 0;
        for (var background : backgroundList) {
            backgroundMap.put(imageNum++, background);
        }
        backgroundLambdaMap.put(key, () -> backgroundMap);

        if (!Boolean.TRUE.equals(data.getHidden())) putAlia(key, data);
    }

    private void putAlia(String key, KeyData data) {
        StringBuilder keyStringBuilder = new StringBuilder();
        keyStringBuilder.append("\n").append(key);
        if (data.getAlias() != null) {
            keyStringBuilder.append(" ( ");
            data.getAlias().forEach((aliasKey) -> {
                keyStringBuilder.append(aliasKey).append(" ");
                if (aliaMap.get(aliasKey) == null) {
                    aliaMap.put(aliasKey, new String[]{key});
                    return;
                }
                String[] oldArray = aliaMap.get(aliasKey);
                String[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
                newArray[oldArray.length] = key;
                aliaMap.put(aliasKey, newArray);
            });
            keyStringBuilder.append(")");
        }
        keyListString += keyStringBuilder.toString();
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
        setGifMaxSize(config.getGifMaxSize());
        encoder = config.getGifEncoder();
        quality = config.getGifQuality();
        setGifMakerThreadPoolSize(config.getThreadPoolSize());
        if (config.getHeadless()) System.setProperty("java.awt.headless", "true");
    }

    public String getFileStr(File file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return InputStream 及其图片格式（值域：["gif", "png"...]）
     */
    public Pair<InputStream, String> generateImage(
            @NotNull String key,
            GifAvatarExtraDataProvider gifAvatarExtraDataProvider,
            TextExtraData textExtraData,
            List<TextData> additionTextDataList
    ) {
        if (!dataMap.containsKey(key) && !aliaMap.containsKey(key)) {
            throw new RuntimeException("无效的key: “" + key + "”");
        }
        KeyData data = dataMap.containsKey(key) ? dataMap.get(key) : dataMap.get(aliaMap.get(key)[0]);
        try {
            ArrayList<TextModel> textList = new ArrayList<>();
            // add from KeyData
            if (!data.getText().isEmpty()) {
                data.getText().forEach(textElement ->
                        textList.add(new TextModel(textElement, textExtraData))
                );
            }
            // add from params
            if (additionTextDataList != null) {
                additionTextDataList.forEach(textElement ->
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
                    encoder, delay, gifMaxSize, antialias, quality,
                    Boolean.TRUE.equals(data.getReverse())
            );

            if (data.getType() == Type.GIF) {
                Map<Short, BufferedImage> stickerMap = backgroundLambdaMap.get(key).call();

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
                BufferedImage sticker = getBackgroundImage(key, data, avatarList, textList);
                assert sticker != null;
                InputStream inputStream = imageMaker.makeImage(
                        avatarList, textList, sticker, renderParams);
                return new Pair<>(inputStream, "png");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("解析 " + key + "/data.json 出错");
        }
        throw new RuntimeException();
    }

    private BufferedImage getBackgroundImage(
            @NotNull String key,
            KeyData data,
            ArrayList<AvatarModel> avatarList,
            ArrayList<TextModel> textList
    ) throws Exception {
        var backgroundMap = backgroundLambdaMap.get(key).call();
        if (backgroundMap.isEmpty() && data.getBackground() == null) { //没有背景图片和背景配置
            throw new FileNotFoundException("找不到 " + key + " 背景文件");
        }
        if (backgroundMap.isEmpty() && data.getBackground() != null) { //无背景图片(读取背景配置
            return new BackgroundModel(data.getBackground(), avatarList, textList).getImage();
        }
        assert !backgroundMap.isEmpty();
        var background = backgroundMap.get(
                (short) random.nextInt(backgroundMap.keySet().size())
        );

        if (data.getBackground() == null) return background;  //无背景配置(读取随机背景图片

        //有配置项和图片
        return new BackgroundModel(
                data.getBackground(), avatarList, textList, background
        ).getImage();
    }

    public static Color decodeColor(@NotNull JsonElement jsonElement) {
        return decodeColor(jsonElement, new short[]{255, 255, 255, 255}); //#fff
    }

    /**
     * 解析 RGB / RGBA / HEX 颜色, <b>可能更改原数组</b>
     */
    public static Color decodeColor(@NotNull JsonElement jsonElement, short[] defaultRgba) {
        assert defaultRgba.length == 4;
//        defaultRgba = defaultRgba.clone();
        if (jsonElement instanceof JsonArray) {
            JsonArray jsonArray = (JsonArray) jsonElement;
            if (jsonArray.getSize() != 3 && jsonArray.getSize() != 4) {
                System.err.println("颜色格式有误，请输入正确的 RGB / RGBA 颜色数组\n输入: " + jsonArray.toString());
                return new Color(defaultRgba[0], defaultRgba[1], defaultRgba[2], defaultRgba[3]);
            }
            defaultRgba[0] = Short.parseShort(jsonArray.get(0).toString());
            defaultRgba[1] = Short.parseShort(jsonArray.get(1).toString());
            defaultRgba[2] = Short.parseShort(jsonArray.get(2).toString());
            defaultRgba[3] = jsonArray.getSize() == 4 ? Short.parseShort(jsonArray.get(3).toString()) : 255;
        } else if (jsonElement instanceof JsonPrimitive) {
            String hex = ((JsonPrimitive) jsonElement).getContent().replace("#", "").replace("\"", "");
            if (hex.length() != 6 && hex.length() != 8) {
                System.err.println("颜色格式有误，请输入正确的16进制颜色\n输入: " + hex);
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
    public BaseGifMaker getGifMaker(){
        return gifMaker;
    }
    public BaseImageMaker getImageMaker(){
        return imageMaker;
    }
}
