import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.PetpetTemplateModel;
import moe.dituon.petpet.core.imgres.ImageResourceMap;
import moe.dituon.petpet.old_template.OldPetpetTemplate;
import moe.dituon.petpet.template.PetpetTemplate;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class MainTest {
//    @Test
//    void testAll() throws IOException {
//        int startIndex = 0;
//        File basePath = new File("D:\\dev\\petpet-new\\data\\xmmt.dituon.petpet");
//        File[] childBase = basePath.listFiles();
//        int i = 0;
//        for (File base : childBase) {
//            if (i++ < startIndex) continue;
//            testOne(base);
//        }
//    }

    @Test
    void testOne() throws IOException {
//        File basePath = new File("D:\\bot-test\\overflow\\data\\xmmt.dituon.petpet\\stew");
//        File basePath = new File("D:\\bot-test\\overflow\\data\\xmmt.dituon.petpet.bck\\stew");
        File basePath = new File("D:\\dev\\petpet-templates\\templates\\mirage");
        testOne(basePath);
        System.out.println("done");
    }

    public static void testOne(File base) throws IOException {
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
        var img = model.draw(new RequestContext(
                ImageResourceMap.fromStringMap(Map.of(
                        "to", "https://avatars.githubusercontent.com/u/68615161?v=4",
//                            "to", "C:\\Users\\jiang\\Pictures\\e1b3ebc5c3d8895cc7bc4111f7595dff.jpg",
//                        "to", "https://user-images.githubusercontent.com/6876788/96633009-d1818000-1318-11eb-9f1d-7f914f4ccb16.gif",
                        "from", "https://avatars.githubusercontent.com/u/5362918?v=4"
                )),
//                Map.of("1", "测试测试测试测试测试测试测试测试测试测试测试")
                Map.of()
        ));
        System.out.printf("%s built", base.getName());
        var outFile = Paths.get("./test-output/" + base.getName() + "." + img.format);
        if (!Files.exists(outFile)) {
            Files.createDirectories(outFile.getParent());
        }
        System.out.printf("output size: %s; format: %s", img.bytes.length, img.format);
        Files.write(outFile, img.bytes);
        System.out.println("path: " + outFile);
    }

    public static void main(String[] args) {

    }
}
