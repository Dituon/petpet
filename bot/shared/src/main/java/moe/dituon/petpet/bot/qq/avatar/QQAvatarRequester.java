package moe.dituon.petpet.bot.qq.avatar;

import moe.dituon.petpet.core.imgres.WebImageResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class QQAvatarRequester {
    public static final int[] SPEC_LEVELS = new int[]{40, 100, 140, 640};

    public static String getAvatarUrlString(long qqId, int maxSize) {
        return getAvatarUrlString(String.valueOf(qqId), maxSize);
    }

    public static String getAvatarUrlString(String qqId, int maxSize) {
        int spec = SPEC_LEVELS[3];
        for (int level : SPEC_LEVELS) {
            if (level >= maxSize) {
                spec = level;
            }
        }
        return "https://q.qlogo.cn/headimg_dl?dst_uin=" + qqId + "&spec=" + spec;
    }

    public static URL getAvatarUrl(long qqId, int maxSize) {
        try {
            return new URL(getAvatarUrlString(qqId, maxSize));
        } catch (MalformedURLException ignored) {
            // never
            throw new IllegalStateException();
        }
    }

    public static WebImageResource getAvatarResource(long qqId, int maxSize) {
        URL avatarUrl = getAvatarUrl(qqId, maxSize);
        return new WebImageResource(avatarUrl, QQAvatarRequester::getQQAvatarStream);
    }
    
    public static WebImageResource getAvatarResource(String urlString) throws MalformedURLException {
        return new WebImageResource(new URL(urlString), QQAvatarRequester::getQQAvatarStream);
    }

    public static InputStream getQQAvatarStream(URL avatarUrl) {
        // 背景: 如果用户没有上传高清头像, 获取 640 分辨率头像会导致返回默认头像
        try {
            URLConnection conn = avatarUrl.openConnection();
            InputStream stream = conn.getInputStream();
            if (!avatarUrl.getHost().equals("q.qlogo.cn")) {
                return stream;
            }
            // 通过缓存字段来判断是否存在 640 分辨率的头像, 不存在则获取 100 分辨率头像
            if ("no-cache".equals(conn.getHeaderField("Cache-Control"))) {
                stream.close();
                conn.connect();
                String urlString = avatarUrl.toString();
                stream = new URL(
                        urlString.substring(0, urlString.length() - 3) + "100"
                ).openStream();
            }
            return stream;
        } catch (IOException ex) {
            throw new IllegalStateException("获取用户头像失败, URL: " + avatarUrl, ex);
        }
    }
}
