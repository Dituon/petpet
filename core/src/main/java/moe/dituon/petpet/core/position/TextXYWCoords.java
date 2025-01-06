package moe.dituon.petpet.core.position;

import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.LengthType;
import moe.dituon.petpet.core.length.NumberLength;

import java.util.List;

public class TextXYWCoords {
    public static final int DEFAULT_WIDTH_I = 200;
    public static final Length DEFAULT_WIDTH = new NumberLength(DEFAULT_WIDTH_I, LengthType.PX);
    public static final TextXYWCoords DEFAULT = new TextXYWCoords(
            List.of(NumberLength.EMPTY, NumberLength.EMPTY, DEFAULT_WIDTH)
    );
    public final List<Length> coordsList;
    public final boolean isAbsolute;

    public TextXYWCoords(String x, String y, String width) {
        this(List.of(Length.fromString(x), Length.fromString(y), Length.fromString(width)));
    }

    public TextXYWCoords(String x, String y) {
        this(List.of(Length.fromString(x), Length.fromString(y), DEFAULT_WIDTH));
    }

    public TextXYWCoords(int x, int y, int width) {
        this(List.of(new NumberLength(x, LengthType.PX), new NumberLength(y, LengthType.PX), new NumberLength(width, LengthType.PX)));
    }

    public TextXYWCoords(int x, int y) {
        this(List.of(new NumberLength(x, LengthType.PX), new NumberLength(y, LengthType.PX), DEFAULT_WIDTH));
    }

    public TextXYWCoords(List<Length> coordsList) {
        int len = coordsList.size();
        if (len == 2) {
            this.coordsList = List.of(coordsList.get(0), coordsList.get(1), DEFAULT_WIDTH);
        } else if (len != 3) {
            throw new IllegalArgumentException("XYW pos format must has 3 element");
        } else {
            this.coordsList = coordsList;
        }
        this.isAbsolute = coordsList.stream().allMatch(Length::isAbsolute);
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public int[] getValue() {
        return coordsList.stream()
                .mapToInt(l -> Math.round(l.getValue()))
                .toArray();
    }

    /**
     * @return int[x, y, width]
     */
    public int[] getValue(LengthContext context) {
        return coordsList.stream()
                .mapToInt(l -> Math.round(l.getValue(context)))
                .toArray();
    }

    public int getWidth(LengthContext context) {
        if (coordsList.size() < 3) {
            return DEFAULT_WIDTH_I;
        }
        return Math.round(coordsList.get(2).getValue(context));
    }
}
