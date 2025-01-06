package moe.dituon.petpet.core.length;

public class NumberPercentageLength extends NumberLength implements PercentageLength {
    public NumberPercentageLength(float length, LengthType type) {
        super(length, type);
    }

    public NumberPercentageLength(NumberLength length) {
        super(length.value, length.type);
    }

    @Override
    public float getValue(LengthContext context) {
        if (type == LengthType.PERCENT) {
            throw new IllegalArgumentException("percentage length no definite dimension");
        }
        return getValueWithoutCheck(context, super.type);
    }

    @Override
    public float getValue(LengthContext context, float parent) {
        return getValueWithoutCheck(context, super.type, parent);
    }

    public static NumberPercentageLength fromString(String str) {
        return new NumberPercentageLength(NumberLength.fromString(str));
    }
}
