package moe.dituon.petpet.plugin;

import moe.dituon.petpet.mirai.MiraiPetpet;
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
    public static final String REPO_DATA_PATH = "/data/xmmt.dituon.petpet/";
    private final String repositoryUrl;
    private final File targetDir;

    public DataUpdater(String repositoryUrl, File targetDir) {
        this.repositoryUrl = repositoryUrl;
        this.targetDir = targetDir;
    }

    public void autoUpdate() {
        if (!checkUpdate()) updateData();
    }

    public void updateData() {
        System.out.println("开始更新PetData");
        UpdateIndex index = UpdateIndex.parse(
                Objects.requireNonNull(getUrlText(repositoryUrl + "/index.json")));
        List<String> newPetList = index.getDataList();
        for (String pet : newPetList) {
            if (MiraiPetpet.service.getDataMap().containsKey(pet)
                    || MiraiPetpet.service.disabledKey.contains(pet)) continue;
            if (!saveAs(pet, "data.json")) {
                System.out.println("无法从远程仓库下载PetData: " + getRepositoryFileUrl(pet, "data.json"));
                break;
            }
            short i = 0;
            while (saveAs(pet, i + ".png")) i++;
            System.out.println("PetData/" + pet + "下载成功 (length:" + i + ')');
        }

        String fontsPath = REPO_DATA_PATH + BasePetService.FONTS_FOLDER;
        List<String> localFonts = new File(fontsPath).exists() ?
                Arrays.stream(Objects.requireNonNull(new File(fontsPath).listFiles()))
                        .map(File::getName).distinct().collect(Collectors.toList()) :
                Collections.emptyList();

        for (String font : index.getFontList()) {
            if (localFonts.contains(font)) continue;
            if (!saveAs(BasePetService.FONTS_FOLDER, font)) {
                System.out.println("无法从远程仓库下载PetFont: " + getRepositoryFileUrl(BasePetService.FONTS_FOLDER, font));
                return;
            }
            System.out.println("PetFont/" + font + "下载成功");
        }

        System.out.println("PetData更新完毕, 正在重新加载");
        MiraiPetpet.service.readData(MiraiPetpet.dataFolder);
    }

    public boolean checkUpdate() {
        UpdateIndex update = UpdateIndex.parse(
                Objects.requireNonNull(getUrlText(MiraiPetpet.service.repositoryUrl + "/index.json")));
        if (BasePetService.VERSION != update.getVersion())
            System.out.println("PetpetPlugin可更新到最新版本: " + update.getVersion() +
                    " (当前版本 " + BasePetService.VERSION + ")  要养成经常更新的好习惯哦 (*/ω＼*)");
        for (String pet : update.getDataList()) {
            if (MiraiPetpet.service.getDataMap().containsKey(pet)
                    || MiraiPetpet.service.disabledKey.contains(pet)) continue;
            System.out.println("发现新增PetData");
            return false;
        }
        return true;
    }

    private String getUrlText(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder str = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) str.append(line);
            reader.close();
            connection.disconnect();
            return str.toString();
        } catch (IOException ignored) {
            throw new RuntimeException("无法连接到远程资源: " + url);
        }
    }

    private boolean saveAs(String key, String fileName) {
        String url = getRepositoryFileUrl(key, fileName);
        Path target = Paths.get(targetDir.getAbsolutePath() + '/' + key, fileName);
        try (InputStream ins = new URL(url).openStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(ins, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignore) {
            return false;
        }
        return true;
    }

    private String getRepositoryFileUrl(String key, String fileName) {
        return repositoryUrl + REPO_DATA_PATH + key + '/' + fileName;
    }
}
