package moe.dituon.petpet.share.service;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FontManager {
    protected static class FontManagerInstance {
        private static final FontManager INSTANCE = new FontManager();
    }

    public static FontManager getInstance() {
        return FontManagerInstance.INSTANCE;
    }

    final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    public final List<SupportedLanguage> supportedLanguageList = List.of(
            new SupportedLanguage("en", "Latin", new char[]{0x0061}),
            new SupportedLanguage("zh", "CJK Unified Ideographs", new char[]{0x4E00}),
            new SupportedLanguage("ja", "Hiragana", new char[]{0x3041}),
            new SupportedLanguage("ko", "Hangul Syllables", new char[]{0xAC00}),
            new SupportedLanguage("th", "Thai", new char[]{0x0E01}),
            new SupportedLanguage("ru", "Cyrillic", new char[]{0x0410}),
            new SupportedLanguage("ar", "Arabic", new char[]{0x0621}),
            new SupportedLanguage("he", "Hebrew", new char[]{0x05D0}),
            new SupportedLanguage("vi", "Vietnamese", new char[]{0x0102})
//            , new SupportedLanguage("emoji", "Emoji", Character.toChars(0x1F600))
    );
    final Map<Font, Set<SupportedLanguage>> fontSupportedLanguageMap = new HashMap<>(256);
    final Map<SupportedLanguage, Set<Font>> supportedLanguageFontMap = new HashMap<>(256);

    protected FontManager() {
        for (Font font : environment.getAllFonts()) {
            addFont(font);
        }
    }

    public void addFont(Font font) {
        var langSet = fontSupportedLanguageMap.compute(font, (f, s) ->
                s == null ? new HashSet<>(supportedLanguageList.size()) : s
        );
        for (SupportedLanguage language : supportedLanguageList) {
            boolean flag = true;
            for (char point : language.testPoints) {
                if (!font.canDisplay(point)) flag = false;
            }
            if (flag) {
                langSet.add(language);
                supportedLanguageFontMap.compute(language, (l, s) ->
                        s == null ? new HashSet<>(8) : s
                ).add(font);
            }
        }
    }

    public static class SupportedLanguage {
        protected char[] testPoints;
        public final String name;
        public final String desc;

        protected SupportedLanguage(String name, String desc, char[] testPoints) {
            this.name = name;
            this.desc = desc;
            this.testPoints = testPoints;
        }
    }
}
