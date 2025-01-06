package moe.dituon.petpet.core.transform;

import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.PercentageLength;

public class Offset {
    public static final Offset LEFT_TOP = new Offset(OffsetKeyword.LEFT, OffsetKeyword.TOP);
    public static final Offset EMPTY = new Offset(OffsetKeyword.CENTER, OffsetKeyword.CENTER);
    public static final Offset CENTER = EMPTY;
    public final PercentageLength xOffset;
    public final PercentageLength yOffset;

    public Offset(OffsetValue xOffset, OffsetValue yOffset) {
        this.xOffset = (PercentageLength) ((xOffset.getOffsetType() & OffsetValue.X) != 0 ? xOffset : OffsetKeyword.CENTER).getX();
        this.yOffset = (PercentageLength) ((yOffset.getOffsetType() & OffsetValue.Y) != 0 ? yOffset : OffsetKeyword.CENTER).getY();
    }

    public boolean isAbsolute() {
        return xOffset.isAbsolute() && yOffset.isAbsolute();
    }

    public static Offset fromString(String str) {
        var tokenList = Length.splitString(str);
        switch (tokenList.size()) {
            case 1:
                var value = OffsetValue.fromString(tokenList.get(0));
                if ((value.getOffsetType() & OffsetValue.X) != 0) {
                    return new Offset(value, OffsetKeyword.CENTER);
                } else {
                    return new Offset(OffsetKeyword.CENTER, value);
                }
            case 2:
                return new Offset(
                        OffsetValue.fromString(tokenList.get(0)),
                        OffsetValue.fromString(tokenList.get(1))
                );
            default:
                throw new IllegalArgumentException("offset must has 1 ~ 2 argument");
        }
    }

    @Override
    public String toString() {
        return xOffset.toString() + ' ' + yOffset.toString();
    }
}
