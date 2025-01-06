package moe.dituon.petpet.core.length;

public class DynamicPercentageLength extends DynamicLength implements PercentageLength {
    protected final boolean hasPercentageLength;

    public DynamicPercentageLength(String str) {
        super(str);
        this.hasPercentageLength = checkPercentage();
    }

    public DynamicPercentageLength(DynamicLength length) {
        super(length);
        this.hasPercentageLength = checkPercentage();
    }

    protected boolean checkPercentage() {
        return this.expression.getVariableNames().stream().anyMatch(PERCENT_REPLACE_SUFFIX::equals);
    }

    @Override
    public float getValue() {
        if (this.hasPercentageLength) {
            throw new IllegalArgumentException("percentage length no definite dimension");
        }
        return super.getValue();
    }

    @Override
    public float getValue(LengthContext context, float parent) {
        super.updateExpression(context);
        this.expression.setVariable(PERCENT_REPLACE_SUFFIX, parent);
        return (float) this.expression.evaluate();
    }

    public static PercentageLength fromString(String str) {
        var len = new DynamicPercentageLength(str);
        if (len.isAbsolute) {
            return new NumberPercentageLength(len.getValue(), LengthType.PX);
        }
        return len;
    }
}
