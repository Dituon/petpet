package moe.dituon.petpet.core.transform;

import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.PercentageLength;

public interface OffsetValue {
    int X = 0b0001;
    int Y = 0b0010;
    int XY = X | Y;

    boolean canLinkWith(OffsetValue value);
    int getOffsetType();
    Length getX();
    Length getY();

    static OffsetValue fromString(String str) {
        for (OffsetKeyword keyword : OffsetKeyword.values()) {
            if (keyword.keyword.equals(str)) return keyword;
        }
        return PercentageLength.fromString(str);
    }
}
