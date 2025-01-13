import moe.dituon.petpet.core.utils.text.TextStringTemplate;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TextStringTemplateTest {

    @Test
    void testSimpleVariableExpansion() {
        String templateString = "Hello ${name}!";
        TextStringTemplate template = TextStringTemplate.parse(templateString);

        Map<String, String> variables = new HashMap<>();
        variables.put("name", "Alice");

        String result = template.expand(variables);
        assertEquals("Hello Alice!", result);
    }

    @Test
    void testDefaultVariableExpansion() {
        String templateString = "Hello ${name:-User}!";
        TextStringTemplate template = TextStringTemplate.parse(templateString);

        Map<String, String> variables = new HashMap<>();

        String result = template.expand(variables);
        assertEquals("Hello User!", result);
    }

    @Test
    void testDefaultValueWithExistingVariable() {
        String templateString = "Hello ${name:-User}!";
        TextStringTemplate template = TextStringTemplate.parse(templateString);

        Map<String, String> variables = new HashMap<>();
        variables.put("name", "Alice");

        String result = template.expand(variables);
        assertEquals("Hello Alice!", result);
    }

    @Test
    void testNestedDefaultVariables() {
        String templateString = "Welcome to ${place:${country:-Earth}}!";
        TextStringTemplate template = TextStringTemplate.parse(templateString);

        Map<String, String> variables = new HashMap<>();
        variables.put("country", "Wonderland");

        String result = template.expand(variables);
        assertEquals("Welcome to Wonderland!", result);
    }

    @Test
    void testVariablesWithoutDefaults() {
        String templateString = "Hello ${name}! Welcome to ${place:${country:-Earth}}.";
        TextStringTemplate template = TextStringTemplate.parse(templateString);

        Set<String> variables = template.getVariablesWithoutDefaults();
        assertTrue(variables.contains("name"));
        assertFalse(variables.contains("place"));
        assertFalse(variables.contains("country"));
    }

    @Test
    void testAllVariables() {
        String templateString = "Hello ${name}! Welcome to ${place:${country:-Earth}}.";
        TextStringTemplate template = TextStringTemplate.parse(templateString);

        Set<String> variables = template.getVariables();
        assertTrue(variables.contains("name"));
        assertTrue(variables.contains("place"));
        assertTrue(variables.contains("country"));
    }

    @Test
    void testShortVariableSyntax() {
        String templateString = "Hello $name!";
        TextStringTemplate template = TextStringTemplate.parse(templateString);

        Map<String, String> variables = new HashMap<>();
        variables.put("name", "Alice");

        String result = template.expand(variables);
        assertEquals("Hello Alice!", result);
    }

    @Test
    void testEscapeCharacter() {
        String templateString = "Hello \\${name}!";
        TextStringTemplate template = TextStringTemplate.parse(templateString);

        Map<String, String> variables = new HashMap<>();
        variables.put("name", "Alice");

        String result = template.expand(variables);
        assertEquals("Hello ${name}!", result);
    }

    @Test
    void testErrorForMissingVariable() {
        String templateString = "Hello ${name}!";
        TextStringTemplate template;
        try {
            template = TextStringTemplate.parse(templateString);
            Map<String, String> variables = new HashMap<>();
            template.expand(variables);
            fail("Expected an ExpandException to be thrown");
        } catch (TextStringTemplate.ExpandException e) {
            assertTrue(e.getMessage().contains("Variable not found: name"));
        } catch (TextStringTemplate.ParseException e) {
            fail("Unexpected ParseException");
        }
    }

    @Test
    void testErrorForInvalidTemplate() {
        String templateString = "Hello ${name";
        assertThrows(TextStringTemplate.ParseException.class, () -> TextStringTemplate.parse(templateString));
    }

    @Test
    void testComplexTemplate() {
        String templateString = "Hello ${name}! Your role is ${role:-Guest}. Location: ${location:-${city:-Unknown City}}.";
        TextStringTemplate template = TextStringTemplate.parse(templateString);

        Map<String, String> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("city", "New York");

        String result = template.expand(variables);
        assertEquals("Hello Alice! Your role is Guest. Location: New York.", result);
    }
}
