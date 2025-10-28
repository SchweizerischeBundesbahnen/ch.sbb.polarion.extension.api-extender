package ch.sbb.polarion.extension.api_extender.velocity;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegexToolTest {

    private final RegexTool regexTool = new RegexTool();

    @Test
    void findMatches_shouldFindSingleMatchWithNoGroups() {
        Map<Integer, String[]> matches = regexTool.findMatches("\\d+", "test123");
        assertEquals(1, matches.size());
        assertTrue(matches.containsKey(0));
        assertEquals(0, matches.get(0).length); // No capturing groups
    }

    @Test
    void findMatches_shouldFindMultipleMatchesWithNoGroups() {
        Map<Integer, String[]> matches = regexTool.findMatches("\\d+", "test123and456");
        assertEquals(2, matches.size());
        assertTrue(matches.containsKey(0));
        assertTrue(matches.containsKey(1));
        assertEquals(0, matches.get(0).length);
        assertEquals(0, matches.get(1).length);
    }

    @Test
    void findMatches_shouldFindMatchesWithCapturingGroups() {
        // Pattern with one capturing group
        Map<Integer, String[]> matches = regexTool.findMatches("(\\d+)", "test123and456");
        assertEquals(2, matches.size());
        assertEquals(1, matches.get(0).length);
        assertEquals("123", matches.get(0)[0]);
        assertEquals(1, matches.get(1).length);
        assertEquals("456", matches.get(1)[0]);
    }

    @Test
    void findMatches_shouldFindMatchesWithMultipleCapturingGroups() {
        // Pattern with multiple capturing groups: email username and domain
        // Note: Implementation stores group(0) [full match], group(1), not group(1), group(2)
        Map<Integer, String[]> matches = regexTool.findMatches("([\\w.]+)@([\\w.]+)", "email@test.com and user@example.org");
        assertEquals(2, matches.size());

        // First match - groups[0] is full match, groups[1] is first capturing group
        assertEquals(2, matches.get(0).length);
        assertEquals("email@test.com", matches.get(0)[0]);
        assertEquals("email", matches.get(0)[1]);

        // Second match
        assertEquals(2, matches.get(1).length);
        assertEquals("user@example.org", matches.get(1)[0]);
        assertEquals("user", matches.get(1)[1]);
    }

    @Test
    void findMatches_shouldReturnEmptyMapWhenNoMatches() {
        Map<Integer, String[]> matches = regexTool.findMatches("\\d+", "test_string");
        assertTrue(matches.isEmpty());
    }

    @Test
    void findMatches_shouldHandleEmptyString() {
        Map<Integer, String[]> matches = regexTool.findMatches("\\d+", "");
        assertTrue(matches.isEmpty());
    }

    @Test
    void findMatches_shouldHandleComplexPatternWithGroups() {
        // Extract dates in format DD-MM-YYYY
        // Note: Implementation stores group(0), group(1), group(2), not group(1), group(2), group(3)
        Map<Integer, String[]> matches = regexTool.findMatches(
                "(\\d{2})-(\\d{2})-(\\d{4})",
                "Date1: 23-10-2025 and Date2: 01-12-2024"
        );

        assertEquals(2, matches.size());
        assertEquals(3, matches.get(0).length);
        // First date - groups[0] is full match, then capturing groups
        assertEquals("23-10-2025", matches.get(0)[0]);
        assertEquals("23", matches.get(0)[1]);
        assertEquals("10", matches.get(0)[2]);

        assertEquals(3, matches.get(1).length);
        // Second date
        assertEquals("01-12-2024", matches.get(1)[0]);
        assertEquals("01", matches.get(1)[1]);
        assertEquals("12", matches.get(1)[2]);
    }

    @Test
    void findMatches_shouldHandleNestedGroups() {
        // Pattern with nested groups
        // Note: Implementation stores group(0), group(1), group(2), group(3), not group(1), group(2), group(3), group(4)
        Map<Integer, String[]> matches = regexTool.findMatches(
                "((\\w+)@(\\w+)\\.(\\w+))",
                "Contact: user@example.com"
        );
        assertEquals(1, matches.size());
        assertEquals(4, matches.get(0).length);
        assertEquals("user@example.com", matches.get(0)[0]); // group(0) - full match
        assertEquals("user@example.com", matches.get(0)[1]); // group(1) - outer group
        assertEquals("user", matches.get(0)[2]); // group(2) - first inner group
        assertEquals("example", matches.get(0)[3]); // group(3) - second inner group
    }

    @Test
    void findMatches_shouldHandlePatternWithNoCapturingGroupsButMultipleMatches() {
        Map<Integer, String[]> matches = regexTool.findMatches("\\b\\w+\\b", "hello world test");
        assertEquals(3, matches.size());
        assertEquals(0, matches.get(0).length);
        assertEquals(0, matches.get(1).length);
        assertEquals(0, matches.get(2).length);
    }

    @Test
    void findMatches_shouldHandleSpecialCharactersInPattern() {
        // Note: Implementation stores group(0) [full match], group(1), not just group(1)
        Map<Integer, String[]> matches = regexTool.findMatches(
                "\\$([0-9]+\\.[0-9]{2})",
                "Price: $19.99 and $5.50"
        );
        assertEquals(2, matches.size());
        assertEquals(1, matches.get(0).length);
        assertEquals("$19.99", matches.get(0)[0]); // group(0) - full match including $
        assertEquals(1, matches.get(1).length);
        assertEquals("$5.50", matches.get(1)[0]); // group(0) - full match including $
    }

    @Test
    void findMatches_shouldHandleEmptyCapturingGroups() {
        // Pattern that might match empty groups
        Map<Integer, String[]> matches = regexTool.findMatches(
                "(\\d*)(\\w+)",
                "abc 123def"
        );
        assertEquals(2, matches.size());
        assertEquals(2, matches.get(0).length);
        assertEquals(2, matches.get(1).length);
    }

}
