package moe.dituon.petpet.share;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BaseConfigFactory {
    private static final Random defaultRandom = new Random();

    @Deprecated
    public static AvatarExtraDataProvider getAvatarExtraDataFromUrls(
            String fromAvatarUrl,
            String toAvatarUrl,
            String groupAvatarUrl,
            String botAvatarUrl,
            List<String> randomAvatarList
    ) {
        try {
            return new AvatarExtraDataProvider(
                    fromAvatarUrl != null ? () -> ImageSynthesis.getWebImage(fromAvatarUrl) : null,
                    toAvatarUrl != null ? () -> ImageSynthesis.getWebImage(toAvatarUrl) : null,
                    groupAvatarUrl != null ? () -> ImageSynthesis.getWebImage(groupAvatarUrl) : null,
                    botAvatarUrl != null ? () -> ImageSynthesis.getWebImage(botAvatarUrl) : null,
                    randomAvatarList != null ? () -> {
                        var randomAvatar = new RandomAvatar(randomAvatarList);
                        return ImageSynthesis.getWebImage(randomAvatar.getRandom());
                    } : null
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static GifAvatarExtraDataProvider getGifAvatarExtraDataFromUrls(
            String fromAvatarUrl,
            String toAvatarUrl,
            String groupAvatarUrl,
            String botAvatarUrl,
            List<String> randomAvatarList
    ) {
        try {
            return new GifAvatarExtraDataProvider(
                    fromAvatarUrl != null ? () -> ImageSynthesis.getWebImageAsList(fromAvatarUrl) : null,
                    toAvatarUrl != null ? () -> ImageSynthesis.getWebImageAsList(toAvatarUrl) : null,
                    groupAvatarUrl != null ? () -> ImageSynthesis.getWebImageAsList(groupAvatarUrl) : null,
                    botAvatarUrl != null ? () -> ImageSynthesis.getWebImageAsList(botAvatarUrl) : null,
                    randomAvatarList != null ? () -> {
                        var randomAvatar = new RandomAvatar(randomAvatarList);
                        return ImageSynthesis.getWebImageAsList(randomAvatar.getRandom());
                    } : null
            );
        } catch (Exception e) {
            return null;
        }
    }

    static public class RandomAvatar {
        private final List<String> urlList;
        private Random random = defaultRandom;

        public RandomAvatar(List<String> urlList) {
            this.urlList = new ArrayList<>(urlList);
        }

        public void setSeed(long seed) {
            this.random = new Random(seed);
        }

        public String getRandom() {
            if (urlList.size() == 1) return urlList.get(0);
            return urlList.remove(random.nextInt(urlList.size()));
        }
    }
}
