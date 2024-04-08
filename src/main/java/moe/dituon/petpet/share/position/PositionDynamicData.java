package moe.dituon.petpet.share.position;

import moe.dituon.petpet.share.ArithmeticParser;

public class PositionDynamicData {
    private int width;
    private int height;

    public ArithmeticParser inject(ArithmeticParser parser) {
        parser.put("width", width);
        parser.put("height", height);
        return parser;
    }

    public static PositionDynamicData fromWH(int width, int height) {
        PositionDynamicData data = new PositionDynamicData();
        data.width = width;
        data.height = height;
        return data;
    }
}
