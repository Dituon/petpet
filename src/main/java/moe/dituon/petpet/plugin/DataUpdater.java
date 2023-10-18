package moe.dituon.petpet.plugin;

import moe.dituon.petpet.share.BasePetService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class DataUpdater {
    public static final String DEFAULT_REPO_URL = "https://raw.githubusercontent.com/Dituon/petpet/main";
    public static final String DEFAULT_REPO_DATA_PATH = "/data/xmmt.dituon.petpet/";
    public static final String INDEX_FILE = "index.json";
    public static final String TEMPLATE_FILE = "data.json";
    private final String[] repositoryUrls;
    private final File targetDir;
    private final PluginPetService service;

    public DataUpdater(PluginPetService service, File targetDir) {
        this.repositoryUrls = service.repositoryUrls;
        this.targetDir = targetDir;
        this.service = service;
    }

    public boolean autoUpdate() {
        List<UpdateIndex> indices = new ArrayList<>(repositoryUrls.length);
        for (String repositoryUrl : repositoryUrls) {
            String url = joinPath(repositoryUrl, INDEX_FILE);
            try {
                UpdateIndex index = UpdateIndex.parse(getUrlText(url));
                index.setUrl(repositoryUrl);
                indices.add(index);
            } catch (IOException ex) {
                PluginPetService.LOGGER.warning("无法连接到远程仓库: " + repositoryUrl, ex);
            } catch (Exception ex) {
                PluginPetService.LOGGER.warning("无法解析索引文件: " + url, ex);
            }
        }

        if (!checkUpdate(indices)) {
            PluginPetService.LOGGER.info("开始更新PetData");
            updateData(indices);
            return true;
        }
        return false;
    }

    public void updateData(Iterable<UpdateIndex> indices) {
        Map<String, String> templateMap = new HashMap<>(256);
        Map<String, String> fontMap = new HashMap<>(8);
        for (UpdateIndex index : indices) {
            String baseUrl = index.getUrl() + '/' + index.getDataPath() + '/';

            index.getDataList().forEach(key -> templateMap.put(key, baseUrl + key));
            index.getFontList().forEach(font -> fontMap.put(font, baseUrl + BasePetService.FONTS_FOLDER));
        }

        for (var entry : templateMap.entrySet()) {
            String key = entry.getKey();
            if (isExcludedKey(key)) {
                continue;
            }

            String url = entry.getValue();
            if (!saveAs(url, TEMPLATE_FILE)) {
                PluginPetService.LOGGER.warning("无法从远程仓库下载 PetTemplate: " + url);
                continue;
            }
            short i = 0;
            while (saveAs(url, i + ".png")) i++;
            PluginPetService.LOGGER.info("PetTemplate/" + key + "下载成功 (length:" + i + ')');
        }

        File localFontsDir = new File(DEFAULT_REPO_DATA_PATH + BasePetService.FONTS_FOLDER);
        Set<String> localFonts = localFontsDir.exists() && localFontsDir.isDirectory() && localFontsDir.canRead() ?
                Arrays.stream(Objects.requireNonNull(localFontsDir.listFiles())).map(File::getName).collect(Collectors.toSet()) :
                Collections.emptySet();

        for (var entry : fontMap.entrySet()) {
            String font = entry.getKey(), url = entry.getValue();
            if (localFonts.contains(font)) continue;

            if (!saveAs(url, font)) {
                PluginPetService.LOGGER.warning("无法从远程仓库下载PetFont: " + url);
                return;
            }

            PluginPetService.LOGGER.info("PetFont/" + font + "下载成功");
        }


        for (String font : fontMap.keySet()) {
            if (localFonts.contains(font)) continue;
            if (!saveAs(BasePetService.FONTS_FOLDER, font)) {
                PluginPetService.LOGGER.warning("无法从远程仓库下载PetFont: " + joinPath(BasePetService.FONTS_FOLDER, font));
                return;
            }
            PluginPetService.LOGGER.info("PetFont/" + font + "下载成功");
        }
    }

    public boolean checkUpdate(Iterable<UpdateIndex> indices) {
        for (UpdateIndex index : indices) {
            if (BasePetService.VERSION != index.getVersion()) {
                PluginPetService.LOGGER.info(
                        "PetpetPlugin 可更新到最新版本: " + index.getVersion() +
                                " (当前版本 " + BasePetService.VERSION + ")  养成更新的好习惯哦 (*/ω＼*)"
                );
            }

            for (String key : index.getDataList()) {
                if (isExcludedKey(key)) continue;
                PluginPetService.LOGGER.info("发现新增 Petpet 模板");
                return false;
            }
        }
        return true;
    }

    protected String getUrlText(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder str = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) str.append(line);
        reader.close();
        connection.disconnect();
        return str.toString();
    }

    protected boolean saveAs(String baseUrl, String filePath) {
        String url = joinPath(baseUrl, filePath);
        Path target = Paths.get(targetDir.getAbsolutePath(), filePath);
        try (InputStream ins = new URL(url).openStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(ins, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignore) {
            return false;
        }
        return true;
    }

    protected boolean isExcludedKey(String key) {
        return service.getDataMap().containsKey(key) || service.disabledKey.contains(key);
    }

    protected String joinPath(String basePath, String fileName) {
        return basePath + '/' + fileName;
    }
}
