package xmmt.dituon.share;

import kotlin.Pair;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class BasePetService {
    private static final String FONTS_FOLDER = "fonts";
    protected boolean antialias = true;

    protected File dataRoot;
    protected HashMap<String, KeyData> dataMap = new HashMap<>();
    protected HashMap<String, String> aliaMap = new HashMap<>();

    protected BaseImageMaker imageMaker;
    protected BaseGifMaker gifMaker;

    protected String keyListString;

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

        StringBuilder keyListStringBuilder = new StringBuilder();
        for (String path : children) {
            if (path.equals(FONTS_FOLDER)) {
                // load fonts folder
                registerFontsToAwt(new File(dir.getAbsolutePath() + File.separator + path));
            } else {
                // load templates folder
                // TODO 模板应放在data/templates而不是直接data
                File dataFile = new File(dir.getAbsolutePath() + File.separator + path + "/data.json");
                try {
                    KeyData data = KeyData.getData(getFileStr(dataFile));
                    dataMap.put(path, data);
                    keyListStringBuilder.append("\n").append(path);
                    if (data.getAlias() != null) {
                        keyListStringBuilder.append(" ( ");
                        data.getAlias().forEach((aliasKey) -> {
                            aliaMap.put(aliasKey, path);
                            keyListStringBuilder.append(aliasKey).append(" ");
                        });
                        keyListStringBuilder.append(")");
                    }
                } catch (Exception ex) {
                    System.out.println("无法读取 " + path + "/data.json: \n\n" + ex);
                }
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

    public String getFileStr(File file) throws IOException {
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
    public Pair<InputStream, String> generateImage(
            String key, AvatarExtraDataProvider avatarExtraDataProvider,
            TextExtraData textExtraData,
            List<TextData> additionTextDatas
    ) {
        if (!dataMap.containsKey(key) && !aliaMap.containsKey(key)) {
            System.out.println("无效的key: “" + key + "”");
            return null;
        }
        KeyData data = dataMap.containsKey(key) ? dataMap.get(key) : dataMap.get(aliaMap.get(key));
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
                InputStream inputStream = gifMaker.makeAvatarGIF(key, avatarList, textList, antialias);
                return new Pair<>(inputStream, "gif");
            }

            if (data.getType() == Type.IMG) {
                InputStream inputStream = imageMaker.makeImage(key, avatarList, textList, antialias);
                return new Pair<>(inputStream, "png");
            }
        } catch (Exception ex) {
            System.out.println("解析 " + key + "/data.json 出错");
            ex.printStackTrace();
        }
        return null;
    }

    public HashMap<String, KeyData> getDataMap() {
        return dataMap;
    }

    public HashMap<String, String> getAliaMap() {
        return aliaMap;
    }
}
