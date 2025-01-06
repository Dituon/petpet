package moe.dituon.petpet.core.utils.expression;

import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.function.Functions;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.shuntingyard.ShuntingYard;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LengthExpressionBuilder {
    protected final String expression;
    protected static final Map<String, Function> userFunctions = Map.of(
            "max", new Function("max", 2) {
                @Override
                public double apply(double... doubles) {
                    return Math.max(doubles[0], doubles[1]);
                }
            },
            "min", new Function("min", 2) {
                @Override
                public double apply(double... doubles) {
                    return Math.min(doubles[0], doubles[1]);
                }
            }
    );
    protected static final Map<String, Operator> userOperators = Collections.emptyMap();
    protected final Set<String> variableNames;

    public LengthExpressionBuilder(String expression) {
        if (expression != null && !expression.trim().isEmpty()) {
            this.expression = expression;
            this.variableNames = new HashSet<>(16);
        } else {
            throw new IllegalArgumentException("Expression can not be empty");
        }
    }

    public LengthExpressionBuilder variables(Set<String> variableNames) {
        this.variableNames.addAll(variableNames);
        return this;
    }

    public LengthExpressionBuilder variables(String... variableNames) {
        Collections.addAll(this.variableNames, variableNames);
        return this;
    }

    public LengthExpressionBuilder variable(String variableName) {
        this.variableNames.add(variableName);
        return this;
    }

    public LengthExpression build() {
        if (this.expression.isEmpty()) {
            throw new IllegalArgumentException("The expression can not be empty");
        } else {
            for (String token : variableNames) {
                if (Functions.getBuiltinFunction(token) != null || userFunctions.containsKey(token)) {
                    throw new IllegalArgumentException("A variable can not have the same name as a function [" + token + "]");
                }
            }
            return new LengthExpression(ShuntingYard.convertToRPN(this.expression, userFunctions, userOperators,
                    this.variableNames, true), userFunctions.keySet());
        }
    }
}

