package moe.dituon.petpet.core;

import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonArray;
import moe.dituon.petpet.share.position.PositionCollectionFactory;
import moe.dituon.petpet.share.position.PositionDynamicData;
import org.junit.Test;

import java.util.Arrays;

public class PositionTest {
    static JsonArray toJsonArray(String str) {
        return Json.Default.decodeFromString(JsonArray.Companion.serializer(), str);
    }

    @Test
    public void testXYWH() {
        // multiple pos
        var str = "[[100, 200, 300, 400], [400, 300, 300, 100]]";
        JsonArray array = toJsonArray(str);
        var pos = PositionCollectionFactory.createXYWHCollection(array);
        assert Arrays.toString(pos.getPosition(1)).equals("[400, 300, 300, 100]");

        // single pos
        str = "[100, 200, 300, 400]";
        array = toJsonArray(str);
        pos = PositionCollectionFactory.createXYWHCollection(array);
        assert Arrays.toString(pos.getPosition(0)).equals("[100, 200, 300, 400]");
    }

    @Test
    public void testDynamicXYWH() {
        final int width = 100, height = 300;

        var str = "[[100, \"" + width + " + 100\", \"" + height + "\", 400], [400, 300, 300, 100]]";
        JsonArray array = toJsonArray(str);
        var pos = PositionCollectionFactory.createXYWHCollection(array);
        assert pos.isDynamical();
        var data = PositionDynamicData.fromWH(width, height);
        assert Arrays.toString(pos.getPosition(0, data)).equals("[100, 200, 300, 400]");
    }

    @Test
    public void testP4A() {
        final var fPos = "[Point2D.Float[100.0, 100.0], Point2D.Float[200.0, 200.0], Point2D.Float[300.0, 300.0], Point2D.Float[400.0, 400.0]]";
        final var fAnchor = "[50, 50, 0, 0]";

        // single pos
        var str = "[[100, 100], [200, 200], [300, 300], [400, 400], [50, 50]]";
        JsonArray array = toJsonArray(str);
        var pos = PositionCollectionFactory.createP4ACollection(array);
        assert Arrays.toString(pos.getPosition(0)).equals(fPos);
        assert Arrays.toString(pos.getAnchor(0)).equals(fAnchor);

        // multiple pos
        str = '[' + str + ", " + str + ']';
        array = toJsonArray(str);
        pos = PositionCollectionFactory.createP4ACollection(array);
        assert Arrays.toString(pos.getPosition(0)).equals(fPos);
        assert Arrays.toString(pos.getAnchor(0)).equals(fAnchor);
    }
}
