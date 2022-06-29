package xmmt.dituon.plugin;

import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import org.junit.Test;
import xmmt.dituon.plugin.parser.PetpetParser;
import xmmt.dituon.plugin.parser.PetpetParserConfig;
import xmmt.dituon.plugin.parser.PetpetDrawStatement;
import xmmt.dituon.plugin.parser.hundun.statement.Statement;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PeteptParserTest {

    static PetpetParser parser;
    static {

        Map<String, List<String>> namesMap = Map.of("petpet", Arrays.asList("摸", "摸爆"));
        parser = new PetpetParser(new PetpetParserConfig(), namesMap);

    }


    MessageChain messageChain;
    Statement statement;
    PetpetDrawStatement petpetStatement;
    static final String offLineImageFakeId = "{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.jpg";
    Image actualImage = Image.fromId(offLineImageFakeId);
    long actualAtTarget = 114514L;

    @Test
    public void testImageCommand() {

        messageChain = new PlainText("pet ")
                .plus(" 摸")
                .plus(actualImage)
        ;
        statement = parser.simpleParse(messageChain);


        petpetStatement = (PetpetDrawStatement) statement;
        assertEquals("petpet", petpetStatement.getTemplateId());
        assertEquals(0, petpetStatement.getAdditionTexts().size());
        assertEquals(actualImage, petpetStatement.getImage());

    }

    @Test
    public void testNoMainName() {
        messageChain = new PlainText("摸 ").plus(new At(actualAtTarget));
        statement = parser.simpleParse(messageChain);

        // 输出
        petpetStatement = (PetpetDrawStatement) statement;
        assertEquals("petpet", petpetStatement.getTemplateId());
        assertEquals(0, petpetStatement.getAdditionTexts().size());
        assertEquals(actualAtTarget, petpetStatement.getAt().getTarget());
    }

    @Test
    public void testEndWithSpace() {
        // 输入
        messageChain = new PlainText("pet ")
                .plus(new At(actualAtTarget))
                .plus(" ")
                ;
        statement = parser.simpleParse(messageChain);

        petpetStatement = (PetpetDrawStatement) statement;
    }

    @Test
    public void testMainNameAndKey() {

        // 输入
        messageChain = new PlainText("pet ").plus(new At(actualAtTarget)).plus(" 摸");
        statement = parser.simpleParse(messageChain);

        // 输出
        petpetStatement = (PetpetDrawStatement) statement;
        assertEquals("petpet", petpetStatement.getTemplateId());
        assertEquals(0, petpetStatement.getAdditionTexts().size());
        assertEquals(actualAtTarget, petpetStatement.getAt().getTarget());

        messageChain = new PlainText("pet ").plus(new At(114514L)).plus(" 摸 嘻嘻");
        statement = parser.simpleParse(messageChain);

        petpetStatement = (PetpetDrawStatement) statement;
        assertEquals("petpet", petpetStatement.getTemplateId());
        assertEquals(1, petpetStatement.getAdditionTexts().size());
        assertEquals("嘻嘻", petpetStatement.getAdditionTexts().get(0));
        assertEquals(actualAtTarget, petpetStatement.getAt().getTarget());



    }


    @Test
    public void testNotCommand() {
        messageChain = new PlainText("pet 嘻嘻").plus("");
        statement = parser.simpleParse(messageChain);

        assertEquals("pet 嘻嘻", statement.getOriginMiraiCode());

        messageChain = new PlainText("嘻嘻").plus("");
        statement = parser.simpleParse(messageChain);

        assertEquals("嘻嘻", statement.getOriginMiraiCode());
    }
}
