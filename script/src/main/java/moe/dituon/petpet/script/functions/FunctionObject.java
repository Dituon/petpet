package moe.dituon.petpet.script.functions;

import org.openjdk.nashorn.api.scripting.AbstractJSObject;

public abstract class FunctionObject extends AbstractJSObject {
    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public boolean isStrictFunction() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    public abstract Object call(Object thiz, Object... args);

    public static FunctionObject of(int paramCount, FunctionCall functionCall) {
        return new FunctionObject() {
            @Override
            public Object call(Object thiz, Object... args) {
                if (args.length != paramCount) {
                    throw new IllegalArgumentException("Function requires " + paramCount + " arguments");
                }
                return functionCall.call(thiz, args);
            }
        };
    }

    public static FunctionObject of(FunctionCall functionCall) {
        return new FunctionObject() {
            @Override
            public Object call(Object thiz, Object... args) {
                return functionCall.call(thiz, args);
            }
        };
    }

    // Functional interface for lambda support
    @FunctionalInterface
    public interface FunctionCall {
        Object call(Object thiz, Object... args);
    }
}
