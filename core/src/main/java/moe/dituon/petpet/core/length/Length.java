package moe.dituon.petpet.core.length;

import moe.dituon.petpet.core.transform.OffsetValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface Length extends OffsetValue {

    LengthType getType();

    /**
     * The length are absolute <br>
     * Not depend on any variable
     */
    boolean isAbsolute();

    float getValue();

    float getRawValue();

    float getValue(LengthContext context);

    /**
     * The length are relative to the canvas size <br>
     * e.g. length type is <code>vw</code> <code>vh</code>
     */
    default boolean isDependOnCanvasSize() {
        return false;
    }

    /**
     * The length are relative to the element size <br>
     * e.g. length type is <code>ew</code> <code>eh</code>
     */
    default boolean isDependOnElementSize() {
        return false;
    }

    default Set<String> getDependentIds() {
        return Collections.emptySet();
    }

    @Override
    default boolean canLinkWith(OffsetValue value) {
        return true;
    }

    @Override
    default int getOffsetType() {
        return OffsetValue.XY;
    }

    @Override
    default Length getX() {
        return this;
    }

    @Override
    default Length getY() {
        return this;
    }

    static Length fromString(String str) {
        str = str.trim();
        try {
            return Character.isDigit(str.charAt(0)) ?
                    NumberLength.fromString(str) : DynamicLength.fromString(str);
        } catch (NumberFormatException ex) {
            return DynamicLength.fromString(str);
        }
    }

    /**
     * 分割字符串中各参数 <n/>
     * <blockquote><pre>
     *     var str = "100px 200px calc(100vw - 20px) 400px";
     *     var list = Length.splitString(str);
     *     // output: list == ["100px", "200px", "calc(100vw - 20px)", "400px"]
     *</pre></blockquote>
     */
    static List<String> splitString(String str) {
        List<String> result = new ArrayList<>();
        if (str == null || str.isEmpty()) {
            return result;
        }

        StringBuilder currentToken = new StringBuilder();
        int parenthesisCount = 0;
        char[] chars = str.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '(':
                    parenthesisCount++;
                    currentToken.append(chars[i]);
                    break;
                case ')':
                    parenthesisCount--;
                    currentToken.append(chars[i]);
                    break;
                case ' ':
                    if (parenthesisCount == 0) {
                        if (currentToken.length() > 0) {
                            result.add(currentToken.toString());
                            currentToken.setLength(0);
                        }
                    } else {
                        currentToken.append(chars[i]);
                    }
                    break;
                default:
                    currentToken.append(chars[i]);
                    break;
            }
        }

        // Add the last token if any
        if (currentToken.length() > 0) {
            result.add(currentToken.toString());
        }

        return result;
    }
}
