package moe.dituon.petpet.share.position;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonPrimitive;

import java.awt.geom.Point2D;

public class PositionP4ACollection implements PositionCollection<Point2D[]> {
    protected final Point2D[][] pos;
    protected final int[][] anchor;

    public static final int POS_SIZE = 4;

    PositionP4ACollection(Point2D[][] pos, int[][] anchor) {
        this.pos = pos;
        this.anchor = anchor;
    }

    public int[] getAnchor(int index) {
        return anchor[index % anchor.length];
    }

    @Override
    public Point2D[] getPosition(int index) {
        return pos[index % pos.length];
    }

    @Override
    public Point2D[] getPosition(int index, PositionDynamicData data) {
        return getPosition(index);
    }

    @Override
    public boolean isDynamical() {
        return false;
    }

    public static PositionP4ACollection fromJson(JsonArray posElements) {
        int i = 0;
        var pos = new Point2D[posElements.size()][4];
        var anchor = new int[posElements.size()][4];
        if (PositionCollection.calculateDepth(posElements) == 2) {
            parseJsonArray(posElements, pos[0], anchor[0]);
            return new PositionP4ACollection(pos, anchor);
        }
        for (JsonElement je : posElements) {
            JsonArray ja = (JsonArray) je;
            parseJsonArray(ja, pos[i], anchor[i]);
            i++;
        }
        return new PositionP4ACollection(pos, anchor);
    }

    protected static void parseJsonArray(JsonArray posElements, Point2D[] pos, int[] anchor) {
        for (short i = 0; i < POS_SIZE; i++) {
            var ele = (JsonArray) posElements.get(i);
            pos[i] = new Point2D.Float(
                    Float.parseFloat(((JsonPrimitive) ele.get(0)).getContent()),
                    Float.parseFloat(((JsonPrimitive) ele.get(1)).getContent())
            );
        }
        var ele = (JsonArray) posElements.get(POS_SIZE);
        anchor[0] = Integer.parseInt(((JsonPrimitive) ele.get(0)).getContent());
        anchor[1] = Integer.parseInt(((JsonPrimitive) ele.get(1)).getContent());
    }
}
