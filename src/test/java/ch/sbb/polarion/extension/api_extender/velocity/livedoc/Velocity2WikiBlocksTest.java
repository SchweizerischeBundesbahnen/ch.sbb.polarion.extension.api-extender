package ch.sbb.polarion.extension.api_extender.velocity.livedoc;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Velocity2WikiBlocksTest {

    private static final String DELIMITER = "\n<hr id=\"polarion_wiki_block_delimiter\">\n";

    @Test
    void detectsMarker() {
        assertTrue(Velocity2WikiBlocks.containsMarker("a {velocity2}#set($x = 1){velocity2}"));
        assertFalse(Velocity2WikiBlocks.containsMarker("#set($x = 1)"));
    }

    @Test
    void replacesRegionsWithTokens() {
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract("before {velocity2}$a{velocity2} middle {velocity2}\n$b\n{velocity2} after");

        assertEquals(List.of("$a", "\n$b\n"), blocks.getScripts());
        assertFalse(blocks.getSource().contains("{velocity2}"));
        assertFalse(blocks.getSource().contains("$"));
        assertTrue(blocks.getSource().startsWith("before velocitytwo"), blocks.getSource());
        assertTrue(blocks.getSource().endsWith(" after"), blocks.getSource());
    }

    @Test
    void restoresOutputInPlaceOfTokens() {
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract("<p>{velocity2}$a{velocity2}</p><p>{velocity2}$b{velocity2}</p>");
        String rendered = "<div>" + blocks.getSource() + "</div>";

        assertEquals("<div><p>A</p><p>B</p></div>", blocks.restore(rendered, List.of("A", "B")));
    }

    @Test
    void tokensWithTenOrMoreRegionsDoNotOverlap() {
        StringBuilder source = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            source.append("{velocity2}").append(i).append("{velocity2};");
        }
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract(source.toString());

        assertEquals("0;1;2;3;4;5;6;7;8;9;10;11;", blocks.restore(blocks.getSource(), blocks.getScripts()));
    }

    @Test
    void regionDoesNotCrossBlockDelimiter() {
        String source = "{velocity2}unclosed" + DELIMITER + "{velocity2}$b{velocity2}";
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract(source);

        assertEquals(List.of("$b"), blocks.getScripts());
        assertTrue(blocks.getSource().startsWith("{velocity2}unclosed" + DELIMITER), blocks.getSource());
    }

    @Test
    void tokensDifferBetweenRenderings() {
        String source = "{velocity2}$a{velocity2}";
        assertNotEquals(Velocity2WikiBlocks.extract(source).getSource(), Velocity2WikiBlocks.extract(source).getSource());
    }

    @Test
    void tokenIsPlainLowercaseText() {
        String token = Velocity2WikiBlocks.extract("{velocity2}$a{velocity2}").getSource();
        assertTrue(token.matches("[a-z0-9]+"), token);
    }
}
