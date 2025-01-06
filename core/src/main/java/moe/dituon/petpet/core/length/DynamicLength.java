package moe.dituon.petpet.core.length;

import moe.dituon.petpet.core.utils.expression.LengthExpression;
import moe.dituon.petpet.core.utils.expression.LengthExpressionBuilder;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DynamicLength implements Length {
    public static final String EVAL_EXPR = "calc";
    public static final String PERCENT_REPLACE_SUFFIX = "percent";
    public static final String ELEMENT_WIDTH_SUFFIX = "_width";
    public static final String ELEMENT_HEIGHT_SUFFIX = "_height";
    public static final String ELEMENT_LENGTH_SUFFIX = "_length";
    protected static final Pattern ELEMENT_VARS_PATTERN = Pattern.compile("(?!\\d)(\\w+?_width|\\w+?_height|\\w+?_length)");

    protected static final String[] SUFFIX_VARS = new String[]{
            LengthType.VW.suffix,
            LengthType.VH.suffix,
            LengthType.CW.suffix,
            LengthType.CH.suffix,
            PERCENT_REPLACE_SUFFIX
    };

    protected static final Set<String> DYNAMIC_VAR_SET = Set.of(SUFFIX_VARS);

    public final LengthExpression expression;
    public final String rawExpr;
    public final boolean isAbsolute;

    private final Set<String> variableNames;
    private Set<String> dependentIds = null;

    public DynamicLength(String str) {

        var builder = new LengthExpressionBuilder(
                str.replace(EVAL_EXPR, "")
                        .replace(LengthType.PX.suffix, "")
                        .replace(LengthType.PERCENT.suffix, PERCENT_REPLACE_SUFFIX)
        ).variables(SUFFIX_VARS);


        boolean varFlag = false;
        if (str.contains(ELEMENT_WIDTH_SUFFIX)
                || str.contains(ELEMENT_HEIGHT_SUFFIX)
                || str.contains(ELEMENT_LENGTH_SUFFIX)
        ) {
            var matcher = ELEMENT_VARS_PATTERN.matcher(str);
            while (matcher.find()) {
                var varName = matcher.group(1);
                builder.variable(varName);
            }
            varFlag = true;
        }

        this.expression = builder.build();
        this.rawExpr = str;
        this.variableNames = this.expression.getVariableNames();
        this.isAbsolute = !varFlag && variableNames.isEmpty();
    }

    public DynamicLength(DynamicLength length) {
        this.expression = length.expression;
        this.rawExpr = length.rawExpr;
        this.isAbsolute = length.isAbsolute;
        this.variableNames = length.variableNames;
        this.dependentIds = length.dependentIds;
    }

    public static Length fromString(String str) {
        var len = new DynamicLength(str);
        if (len.isAbsolute) {
            return new NumberLength(len.getValue(), LengthType.PX);
        }
        return len;
    }

    @Override
    public LengthType getType() {
        return LengthType.PX;
    }

    @Override
    public boolean isAbsolute() {
        return isAbsolute;
    }

    @Override
    public boolean isDependOnCanvasSize() {
        for (String token : this.variableNames) {
            if (token.equals(LengthType.VW.suffix) || token.equals(LengthType.VH.suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDependOnElementSize() {
        for (String token : this.variableNames) {
            if (token.equals(LengthType.CW.suffix)
                    || token.equals(LengthType.CH.suffix)
                    || token.equals(PERCENT_REPLACE_SUFFIX)
            ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getValue() {
        return getValue(LengthContext.EMPTY);
    }

    @Override
    public float getRawValue() {
        return 0f;
    }

    @Override
    public float getValue(LengthContext context) {
        this.updateExpression(context);
        return (float) this.expression.evaluate();
    }

    protected void updateExpression(LengthContext context) {
        this.expression
                .setVariable(LengthType.VW.suffix, context.canvasWidth * 0.01d)
                .setVariable(LengthType.VH.suffix, context.canvasHeight * 0.01d)
                .setVariable(LengthType.CW.suffix, context.elementWidth * 0.01d)
                .setVariable(LengthType.CH.suffix, context.elementHeight * 0.01d);
        this.expression.setIntVariables(context.variables);
    }

    @Override
    public Set<String> getDependentIds() {
        if (this.dependentIds != null) return this.dependentIds;
        if (this.isAbsolute) {
            this.dependentIds = Collections.emptySet();
            return this.dependentIds;
        }
        this.dependentIds = this.variableNames.stream()
                .filter(token -> !DYNAMIC_VAR_SET.contains(token))
                .map(s -> s.replace(ELEMENT_WIDTH_SUFFIX, ""))
                .map(s -> s.replace(ELEMENT_HEIGHT_SUFFIX, ""))
                .map(s -> s.replace(ELEMENT_LENGTH_SUFFIX, ""))
                .collect(Collectors.toSet());
        return this.dependentIds;
    }

    public DynamicLength plus(Length other) {
        return new DynamicLength(this.rawExpr + '+' + other.toString());
    }

    @Override
    public String toString() {
        return this.rawExpr;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DynamicLength)) return false;

        DynamicLength that = (DynamicLength) o;
        return rawExpr.equals(that.rawExpr);
    }

    @Override
    public int hashCode() {
        return rawExpr.hashCode();
    }
}
