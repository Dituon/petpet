import moe.dituon.petpet.service.TemplateManger;
import org.junit.jupiter.api.Test;

import java.io.File;

class TemplateManagerTest {
    @Test
    void testLoadTemplates() throws Exception {
        var templateManager = new TemplateManger(){};
        templateManager.addTemplates(new File("src/test/resources/test-templates"));
        templateManager.updateScriptService();
    }
}
