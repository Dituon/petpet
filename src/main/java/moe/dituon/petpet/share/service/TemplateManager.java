package moe.dituon.petpet.share.service;

import lombok.Getter;
import moe.dituon.petpet.share.Type;
import moe.dituon.petpet.share.template.PetpetTemplate;
import moe.dituon.petpet.share.template.TemplateBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;

public class TemplateManager {
    public static final String TEMPLATE_FILE_NAME = "data.json";
    protected final ResourceManager resourceManager;
    @Getter
    protected final HashMap<String, TemplateBuilder> templateMap = new HashMap<>(256);

    public TemplateManager() {
        this(ResourceManager.getDefaultInstance());
    }

    public TemplateManager(ResourceManager manager) {
        this.resourceManager = manager;
    }

    public void pushBasePath(File basePath) throws IOException {
        if (!basePath.isDirectory()) return;
        for (File file : Objects.requireNonNull(basePath.listFiles())) {
            if (!file.isDirectory()) continue;
            var templateRaw = file.toPath().resolve(TEMPLATE_FILE_NAME);
            if (!Files.isReadable(templateRaw)) continue;
            var id = file.getName();
            var templateConfig = PetpetTemplate.fromString(Files.readString(templateRaw));

            var background = new BackgroundResource(file);
            resourceManager.pushBackground(id, background);
            if (templateConfig.getType() == Type.IMG && background.files.length > 1) {
                background.randomFlag = true;
            }
            var templateBuilder = new TemplateBuilder(templateConfig, background);
            templateMap.put(id, templateBuilder);
        }
    }

    public TemplateBuilder getTemplate(String name) {
        return templateMap.get(name);
    }
}
