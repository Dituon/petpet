package xmmt.dituon.share;

public class BaseConfigFactory {

    @Deprecated
    public static AvatarExtraDataProvider getAvatarExtraDataFromUrls(
            String fromAvatarUrl,
            String toAvatarUrl,
            String groupAvatarUrl,
            String botAvatarUrl) {
        try {
            return new AvatarExtraDataProvider(
                    fromAvatarUrl != null ? () -> ImageSynthesis.getWebImage(fromAvatarUrl) : null,
                    toAvatarUrl != null ? () -> ImageSynthesis.getWebImage(toAvatarUrl) : null,
                    groupAvatarUrl != null ? () -> ImageSynthesis.getWebImage(groupAvatarUrl) : null,
                    botAvatarUrl != null ? () -> ImageSynthesis.getWebImage(botAvatarUrl) : null
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static GifAvatarExtraDataProvider getGifAvatarExtraDataFromUrls(
            String fromAvatarUrl,
            String toAvatarUrl,
            String groupAvatarUrl,
            String botAvatarUrl) {
        try {
            return new GifAvatarExtraDataProvider(
                    fromAvatarUrl != null ? () -> ImageSynthesis.getWebImageAsList(fromAvatarUrl) : null,
                    toAvatarUrl != null ? () -> ImageSynthesis.getWebImageAsList(toAvatarUrl) : null,
                    groupAvatarUrl != null ? () -> ImageSynthesis.getWebImageAsList(groupAvatarUrl) : null,
                    botAvatarUrl != null ? () -> ImageSynthesis.getWebImageAsList(botAvatarUrl) : null
            );
        } catch (Exception e) {
            return null;
        }
    }
}
