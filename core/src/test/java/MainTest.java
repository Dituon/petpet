import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.PetpetTemplateModel;
import moe.dituon.petpet.core.imgres.BufferedImageResource;
import moe.dituon.petpet.core.imgres.ImageResourceMap;
import moe.dituon.petpet.old_template.OldPetpetTemplate;
import moe.dituon.petpet.template.PetpetTemplate;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class MainTest {
    private final BufferedImage testInputImage = ImageIO.read(new File(
            getClass().getClassLoader().getResource("test-images/input.png").getFile()
    ));
    private final ImageResourceMap testImageResourceMap = new ImageResourceMap(Map.of(
            "from", new BufferedImageResource(testInputImage),
            "to", new BufferedImageResource(testInputImage)
    ));
    private final Map<String, String> testStringMap = Map.of(
            "from", "FROM",
            "to", "TO"
    );

    public MainTest() throws IOException {
    }

//    @Test
//    void testAll() throws IOException {
//        int startIndex = 0;
//        File basePath = new File("D:\\dev\\petpet-templates\\templates");
//        File[] childBase = basePath.listFiles();
//        int i = 0;
//        for (File base : childBase) {
//            if (i++ < startIndex) continue;
//            testOne(base);
//        }
//    }

//    @Test
//    void testOne() throws IOException {
//        File basePath = new File("D:\\dev\\petpet-templates\\templates\\breakdown");
//        testOne(basePath);
//    }

    public void testOne(File base) throws IOException {
        PetpetTemplate template;
        if (Files.exists(base.toPath().resolve("template.json"))) {
            template = PetpetTemplate.fromJsonFile((base.toPath().resolve("template.json").toFile()));
        } else {
            var file = base.toPath().resolve("data.json").toFile();
            if (!file.exists()) return;
            var oldTemplate = OldPetpetTemplate.fromJsonFile(file);
            template = oldTemplate.toTemplate();
        }

        var model = new PetpetTemplateModel(template);
        var img = model.draw(new RequestContext(testImageResourceMap, testStringMap));
        var outFile = Paths.get("./test-output/" + base.getName() + "." + img.format);
        if (!Files.exists(outFile)) {
            Files.createDirectories(outFile.getParent());
        }
        Files.write(outFile, img.bytes);
        System.out.printf("template: %s: output size: %s; format: %s; path: %s%n",
                base.getName(), img.bytes.length, img.format, outFile);
    }

    public static void main(String[] args) {

    }
}
