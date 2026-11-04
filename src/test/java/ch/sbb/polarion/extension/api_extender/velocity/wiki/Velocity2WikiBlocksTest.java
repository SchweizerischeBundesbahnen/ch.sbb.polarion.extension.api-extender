package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Velocity2WikiBlocksTest {

    private static final String DELIMITER = "\n<hr id=\"polarion_wiki_block_delimiter\">\n";

    @Test
    void detectsMarker() {
        assertTrue(Velocity2WikiBlocks.containsMarker("a {velocityV2}#set($x = 1){velocityV2}"));
        assertFalse(Velocity2WikiBlocks.containsMarker("#set($x = 1)"));
    }

    @Test
    void replacesRegionsWithTokens() {
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract("before {velocityV2}$a{velocityV2} middle {velocityV2}\n$b\n{velocityV2} after");

        assertEquals(List.of("$a", "\n$b\n"), blocks.getScripts());
        assertFalse(blocks.getSource().contains("{velocityV2}"));
        assertFalse(blocks.getSource().contains("$"));
        assertTrue(blocks.getSource().startsWith("before velocitytwo"), blocks.getSource());
        assertTrue(blocks.getSource().endsWith(" after"), blocks.getSource());
    }

    @Test
    void restoresOutputInPlaceOfTokens() {
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract("<p>{velocityV2}$a{velocityV2}</p><p>{velocityV2}$b{velocityV2}</p>");
        String rendered = "<div>" + blocks.getSource() + "</div>";

        assertEquals("<div><p>A</p><p>B</p></div>", blocks.restore(rendered, List.of("A", "B")));
    }

    @Test
    void tokensWithTenOrMoreRegionsDoNotOverlap() {
        StringBuilder source = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            source.append("{velocityV2}").append(i).append("{velocityV2};");
        }
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract(source.toString());

        assertEquals("0;1;2;3;4;5;6;7;8;9;10;11;", blocks.restore(blocks.getSource(), blocks.getScripts()));
    }

    @Test
    void regionDoesNotCrossBlockDelimiter() {
        String source = "{velocityV2}unclosed" + DELIMITER + "{velocityV2}$b{velocityV2}";
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract(source);

        assertEquals(List.of("$b"), blocks.getScripts());
        assertTrue(blocks.getSource().startsWith("{velocityV2}unclosed" + DELIMITER), blocks.getSource());
    }

    @Test
    void tokensDifferBetweenRenderings() {
        String source = "{velocityV2}$a{velocityV2}";
        assertNotEquals(Velocity2WikiBlocks.extract(source).getSource(), Velocity2WikiBlocks.extract(source).getSource());
    }

    @Test
    void tokenIsPlainLowercaseText() {
        String token = Velocity2WikiBlocks.extract("{velocityV2}$a{velocityV2}").getSource();
        assertTrue(token.matches("[a-z0-9]+"), token);
    }
}
