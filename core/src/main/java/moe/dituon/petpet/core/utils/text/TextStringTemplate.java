package moe.dituon.petpet.core.utils.text;

import lombok.Getter;

import java.util.*;

// https://github.com/fizyr/subst
public class TextStringTemplate {
    protected final List<Part> parts;
    @Getter
    protected final String source;

    public TextStringTemplate(List<Part> parts, String source) {
        this.parts = parts;
        this.source = source;
    }

    public static TextStringTemplate parse(String source) throws ParseException {
        List<Part> parts = new ArrayList<>();
        int index = 0;

        while (index < source.length()) {
            int nextSpecial = findNextSpecialChar(source, index);
            if (nextSpecial == -1) {
                parts.add(new Literal(source, index, source.length()));
                break;
            }

            if (nextSpecial > index) {
                parts.add(new Literal(source, index, nextSpecial));
            }

            char specialChar = source.charAt(nextSpecial);
            if (specialChar == '\\') {
                if (nextSpecial + 1 >= source.length()) {
                    throw new ParseException("Invalid escape sequence at end of input.");
                }
                parts.add(new EscapedByte(source.charAt(nextSpecial + 1)));
                index = nextSpecial + 2;
            } else if (specialChar == '$') {
                if (nextSpecial + 1 < source.length() && source.charAt(nextSpecial + 1) == '{') {
                    Variable variable = Variable.parse(source, nextSpecial);
                    parts.add(variable);
                    index = variable.getEndIndex();
                } else {
                    Variable variable = Variable.parseShortFormat(source, nextSpecial);
                    parts.add(variable);
                    index = variable.getEndIndex();
                }
            } else {
                throw new ParseException("Unexpected character: " + specialChar);
            }
        }

        return new TextStringTemplate(parts, source);
    }

    public String expand(Map<String, String> variables) throws ExpandException {
        StringBuilder output = new StringBuilder();

        for (Part part : parts) {
            part.expand(output, variables);
        }

        return output.toString();
    }

    public Set<String> getVariables() {
        Set<String> variables = new HashSet<>();
        for (Part part : parts) {
            if (part instanceof Variable) {
                variables.add(((Variable) part).name);
                // Recursively add variables from default value templates
                if (((Variable) part).defaultValue != null) {
                    try {
                        TextStringTemplate nested = TextStringTemplate.parse(((Variable) part).defaultValue);
                        variables.addAll(nested.getVariables());
                    } catch (ParseException ignored) {
                        // Ignore errors in parsing nested templates
                    }
                }
            }
        }
        return variables;
    }

    public Set<String> getVariablesWithoutDefaults() {
        Set<String> variables = new HashSet<>();
        for (Part part : parts) {
            if (part instanceof Variable) {
                Variable variable = (Variable) part;
                if (variable.defaultValue == null) {
                    variables.add(variable.name);
                } else {
                    // Check for variables in the nested default template
                    try {
                        TextStringTemplate nested = TextStringTemplate.parse(variable.defaultValue);
                        variables.addAll(nested.getVariablesWithoutDefaults());
                    } catch (ParseException ignored) {
                        // Ignore errors in parsing nested templates
                    }
                }
            }
        }
        return variables;
    }

    private static int findNextSpecialChar(String source, int start) {
        for (int i = start; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '$' || c == '\\') {
                return i;
            }
        }
        return -1;
    }

    public interface Part {
        void expand(StringBuilder output, Map<String, String> variables) throws ExpandException;
    }

    public static class Literal implements Part {
        private final String source;
        private final int start;
        private final int end;

        public Literal(String source, int start, int end) {
            this.source = source;
            this.start = start;
            this.end = end;
        }

        @Override
        public void expand(StringBuilder output, Map<String, String> variables) {
            output.append(source, start, end);
        }
    }

    public static class EscapedByte implements Part {
        private final char value;

        public EscapedByte(char value) {
            this.value = value;
        }

        @Override
        public void expand(StringBuilder output, Map<String, String> variables) {
            output.append(value);
        }
    }

    public static class Variable implements Part {
        private final String name;
        private final String defaultValue;
        @Getter
        private final int endIndex;

        public Variable(String name, String defaultValue, int endIndex) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.endIndex = endIndex;
        }

        public static Variable parse(String source, int start) throws ParseException {
            int endBrace = findClosingBrace(source, start + 2);
            if (endBrace == -1) {
                throw new ParseException("Missing closing brace for variable at position: " + start);
            }

            String content = source.substring(start + 2, endBrace);
            String[] parts = splitVariableContent(content);
            String name = parts[0];
            String defaultValue = parts[1];

            return new Variable(name, defaultValue, endBrace + 1);
        }

        public static Variable parseShortFormat(String source, int start) throws ParseException {
            int end = start + 1;
            while (end < source.length() && (Character.isLetterOrDigit(source.charAt(end)) || source.charAt(end) == '_')) {
                end++;
            }

            if (end == start + 1) {
                throw new ParseException("Invalid short variable format at position: " + start);
            }

            String name = source.substring(start + 1, end);
            return new Variable(name, null, end);
        }

        private static String[] splitVariableContent(String content) {
            int colonIndex = content.indexOf(':');
            if (colonIndex == -1) {
                return new String[]{content, null};
            } else {
                String name = content.substring(0, colonIndex);
                String defaultValue = content.substring(colonIndex + 1);

                // Support for different syntaxes like :-, :=, :+.
                switch (defaultValue.charAt(0)) {
                    case '-':
                    case '=':
                    case '+':
                        defaultValue = defaultValue.substring(1);
                        break;
                    default:
                        break;
                }

                return new String[]{name, defaultValue};
            }
        }

        private static int findClosingBrace(String source, int start) {
            int nesting = 0;
            for (int i = start; i < source.length(); i++) {
                char c = source.charAt(i);
                if (c == '{') {
                    nesting++;
                } else if (c == '}') {
                    if (nesting == 0) {
                        return i;
                    }
                    nesting--;
                }
            }
            return -1;
        }

        @Override
        public void expand(StringBuilder output, Map<String, String> variables) throws ExpandException {
            String value = variables.get(name);
            if (value == null && defaultValue != null) {
                TextStringTemplate nestedTemplate;
                try {
                    nestedTemplate = TextStringTemplate.parse(defaultValue);
                    value = nestedTemplate.expand(variables);
                } catch (ParseException e) {
                    throw new ExpandException("Error parsing default value for variable: " + name);
                }
            }

            if (value == null) {
                throw new ExpandException("Variable not found: " + name);
            }
            output.append(value);
        }
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }

    public static class ExpandException extends RuntimeException {
        public ExpandException(String message) {
            super(message);
        }
    }
}
