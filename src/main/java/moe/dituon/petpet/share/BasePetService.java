package moe.dituon.petpet.share;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class BasePetService {
    public static final float VERSION = 6.1F;
    public static final int DEFAULT_INITIAL_CAPACITY = 256;
    public static final BaseLogger LOGGER = BaseLogger.getInstance();
    public static final String FONTS_FOLDER = "fonts";
    public static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;
    protected boolean antialias = true;
    protected boolean resampling = true;
    protected int quality = 10;
    private List<Integer> gifMaxSize = null;
    public Encoder encoder = Encoder.ANIMATED_LIB;

    public File dataRoot = new File("./data");
    protected HashMap<String, TemplateDTO> dataMap = new HashMap<>(DEFAULT_INITIAL_CAPACITY);
    protected HashMap<String, String[]> aliaMap = new HashMap<>(DEFAULT_INITIAL_CAPACITY);
    protected HashMap<String, Callable<BufferedImage[]>> backgroundLambdaMap = new HashMap<>(DEFAULT_INITIAL_CAPACITY);
    protected WeakHashMap<String, BufferedImage[]> backgroundCacheMap = new WeakHashMap<>(DEFAULT_INITIAL_CAPACITY);
    public String keyListString = "";

    //    protected int serviceThreadPoolSize = DEFAULT_THREAD_POOL_SIZE;
//    protected ExecutorService serviceThreadPool = Executors.newFixedThreadPool(serviceThreadPoolSize);
    protected int gifEncoderThreadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    protected BaseGifMaker gifMaker = new BaseGifMaker();
    protected BaseImageMaker imageMaker = new BaseImageMaker(gifMaker);
    public static final Random random = new Random();

    /**
     * 从文件中读取KeyData模板到dataMap中
     *
     * @param files KeyData目录
     */
    public void readData(File[] files) {
        if (files == null || files.length == 0) {
            LOGGER.warning("无法读取文件, 请检查data目录 (files.length = 0)");
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
                TemplateDTO data = TemplateDTO.getData(getFileStr(dataFile));
                putKeyData(file.getName(), data);
            } catch (Exception ex) {
                LOGGER.warning("无法读取 " + file + "/data.json ", ex);
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
    public void putKeyData(String key, TemplateDTO data) {
        data.getAvatar().forEach(avatar -> {
            if (avatar.getResampling() == null) avatar.setResampling(resampling);
        });
        dataMap.put(key.intern(), data);

        String path = dataRoot.getAbsolutePath() + File.separator + key + File.separator;
        File[] files = new File(path).listFiles();
        if (files == null) throw new RuntimeException("无法读取 " + path + " 目录");

        File[] backgroundFiles = Arrays.stream(files)
                .filter(f -> f.getName().endsWith(".png"))
                .sorted(Comparator.comparingInt(f -> {
                    String fileName = f.getName();
                    int startIndex = fileName.lastIndexOf("/") + 1;
                    int endIndex = fileName.lastIndexOf(".");
                    String number = fileName.substring(startIndex, endIndex);
                    return Integer.parseInt(number);
                }))
                .toArray(File[]::new);

        assert dataRoot != null;
        backgroundLambdaMap.put(key, () -> { //使用Lambda实现按需加载, 减少内存占用
            if (backgroundCacheMap.containsKey(key)) {
                return backgroundCacheMap.get(key);
            }
            BufferedImage[] result = Arrays.stream(backgroundFiles).map(bg -> {
                try {
                    return ImageIO.read(bg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(BufferedImage[]::new);
            backgroundCacheMap.put(key, result);
            return result;
        });

        if (!Boolean.TRUE.equals(data.getHidden())) putAlia(key, data);
    }

    /**
     * 将KeyData添加到dataMap中, 指定背景, 并更新 aliaMap&keyListString
     *
     * @param key 索引
     */
    public void putKeyData(String key, TemplateDTO data, BufferedImage background) {
        putKeyData(key, data, List.of(background));
    }

    /**
     * 将KeyData添加到dataMap中, 指定背景表列, 并更新 aliaMap&keyListString
     *
     * @param key 索引
     */
    public void putKeyData(String key, TemplateDTO data, List<BufferedImage> backgroundList) {
        data.getAvatar().forEach(avatar -> {
            if (avatar.getResampling() == null) avatar.setResampling(resampling);
        });

        dataMap.put(key, data);
        backgroundLambdaMap.put(key, () -> backgroundList.toArray(BufferedImage[]::new));

        if (!Boolean.TRUE.equals(data.getHidden())) putAlia(key, data);
    }

    private void putAlia(String key, TemplateDTO data) {
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
        if (!fontsFolder.exists() || !fontsFolder.isDirectory()) return;

        List<String> successNames = new ArrayList<>();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (File fontFile : Objects.requireNonNull(fontsFolder.listFiles())) {
            try {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                boolean success = ge.registerFont(customFont);
                if (success) {
                    successNames.add(fontFile.getName());
                } else {
                    LOGGER.info("注册字体失败: " + fontFile.getName());
                }
            } catch (Exception e) {
                LOGGER.warning("注册字体异常: " + e);
            }
        }
        LOGGER.info("注册字体成功: " + successNames);
    }

    public void readBaseServiceConfig(BaseServiceConfig config) {
        antialias = config.getAntialias();
        resampling = config.getResampling();
        setGifMaxSize(config.getGifMaxSize());
        encoder = config.getGifEncoder();
        quality = config.getGifQuality();
        setThreadPoolSize(config.getThreadPoolSize());
//        setServiceThreadPoolSize(config.getServiceThreadPoolSize());
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
        try {
            TemplateDTO data = dataMap.containsKey(key) ? dataMap.get(key) : dataMap.get(aliaMap.get(key)[0]);
            BufferedImage[] backgrounds = backgroundLambdaMap.get(key).call();
            return generateImage(
                    data, backgrounds, gifAvatarExtraDataProvider, textExtraData, additionTextDataList
            );
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("无法读取 " + key + " 背景文件", ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Pair<InputStream, String> generateImage(
            TemplateDTO data,
            BufferedImage[] backgrounds,
            GifAvatarExtraDataProvider gifAvatarExtraDataProvider,
            TextExtraData textExtraData,
            List<TextData> additionTextDataList
    ) throws FileNotFoundException {
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

        ArrayList<AvatarModel> avatarList = new ArrayList<>(data.getAvatar().size());

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

        switch (data.getType()) {
            case GIF:
                BufferedImage[] stickers;

                if (data.getBackground() != null) { //从配置文件读背景
                    stickers = new BackgroundModel(data.getBackground(), avatarList, textList).getImages();
                } else {
                    stickers = backgrounds;
                }

                InputStream inputStream = gifMaker.makeGIF(
                        avatarList, textList, stickers, renderParams
                );
                return new Pair<>(inputStream, "gif");
            case IMG:
                BufferedImage sticker = getBackgroundImage(backgrounds, data, avatarList, textList);
                assert sticker != null;
                return imageMaker.makeImage(avatarList, textList, sticker, renderParams);
        }

        throw new RuntimeException(); //never
    }

    private BufferedImage getBackgroundImage(
            BufferedImage[] backgrounds,
            TemplateDTO data,
            ArrayList<AvatarModel> avatarList,
            ArrayList<TextModel> textList
    ) throws FileNotFoundException {
        boolean isEmpty = backgrounds.length == 0;
        if (isEmpty && data.getBackground() == null) { //没有背景图片和背景配置
            throw new FileNotFoundException();
        }
        if (isEmpty && data.getBackground() != null) { //无背景图片(读取背景配置
            return new BackgroundModel(data.getBackground(), avatarList, textList).getImage();
        }
        assert !isEmpty;
        var background = backgrounds[random.nextInt(backgrounds.length)];

        if (data.getBackground() == null) return background;  //无背景配置(读取随机背景图片

        //有配置项和图片
        return new BackgroundModel(
                data.getBackground(), avatarList, textList, background
        ).getImage();
    }

    public HashMap<String, TemplateDTO> getDataMap() {
        return dataMap;
    }

    public HashMap<String, String[]> getAliaMap() {
        return aliaMap;
    }

    public void setGifMaxSize(List<Integer> maxSize) {
        if (maxSize == null || maxSize.isEmpty()) return;
        if (maxSize.size() > 3) {
            LOGGER.warning("GifMaxSize无效: Length Must <= 3");
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

    public void setThreadPoolSize(int size) {
        assert size >= 0;
        gifEncoderThreadPoolSize = size == 0 ? DEFAULT_THREAD_POOL_SIZE : size;
        ImageSynthesis.threadPool.shutdown();
        ImageSynthesis.threadPool = Executors.newFixedThreadPool(gifEncoderThreadPoolSize);
    }

//    public void setServiceThreadPoolSize(int size) {
//        assert size >= 0;
//        serviceThreadPoolSize = size == 0 ? DEFAULT_THREAD_POOL_SIZE : size;
//        serviceThreadPool = Executors.newFixedThreadPool(serviceThreadPoolSize);
//    }
//
//    public int getServiceThreadPoolSize(){
//        return serviceThreadPoolSize;
//    }
//
//    public ExecutorService getServiceThreadPool(){
//        return serviceThreadPool;
//    }

    public int getGifEncoderThreadPoolSize() {
        return gifEncoderThreadPoolSize;
    }

    public BaseGifMaker getGifMaker() {
        return gifMaker;
    }

    public BaseImageMaker getImageMaker() {
        return imageMaker;
    }
}
