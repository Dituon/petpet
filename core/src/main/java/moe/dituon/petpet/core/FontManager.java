package moe.dituon.petpet.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

@Slf4j
public class FontManager {
    protected static class FontManagerInstance {
        private static final FontManager INSTANCE = new FontManager();
    }

    public static FontManager getInstance() {
        return FontManagerInstance.INSTANCE;
    }
    public static final String DEFAULT_WINDOWS_FONT = "Microsoft YaHei";
    public static final String DEFAULT_MACOS_FONT = "PingFang SC";
    public static final String DEFAULT_LINUX_FONT = "Noto Sans CJK SC";
    public static final String DEFAULT_FONT = "MiSans";
    public static final String[] preferredFonts = {DEFAULT_WINDOWS_FONT, DEFAULT_MACOS_FONT, DEFAULT_LINUX_FONT};

    public static final SupportedLanguage LANG_EN = new SupportedLanguage("en", "Latin", new char[]{0x0061});
    public static final SupportedLanguage LANG_ZH = new SupportedLanguage("zh", "CJK Unified Ideographs", new char[]{0x4E00});
    public static final SupportedLanguage LANG_JA = new SupportedLanguage("ja", "Hiragana", new char[]{0x3041});
    public static final SupportedLanguage LANG_KO = new SupportedLanguage("ko", "Hangul Syllables", new char[]{0xAC00});
    public static final SupportedLanguage LANG_TH = new SupportedLanguage("th", "Thai", new char[]{0x0E01});
    public static final SupportedLanguage LANG_RU = new SupportedLanguage("ru", "Cyrillic", new char[]{0x0410});
    public static final SupportedLanguage LANG_AR = new SupportedLanguage("ar", "Arabic", new char[]{0x0621});
    public static final SupportedLanguage LANG_HE = new SupportedLanguage("he", "Hebrew", new char[]{0x05D0});
    public static final SupportedLanguage LANG_VI = new SupportedLanguage("vi", "Vietnamese", new char[]{0x0102});
//    public static final SupportedLanguage LANG_EMOJI = new SupportedLanguage("emoji", "Emoji", Character.toChars(0x1F600));

    protected final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    public final SupportedLanguage[] supportedLanguages = new SupportedLanguage[]{
            LANG_EN, LANG_ZH, LANG_JA, LANG_KO, LANG_TH, LANG_RU, LANG_AR, LANG_HE, LANG_VI
//            , LANG_EMOJI
    };

    @Getter
    protected final Map<Font, Set<SupportedLanguage>> fontSupportedLanguageMap = new HashMap<>(256);
    @Getter
    protected final Map<SupportedLanguage, Set<Font>> supportedLanguageFontMap;
    @Getter
    protected String defaultFontFamily = DEFAULT_FONT;

    protected FontManager() {
        supportedLanguageFontMap = new HashMap<>(supportedLanguages.length);
        for (SupportedLanguage language : supportedLanguages) {
            supportedLanguageFontMap.put(language, new HashSet<>(16));
        }

        for (Font font : environment.getAllFonts()) {
            addFont(font);
        }

        setDefaultFontFamily(null);
    }

    public String setDefaultFontFamily(@Nullable String defaultFamily) {
        defaultFontFamily = (defaultFamily == null) ? DEFAULT_FONT : defaultFamily;

        var allFamilies = new HashSet<>(List.of(environment.getAvailableFontFamilyNames(Locale.ENGLISH)));
        if (allFamilies.contains(defaultFontFamily)) {
            return defaultFontFamily;
        }

        for (String preferredFont : preferredFonts) {
            if (allFamilies.contains(preferredFont)) {
                defaultFontFamily = preferredFont;
                return preferredFont;
            }
        }

        Set<Font> supportedFonts = supportedLanguageFontMap.getOrDefault(LANG_ZH, Collections.emptySet());
        if (!supportedFonts.isEmpty()) {
            defaultFontFamily = supportedFonts.iterator().next().getFamily();
        }

        return defaultFontFamily;
    }


    public Font getFont(
            @Nullable String family,
            @MagicConstant(intValues = {Font.PLAIN, Font.BOLD, Font.ITALIC}) int style,
            int size
    ) {
        return StyleContext.getDefaultStyleContext().getFont(family == null ? defaultFontFamily : family, style, size);
    }

    public void addFont(Font font) {
        var langSet = fontSupportedLanguageMap.computeIfAbsent(font, f -> new HashSet<>(supportedLanguages.length));
        for (SupportedLanguage language : supportedLanguages) {
            boolean allSupported = true;
            for (char point : language.testPoints) {
                if (!font.canDisplay(point)) {
                    allSupported = false;
                    break;
                }
            }
            if (allSupported) {
                langSet.add(language);
                supportedLanguageFontMap.get(language).add(font);
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
                log.error("Can not load font file: {}", fontFile.getAbsolutePath(), e);
            }
        }
        if (!successNames.isEmpty()) {
            log.info("Font register success: {}", String.join(", ", successNames));
        }
        if (!failedNames.isEmpty()) {
            log.info("Font has been registered: {}", String.join(", ", failedNames));
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
