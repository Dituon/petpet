package moe.dituon.petpet.plugin;

import moe.dituon.petpet.share.BasePetService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class DataUpdater {
    public static final String DEFAULT_REPO_URL =
            "https://github.com/Dituon/petpet/raw/main/";
    public static final String DEFAULT_REPO_DATA_PATH = "./data/xmmt.dituon.petpet/";
    public static final String INDEX_FILE = "index.json";
    public static final String TEMPLATE_FILE = "data.json";

    protected final PluginPetService service;
    protected final Path targetPath;
    protected final Map<String, Integer> templateLengthMap = new HashMap<>(256);

    public DataUpdater(PluginPetService service, File targetDir) {
        this.service = service;
        this.targetPath = targetDir.toPath();
    }

    protected byte[] getUrlBytes(URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }

    protected String getUrlText(URL url) throws IOException {
        return new String(getUrlBytes(url), StandardCharsets.UTF_8);
    }

    protected void saveAs(URL url, Path path) throws IOException {
        try (InputStream in = url.openStream()){
            Files.createDirectories(path.getParent());
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public boolean autoUpdate() {
        var baseFontPath = targetPath.resolve(BasePetService.FONTS_FOLDER);
        for (String baseRepoUrl : service.repositoryUrls) {
            try {
                var url = new URI(baseRepoUrl + '/').normalize();
                var indexUrl = url.resolve(INDEX_FILE).toURL();
                var index = UpdateIndex.fromString(getUrlText(indexUrl));
                var baseDataUrl = url.resolve(index.getDataPath() + '/');

                // check plugin
                if (BasePetService.VERSION != index.getVersion()) {
                    PluginPetService.LOGGER.info(
                            "PetpetPlugin 可更新到最新版本: " + index.getVersion() +
                                    " (当前版本 " + BasePetService.VERSION + ")  养成更新的好习惯哦 (*/ω＼*)"
                    );
                }

                // update templates
                for (String templateKey : index.getDataList()) {
                    if (service.getDataMap().containsKey(templateKey)
                            || service.disabledKey.contains(templateKey)
                            || this.templateLengthMap.containsKey(templateKey)) {
                        continue;
                    }
                    var templateKeyPath = templateKey + '/';

                    // save data.json
                    saveAs(
                            baseDataUrl.resolve(templateKeyPath).resolve(TEMPLATE_FILE).toURL(),
                            targetPath.resolve(templateKeyPath).resolve(TEMPLATE_FILE)
                    );
                    int count = 0;
                    while (true) {
                        // save backgrounds
                        var imageName = count + ".png";
                        var imageUri = baseDataUrl.resolve(templateKeyPath).resolve(imageName);
                        var imagePath = targetPath.resolve(templateKeyPath).resolve(imageName);
                        try {
                            saveAs(imageUri.toURL(), imagePath);
                            count++;
                        } catch (MalformedInputException | FileNotFoundException ex) {
                            break;
                        }
                    }
                    BasePetService.LOGGER.info("Petpet 模板 " + templateKey + " 下载成功 (length:" + count + ')');
                    this.templateLengthMap.put(templateKey, count);
                }

                // update fonts
                var baseFontUri = baseDataUrl.resolve(BasePetService.FONTS_FOLDER + '/');
                for (String fontName : index.getFontList()) {
                    var fontPath = baseFontPath.resolve(fontName);
                    if (Files.exists(fontPath)) {
                        continue;
                    }
                    saveAs(baseFontUri.resolve(fontName).toURL(), fontPath);
                    BasePetService.LOGGER.info("Petpet 字体 " + fontName + " 下载成功");
                }
            } catch (URISyntaxException | MalformedURLException ex) {
                BasePetService.LOGGER.warning("仓库地址解析失败: " + baseRepoUrl, ex);
            } catch (IOException ex) {
                BasePetService.LOGGER.warning("无法连接到远程仓库: " + baseRepoUrl, ex);
            }
        }
        return !this.templateLengthMap.isEmpty();
    }
}
