package xmmt.dituon.share;

public class BaseConfigFactory {

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
}
