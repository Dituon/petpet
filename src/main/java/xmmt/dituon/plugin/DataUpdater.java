package xmmt.dituon.plugin;

import xmmt.dituon.share.BasePetService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DataUpdater {
    public static void autoUpdate() {
        if (!checkUpdate()) updateData();
    }

    public static void updateData() {
        System.out.println("开始更新PetData");
        UpdateIndex index = UpdateIndex.getUpdate(
                Objects.requireNonNull(getUrlText(Petpet.service.repositoryUrl + "/index.json")));
        List<String> newPetList = index.getDataList();
        for (String pet : newPetList) {
            if (Petpet.service.getDataMap().containsKey(pet)
                    || Petpet.service.updateIgnore.contains(pet)) continue;
            String petDataPath = "/data/xmmt.dituon.petpet/" + pet;
            if (!saveAs(petDataPath, "data.json")) {
                System.out.println("无法从远程仓库下载PetData: " + petDataPath);
                break;
            }
            short i = 0;
            while (saveAs(petDataPath, i + ".png")) i++;
            System.out.println("PetData/" + pet + "下载成功 (length:" + i + ')');
        }

        String fontsPath = "/data/xmmt.dituon.petpet/" + BasePetService.FONTS_FOLDER;
        List<String> localFonts = new ArrayList<>();
        if (new File(fontsPath).exists())
            localFonts = Arrays.stream(Objects.requireNonNull(new File(fontsPath).listFiles()))
                    .map(File::getName).distinct().collect(Collectors.toList());

        for (String font : index.getFontList()) {
            if (localFonts.contains(font)) continue;
            if (!saveAs(fontsPath, font)) {
                System.out.println("无法从远程仓库下载PetFont: " + fontsPath);
                return;
            }
            System.out.println("PetFont/" + font + "下载成功");
        }

        System.out.println("PetData更新完毕, 正在重新加载");
        Petpet.service.readData(Petpet.dataFolder);
    }

    public static boolean checkUpdate() {
        UpdateIndex update = UpdateIndex.getUpdate(
                Objects.requireNonNull(getUrlText(Petpet.service.repositoryUrl + "/index.json")));
        if (Petpet.VERSION != update.getVersion())
            System.out.println("PetpetPlugin可更新到最新版本: " + update.getVersion() + " (当前版本 " + Petpet.VERSION + ")");
        for (String pet : update.getDataList()) {
            if (Petpet.service.getDataMap().containsKey(pet)) continue;
            System.out.println("发现新增PetData");
            return false;
        }
        return true;
    }

    private static String getUrlText(String url) {
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
            System.out.println("无法连接到远程资源: " + url);
            return null;
        }
    }

    private static boolean saveAs(String path, String fileName) {
        try (InputStream ins = new URL(Petpet.service.repositoryUrl + path + '/' + fileName).openStream()) {
            Path target = Paths.get(new File(".").getCanonicalPath() + path, fileName);
            Files.createDirectories(target.getParent());
            Files.copy(ins, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            return false;
        }
        return true;
    }
}
