package moe.dituon.petpet.share.position;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonPrimitive;

public class PositionXYWHCollection implements PositionCollection<int[]> {
    // 出于性能考虑, 原则上不将坐标元素对象化
    protected final int[][] pos;

    public PositionXYWHCollection(int[][] positions) {
        this.pos = positions;
    }

    @Override
    public int[] getPosition(int index) {
        return pos[index % pos.length];
    }

    @Override
    public int[] getPosition(int index, PositionDynamicData data) {
        return getPosition(index);
    }

    @Override
    public int size() {
        return pos.length;
    }

    @Override
    public boolean isDynamical() {
        return false;
    }

    public static PositionXYWHCollection fromJson(JsonArray posElements) {
        int i = 0;

        if (PositionCollection.calculateDepth(posElements) == 1) {
            return new PositionXYWHCollection(new int[][]{parseJsonArray(posElements)});
        }
        var pos = new int[posElements.size()][4];
        for (JsonElement je : posElements) {
            JsonArray ja = (JsonArray) je;
            if (ja.size() != 4) {
                throw new RuntimeException("Invalid position format");
            }
            pos[i++] = parseJsonArray(ja);
        }
        return new PositionXYWHCollection(pos);
    }

    private static int[] parseJsonArray(JsonArray ja) {
        int[] result = new int[ja.size()];
        short i = 0;
        for (JsonElement je : ja) {
            if (je instanceof JsonArray) {
                return parseJsonArray((JsonArray) je);
            }
            String str = ((JsonPrimitive) je).getContent();
            result[i] = Integer.parseInt(str);
            i++;
        }
        return result;
    }
}
