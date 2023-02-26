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
                    randomAvatarList != null && !randomAvatarList.isEmpty() ? () -> {
                        var randomAvatar = new RandomAvatar(randomAvatarList);
                        return ImageSynthesis.getWebImage(randomAvatar.getRandom());
                    } : null
            );
        } catch (Exception e) {
            throw new RuntimeException();
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
                    randomAvatarList != null && !randomAvatarList.isEmpty() ? () -> {
                        var randomAvatar = new RandomAvatar(randomAvatarList);
                        return ImageSynthesis.getWebImageAsList(randomAvatar.getRandom());
                    } : null
            );
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static GifAvatarExtraDataProvider toGifAvatarExtraDataProvider(AvatarExtraDataProvider extraData){
        return new GifAvatarExtraDataProvider(
                extraData.getFromAvatar() != null ? () -> List.of(extraData.getFromAvatar().invoke()) : null,
                extraData.getToAvatar() != null ? () -> List.of(extraData.getToAvatar().invoke()) : null,
                extraData.getGroupAvatar() != null ? () -> List.of(extraData.getGroupAvatar().invoke()) : null,
                extraData.getBotAvatar() != null ? () -> List.of(extraData.getBotAvatar().invoke()) : null,
                extraData.getRandomAvatar() != null ? () -> List.of(extraData.getRandomAvatar().invoke()) : null
        );
    }

    public static GifAvatarExtraDataProvider cacheAvatarExtraDataProvider(GifAvatarExtraDataProvider provider){
        var from = provider.getFromAvatar() == null ? null : provider.getFromAvatar().invoke();
        var to = provider.getToAvatar() == null ? null : provider.getToAvatar().invoke();
        var group = provider.getGroupAvatar() == null ? null : provider.getGroupAvatar().invoke();
        var bot = provider.getBotAvatar() == null ? null : provider.getBotAvatar().invoke();
        var random = provider.getRandomAvatar() == null ? null : provider.getRandomAvatar().invoke();
        return new GifAvatarExtraDataProvider(
                () -> from,
                () -> to,
                () -> group,
                () -> bot,
                () -> random
        );
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
            return urlList.remove(random.nextInt(urlList.size()));
        }
    }
}
