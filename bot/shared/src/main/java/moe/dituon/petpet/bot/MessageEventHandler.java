package moe.dituon.petpet.bot;

import java.util.Set;

public abstract class MessageEventHandler {
    public static final String FROM_KEY = "from";
    public static final String TO_KEY = "to";
    public static final String GROUP_KEY = "group";
    public static final String BOT_KEY = "bot";
    public static final String FROM_ID_KEY = "from_id";
    public static final String TO_ID_KEY = "to_id";
    public static final String GROUP_ID_KEY = "group_id";
    public static final String BOT_ID_KEY = "bot_id";
    public static final String RAW_TEXT_KEY = "raw";

    public static final String DEFAULT_IMAGE_NAME = "这个";

    public static final Set<String> ALL_TEXT_KEYS = Set.of(
            FROM_KEY, TO_KEY, GROUP_KEY, BOT_KEY,
            FROM_ID_KEY, TO_ID_KEY, GROUP_ID_KEY, BOT_ID_KEY
    );
    public static final Set<String> ALL_IMAGE_KEYS = Set.of(
            FROM_KEY, TO_KEY, GROUP_KEY, BOT_KEY
    );
}
