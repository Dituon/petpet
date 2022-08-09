package xmmt.dituon.example;

import kotlin.Pair;
import xmmt.dituon.share.AvatarExtraDataProvider;
import xmmt.dituon.share.BasePetService;
import xmmt.dituon.share.TextData;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public abstract class AbstractServiceTest extends AbstractTest{

    static BasePetService petService = new BasePetService();


    protected void testGeneral(String saveName, String key, List<TextData> additionTextDatas) {
        Pair<InputStream, String> resultStreamAndType = petService.generateImage(key,
                new AvatarExtraDataProvider(() -> avatarImage1, () -> avatarImage2,
                        null, null)
                , null, additionTextDatas);
        String finalSaveName = OUTPUT_ROOT + getClass().getSimpleName() + "-" + saveName + "." + resultStreamAndType.getSecond();
        copyInputStreamToFile(resultStreamAndType.getFirst(), new File(finalSaveName));
        System.out.println("test " + key + " done.");
    }

}
