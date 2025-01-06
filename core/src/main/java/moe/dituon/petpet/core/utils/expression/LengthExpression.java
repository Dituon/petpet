package moe.dituon.petpet.core.utils.expression;

import net.objecthunter.exp4j.ValidationResult;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.tokenizer.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class LengthExpression {
    private final Token[] tokens;
    private final Map<String, Double> variables;
    private final Set<String> userFunctionNames;

    private static Map<String, Double> createDefaultVariables() {
        return new HashMap<>(16);
    }

    public LengthExpression(LengthExpression existing) {
        this.tokens = Arrays.copyOf(existing.tokens, existing.tokens.length);
        this.variables = new HashMap<>();
        this.variables.putAll(existing.variables);
        this.userFunctionNames = new HashSet<>(existing.userFunctionNames);
    }

    public LengthExpression(Token[] tokens) {
        this.tokens = tokens;
        this.variables = createDefaultVariables();
        this.userFunctionNames = Collections.emptySet();
    }

    public LengthExpression(Token[] tokens, Set<String> userFunctionNames) {
        this.tokens = tokens;
        this.variables = createDefaultVariables();
        this.userFunctionNames = userFunctionNames;
    }

    public LengthExpression setVariable(String name, double value) {
        this.variables.put(name, value);
        return this;
    }

    public LengthExpression setIntVariables(Map<String, Integer> variables) {
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            this.variables.put(entry.getKey(), entry.getValue().doubleValue());
        }
        return this;
    }

    public LengthExpression setVariables(Map<String, Double> variables) {
        this.variables.putAll(variables);
        return this;
    }

    public Set<String> getVariableNames() {
        final Set<String> vars = new HashSet<>();
        for (final Token t : tokens) {
            if (t.getType() == Token.TOKEN_VARIABLE)
                vars.add(((VariableToken) t).getName());
        }
        return vars;
    }

    public ValidationResult validate(boolean checkVariablesSet) {
        final List<String> errors = new ArrayList<>(0);
        if (checkVariablesSet) {
            /* check that all vars have a value set */
            for (final Token t : this.tokens) {
                if (t.getType() == Token.TOKEN_VARIABLE) {
                    final String token = ((VariableToken) t).getName();
                    if (!variables.containsKey(token)) {
                        errors.add("The setVariable '" + token + "' has not been set");
                    }
                }
            }
        }

        /* Check if the number of operands, functions and operators match.
           The idea is to increment a counter for operands and decrease it for operators.
           When a function occurs the number of available arguments has to be greater
           than or equals to the function's expected number of arguments.
           The count has to be larger than 1 at all times and exactly 1 after all tokens
           have been processed */
        int count = 0;
        for (Token tok : this.tokens) {
            switch (tok.getType()) {
                case Token.TOKEN_NUMBER:
                case Token.TOKEN_VARIABLE:
                    count++;
                    break;
                case Token.TOKEN_FUNCTION:
                    final Function func = ((FunctionToken) tok).getFunction();
                    final int argsNum = func.getNumArguments();
                    if (argsNum > count) {
                        errors.add("Not enough arguments for '" + func.getName() + "'");
                    }
                    if (argsNum > 1) {
                        count -= argsNum - 1;
                    } else if (argsNum == 0) {
                        // see https://github.com/fasseg/exp4j/issues/59
                        count++;
                    }
                    break;
                case Token.TOKEN_OPERATOR:
                    Operator op = ((OperatorToken) tok).getOperator();
                    if (op.getNumOperands() == 2) {
                        count--;
                    }
                    break;
            }
            if (count < 1) {
                errors.add("Too many operators");
                return new ValidationResult(false, errors);
            }
        }
        if (count > 1) {
            errors.add("Too many operands");
        }
        return errors.isEmpty() ? ValidationResult.SUCCESS : new ValidationResult(false, errors);

    }

    public ValidationResult validate() {
        return this.validate(true);
    }

    public Future<Double> evaluateAsync(ExecutorService executor) {
        return executor.submit(LengthExpression.this::evaluate);
    }

    public double evaluate() {
        ArrayStack output = new ArrayStack();

        for (Token t : this.tokens) {
            if (t.getType() == 1) {
                output.push(((NumberToken) t).getValue());
            } else if (t.getType() == 6) {
                String name = ((VariableToken) t).getName();
                Double value = this.variables.get(name);
                if (value == null) {
                    throw new IllegalArgumentException("No value has been set for the setVariable '" + name + "'.");
                }

                output.push(value);
            } else if (t.getType() == 2) {
                OperatorToken op = (OperatorToken) t;
                if (output.size() < op.getOperator().getNumOperands()) {
                    throw new IllegalArgumentException("Invalid number of operands available for '" + op.getOperator().getSymbol() + "' operator");
                }

                double arg;
                if (op.getOperator().getNumOperands() == 2) {
                    arg = output.pop();
                    double leftArg = output.pop();
                    output.push(op.getOperator().apply(leftArg, arg));
                } else if (op.getOperator().getNumOperands() == 1) {
                    arg = output.pop();
                    output.push(op.getOperator().apply(arg));
                }
            } else if (t.getType() == Token.TOKEN_FUNCTION) {
                FunctionToken func = (FunctionToken) t;
                final int numArguments = func.getFunction().getNumArguments();
                if (output.size() < numArguments) {
                    throw new IllegalArgumentException("Invalid number of arguments available for '" + func.getFunction().getName() + "' function");
                }
                /* collect the arguments from the stack */
                double[] args = new double[output.size()];
                for (int j = numArguments - 1; j >= 0; j--) {
                    args[j] = output.pop();
                }
                output.push(func.getFunction().apply(args));
            }
        }

        if (output.size() > 1) {
            throw new IllegalArgumentException("Invalid number of items on the output queue. Might be caused by an invalid number of arguments for a function.");
        } else {
            return output.pop();
        }
    }

    protected static class ArrayStack {
        private double[] data;
        private int idx;

        ArrayStack() {
            this(5);
        }

        ArrayStack(int initialCapacity) {
            if (initialCapacity <= 0) {
                throw new IllegalArgumentException("Stack's capacity must be positive");
            } else {
                this.data = new double[initialCapacity];
                this.idx = -1;
            }
        }

        void push(double value) {
            if (this.idx + 1 == this.data.length) {
                double[] temp = new double[(int) (this.data.length * 1.2) + 1];
                System.arraycopy(this.data, 0, temp, 0, this.data.length);
                this.data = temp;
            }

            this.data[++this.idx] = value;
        }

        double peek() {
            if (this.idx == -1) {
                throw new EmptyStackException();
            } else {
                return this.data[this.idx];
            }
        }

        double pop() {
            if (this.idx == -1) {
                throw new EmptyStackException();
            } else {
                return this.data[this.idx--];
            }
        }

        boolean isEmpty() {
            return this.idx == -1;
        }

        int size() {
            return this.idx + 1;
        }
    }

}

