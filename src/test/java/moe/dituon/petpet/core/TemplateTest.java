package moe.dituon.petpet.core;

import moe.dituon.petpet.share.TemplateDTO;
import moe.dituon.petpet.share.service.ResourceManager;
import moe.dituon.petpet.share.template.AvatarExtraData;
import moe.dituon.petpet.share.template.ExtraData;
import moe.dituon.petpet.share.template.PetpetTemplate;
import moe.dituon.petpet.share.template.TemplateBuilder;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TemplateTest {
    ResourceManager manager = ResourceManager.getDefaultInstance();

    @Test
    public void testPng() throws IOException {
        var basePath = Path.of("data/xmmt.dituon.petpet/ask");
        manager.pushBackground(basePath.toFile(), false);

        var templateRaw = basePath.resolve("data.json");
        var templateConfig = PetpetTemplate.fromString(Files.readString(templateRaw));

        var templateBuilder = new TemplateBuilder(
                templateConfig,
                manager.getBackgrounds("ask")
        );

        var templateModel = templateBuilder.build(TestUtils.getExtraData(templateBuilder));

        ByteArrayInputStream stream = new ByteArrayInputStream(templateModel.getResult().getBlob());
        Files.copy(stream, Path.of("test.png"), StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testGif() throws IOException {
        var basePath = Path.of("data/xmmt.dituon.petpet/petpet");
        manager.pushBackground(basePath.toFile(), false);

        var templateRaw = basePath.resolve("data.json");
        var templateConfig = PetpetTemplate.fromString(Files.readString(templateRaw));

        var templateBuilder = new TemplateBuilder(
                templateConfig,
                manager.getBackgrounds("petpet")
        );

        var templateModel = templateBuilder.build(TestUtils.getExtraData(templateBuilder));

        ByteArrayInputStream stream = new ByteArrayInputStream(templateModel.getResult().getBlob());
        Files.copy(stream, Path.of("test.gif"), StandardCopyOption.REPLACE_EXISTING);
    }
}
