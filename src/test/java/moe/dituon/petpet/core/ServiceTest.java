package moe.dituon.petpet.core;

import moe.dituon.petpet.share.service.PetpetService;
import moe.dituon.petpet.share.service.PetpetServiceConfig;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ServiceTest {
    private final static boolean testAll = false;

    @Test
    public void testAll() throws IOException {
        if (!testAll) return;
        var service = new PetpetService(new PetpetServiceConfig());
        service.getTemplateManager().pushBasePath(new File("data/xmmt.dituon.petpet/"));
        var templateMap = service.getTemplateManager().getTemplateMap();

        templateMap.forEach((name, template) -> {
            System.out.println(name);
            try {
                var model = template.build(TestUtils.getExtraData(template));
                model.getResult().saveAs("test/out/" + name + "." + model.getResult().getSuffix());
            } catch (IOException e) {
//                 e.printStackTrace();
            }
        });
    }
}
