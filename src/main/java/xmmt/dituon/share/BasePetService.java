package xmmt.dituon.share;

import kotlin.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class BasePetService {
    public static final String FONTS_FOLDER = "fonts";
    protected boolean antialias = true;

    protected File dataRoot;
    protected HashMap<String, KeyData> dataMap = new HashMap<>();
    protected HashMap<String, String[]> aliaMap = new HashMap<>();

    protected BaseImageMaker imageMaker;
    protected BaseGifMaker gifMaker;

    public static String keyListString;

    public void readData(File dir) {
        this.dataRoot = dir;
        String[] children = dir.list();

        if (children == null) {
            System.out.println("无法读取文件，请检查data目录");
            return;
        }

        StringBuilder keyListStringBuilder = new StringBuilder();
        for (String path : children) {
            if (path.equals(FONTS_FOLDER)) {
                // load fonts folder
                registerFontsToAwt(new File(dir.getAbsolutePath() + File.separator + path));
                continue;
            }
            // load templates folder
            // TODO 模板应放在data/templates而不是直接data
            File dataFile = new File(dir.getAbsolutePath() + File.separator + path + "/data.json");
            try {
                KeyData data = KeyData.getData(getFileStr(dataFile));
                dataMap.put(path, data);
                if (Boolean.TRUE.equals(data.getHidden())) continue;

                keyListStringBuilder.append("\n").append(path);
                if (data.getAlias() != null) {
                    keyListStringBuilder.append(" ( ");
                    data.getAlias().forEach((aliasKey) -> {
                        keyListStringBuilder.append(aliasKey).append(" ");
                        if (aliaMap.get(aliasKey) == null) {
                            aliaMap.put(aliasKey, new String[]{path});
                            return;
                        }
                        String[] oldArray = aliaMap.get(aliasKey);
                        String[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
                        newArray[oldArray.length] = path;
                        aliaMap.put(aliasKey, newArray);
                    });
                    keyListStringBuilder.append(")");
                }
            } catch (Exception ex) {
                System.out.println("无法读取 " + path + "/data.json: \n\n" + ex);
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
            try (InputStream inputStream = new FileInputStream(fontFile)) {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
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
                InputStream inputStream = BaseGifMaker.makeGIF(avatarList, textList, stickerMap, antialias);
                return new Pair<>(inputStream, "gif");
            }

            if (data.getType() == Type.IMG) {
                BufferedImage sticker = getBackgroundImage(new File(key), data, avatarList, textList);
                assert sticker != null;
                InputStream inputStream = BaseImageMaker.makeImage(avatarList, textList, sticker, antialias);
                return new Pair<>(inputStream, "png");
            }
        } catch (Exception ex) {
            System.out.println("解析 " + key + "/data.json 出错");
            ex.printStackTrace();
        }
        return null;
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
                    avatarList.add(new AvatarModel(avatarData, gifAvatarExtraDataProvider, data.getType()));
                }
            }

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
                InputStream inputStream = BaseGifMaker.makeGIF(avatarList, textList, stickerMap, antialias);
                return new Pair<>(inputStream, "gif");
            }

            if (data.getType() == Type.IMG) {
                BufferedImage sticker = getBackgroundImage(new File(key), data, avatarList, textList);
                assert sticker != null;
                InputStream inputStream = BaseImageMaker.makeImage(avatarList, textList, sticker, antialias);
                return new Pair<>(inputStream, "png");
            }
        } catch (Exception ex) {
            System.out.println("解析 " + key + "/data.json 出错");
            ex.printStackTrace();
        }
        return null;
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

    public HashMap<String, KeyData> getDataMap() {
        return dataMap;
    }

    public HashMap<String, String[]> getAliaMap() {
        return aliaMap;
    }
}
