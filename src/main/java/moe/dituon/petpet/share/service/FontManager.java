package moe.dituon.petpet.share.service;

import moe.dituon.petpet.share.BaseLogger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

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

        supportedLanguageFontMap.forEach((k, v) -> {
            if (v.isEmpty()) {
                BaseLogger.getInstance().warning("Can not find font that support " + k.name + " (" + k.desc + ")");
            }
        });
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

    public boolean addFont(File fontFile) throws IOException, FontFormatException {
        Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
        boolean success = environment.registerFont(customFont);
        if (success) {
            addFont(customFont);
        }
        return success;
    }

    public void addFonts(Path fontDirectory) {
        if (!Files.exists(fontDirectory) || !Files.isDirectory(fontDirectory)) return;

        var files = fontDirectory.toFile().listFiles();
        if (files == null) return;
        List<String> successNames = new ArrayList<>(files.length);
        List<String> failedNames = new ArrayList<>(files.length);
        for (File fontFile : files) {
            try {
                if (this.addFont(fontFile)) {
                    successNames.add(fontFile.getName());
                } else {
                    failedNames.add(fontFile.getName());
                }
            } catch (IOException | FontFormatException e) {
                BaseLogger.getInstance().error("无法读取字体文件: " + fontFile.getAbsolutePath(), e);
            }
        }
        if (successNames.isEmpty()) {
            BaseLogger.getInstance().info("成功注册字体文件: " + String.join(", ", successNames));
        }
        if (!failedNames.isEmpty()) {
            BaseLogger.getInstance().warning("字体文件已被注册: " + String.join(", ", failedNames));
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
