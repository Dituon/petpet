package moe.dituon.petpet.plugin;

import moe.dituon.petpet.share.BaseConfigFactory;
import moe.dituon.petpet.share.GifAvatarExtraDataProvider;
import moe.dituon.petpet.share.ImageSynthesis;
import moe.dituon.petpet.share.ImageSynthesisCore;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class QQConfigFactory extends BaseConfigFactory {
    public static GifAvatarExtraDataProvider getGifAvatarExtraDataFromUrls(
            String fromAvatarUrl,
            String toAvatarUrl,
            String groupAvatarUrl,
            String botAvatarUrl,
            List<String> randomAvatarList
    ) {
        try {
            return new GifAvatarExtraDataProvider(
                    fromAvatarUrl != null ? () -> QQImageSynthesis.getWebImageAsList(fromAvatarUrl) : null,
                    toAvatarUrl != null ? () -> QQImageSynthesis.getWebImageAsList(toAvatarUrl) : null,
                    groupAvatarUrl != null ? () -> ImageSynthesisCore.getWebImageAsList(groupAvatarUrl) : null,
                    botAvatarUrl != null ? () -> QQImageSynthesis.getWebImageAsList(botAvatarUrl) : null,
                    randomAvatarList != null && !randomAvatarList.isEmpty() ? () -> {
                        var randomAvatar = new RandomAvatar(randomAvatarList);
                        return QQImageSynthesis.getWebImageAsList(randomAvatar.getRandom());
                    } : null
            );
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private static class QQImageSynthesis extends ImageSynthesis {
        // 背景: 如果用户没有上传高清头像, 获取 640 分辨率头像会导致返回默认头像
        public static List<BufferedImage> getWebImageAsList(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                URLConnection conn = url.openConnection();
                InputStream originStream = conn.getInputStream();
                // 通过缓存字段来判断是否存在 640 分辨率的头像, 不存在则获取 100 分辨率头像
                if ("no-cache".equals(conn.getHeaderField("Cache-Control"))) {
                    originStream.close();
                    conn.connect();
                    originStream = new URL(
                            imageUrl.substring(0, imageUrl.length() - 3) + "100"
                    ).openStream();
                }
                return getImageAsList(new BufferedInputStream(originStream));
            } catch (Exception ex) {
                throw new RuntimeException("[获取/解析 图像失败]  URL: " + imageUrl, ex);
            }
        }
    }
}
