package moe.dituon.petpet.core.length;

public interface PercentageLength extends Length {
    float getValue(LengthContext context, float parent);

    static PercentageLength fromString(String str) {
        str = str.trim();
        return str.startsWith(DynamicLength.EVAL_EXPR) ?
                DynamicPercentageLength.fromString(str) : NumberPercentageLength.fromString(str);
    }
}
