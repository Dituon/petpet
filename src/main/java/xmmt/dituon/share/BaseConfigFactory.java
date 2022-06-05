package xmmt.dituon.share;

public class BaseConfigFactory {

    public static AvatarExtraData getAvatarExtraDataFromUrls(
            String fromAvatarUrl,
            String toAvatarUrl,
            String groupAvatarUrl,
            String botAvatarUrl)  {
        try {
            return new AvatarExtraData(
                    fromAvatarUrl != null ? ImageSynthesis.getAvatarImage(fromAvatarUrl) : null,
                    toAvatarUrl != null ? ImageSynthesis.getAvatarImage(toAvatarUrl) : null,
                    groupAvatarUrl != null ? ImageSynthesis.getAvatarImage(groupAvatarUrl) : null,
                    botAvatarUrl != null ? ImageSynthesis.getAvatarImage(botAvatarUrl) : null
            );
        } catch (Exception e) {
            return null;
        }
    }
}
