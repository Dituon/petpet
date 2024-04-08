package moe.dituon.petpet.share.position;

import kotlin.Pair;
import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonPrimitive;
import moe.dituon.petpet.share.ArithmeticParser;

public class PositionDynamicXYWHCollection extends PositionXYWHCollection {
    private final ArithmeticParser[][] arithmeticParsers;
    // TODO: 缓存动态坐标
    // private final WeakHashMap<PositionDynamicData, int[][]> cacheMap = new WeakHashMap<>(4);

    public PositionDynamicXYWHCollection(int[][] positions, ArithmeticParser[][] arithmeticParsers) {
        super(positions);
        this.arithmeticParsers = arithmeticParsers;
    }

    @Override
    public boolean isDynamical() {
        return true;
    }

    public int[] getPosition(int index, PositionDynamicData data) {
        var parsers = this.arithmeticParsers[index % pos.length];
        if (parsers == null) return getPosition(index);

        int[] result = getPosition(index).clone();
        for (int i = 0; i < parsers.length; i++) {
            var parser = parsers[i];
            if (parser == null) continue;
            result[i] = (int) data.inject(parser).eval();
        }

        return result;
    }

    public static PositionDynamicXYWHCollection fromJson(JsonArray posElements) {
        var pos = new int[posElements.size()][4];
        var parsers = new ArithmeticParser[posElements.size()][];
        if (PositionCollection.calculateDepth(posElements) == 1) {
            var pair = parseJsonArray(posElements);
            pos[0] = pair.getFirst();
            parsers[0] = pair.getSecond();
            return new PositionDynamicXYWHCollection(pos, parsers);
        }
        int i = 0;
        for (JsonElement je : posElements) {
            JsonArray ja = (JsonArray) je;
            if (ja.size() != 4) {
                throw new RuntimeException("Invalid position format");
            }
            var pair = parseJsonArray(ja);
            pos[i] = pair.getFirst();
            parsers[i] = pair.getSecond();
            i++;
        }
        return new PositionDynamicXYWHCollection(pos, parsers);
    }

    private static Pair<int[], ArithmeticParser[]> parseJsonArray(JsonArray ja) {
        int[] result = new int[ja.size()];
        ArithmeticParser[] parsers = null;
        short i = 0;
        for (JsonElement je : ja) {
            if (je instanceof JsonArray) {
                return parseJsonArray((JsonArray) je);
            }
            String str = ((JsonPrimitive) je).getContent();
            try {
                result[i] = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                ArithmeticParser parser = new ArithmeticParser(str);
                if (parsers == null) {
                    parsers = new ArithmeticParser[ja.size()];
                }
                parsers[i] = parser;
            }
            i++;
        }
        return new Pair<>(result, parsers);
    }
}
