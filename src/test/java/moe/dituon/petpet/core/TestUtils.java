package moe.dituon.petpet.core;

import moe.dituon.petpet.share.template.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestUtils {
    public static ExtraData getExtraData(TemplateBuilder builder) {
        return new ExtraData(getAvatarExtraData(builder), getTextExtraData());
    }

    public static AvatarExtraData getAvatarExtraData(TemplateBuilder builder) {
        return new AvatarExtraDataUrls(
                Map.of(
                        "from", "https://avatars.githubusercontent.com/u/68615161?v=4&size=64",
                        "to", "https://avatars.githubusercontent.com/u/68615161?v=4&size=64",
                        "group", "https://avatars.githubusercontent.com/u/68615161?v=4&size=64",
                        "bot", "https://avatars.githubusercontent.com/u/68615161?v=4&size=64"
                ),
                Collections.emptyList()
        ).toAvatarExtraData(builder.getBackgroundResource().getBasePath());
    }

    public static TextExtraData getTextExtraData() {
        return new TextExtraData(
                Map.of(
                        "from", "from",
                        "to", "to",
                        "group", "group",
                        "bot", "bot"
                ),
                List.of("text1", "text2")
        );
    }
}
