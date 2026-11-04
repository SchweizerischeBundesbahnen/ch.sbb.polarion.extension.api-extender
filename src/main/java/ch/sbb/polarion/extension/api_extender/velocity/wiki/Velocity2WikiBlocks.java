package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds the {@code {velocityV2}...{velocityV2}} regions in a wiki source and replaces each with a token.
 * <p>
 * Velocity 1.4 and the wiki markup renderer leave the tokens unchanged, so {@link #restore(String, List)}
 * can put the Velocity 2 output in their place after Polarion renders the rest of the source.
 */
public final class Velocity2WikiBlocks {

    public static final String MARKER = "{velocityV2}";

    // Polarion joins all Wiki Content blocks of a document with this delimiter and splits the rendered result by it.
    // A region must not cross it, or the rendered blocks get mixed up.
    private static final String BLOCK_DELIMITER = "<hr id=\"polarion_wiki_block_delimiter\">";
    private static final Pattern REGION = Pattern.compile(
            Pattern.quote(MARKER) + "((?:(?!" + Pattern.quote(MARKER) + "|" + Pattern.quote(BLOCK_DELIMITER) + ").)*?)" + Pattern.quote(MARKER),
            Pattern.DOTALL);

    private final String source;
    private final List<String> scripts;
    private final List<String> tokens;

    private Velocity2WikiBlocks(@NotNull String source, @NotNull List<String> scripts, @NotNull List<String> tokens) {
        this.source = source;
        this.scripts = scripts;
        this.tokens = tokens;
    }

    public static boolean containsMarker(@NotNull String wikiSource) {
        return wikiSource.contains(MARKER);
    }

    public static @NotNull Velocity2WikiBlocks extract(@NotNull String wikiSource) {
        // A random prefix per rendering stops a script from writing a token which belongs to another region.
        String prefix = "velocitytwo" + UUID.randomUUID().toString().replace("-", "") + "n";
        List<String> scripts = new ArrayList<>();
        List<String> tokens = new ArrayList<>();
        Matcher matcher = REGION.matcher(wikiSource);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String token = prefix + tokens.size() + "x";
            scripts.add(matcher.group(1));
            tokens.add(token);
            matcher.appendReplacement(result, token);
        }
        matcher.appendTail(result);
        return new Velocity2WikiBlocks(result.toString(), Collections.unmodifiableList(scripts), Collections.unmodifiableList(tokens));
    }

    /**
     * @return the wiki source with tokens in place of the Velocity 2 regions
     */
    public @NotNull String getSource() {
        return source;
    }

    public @NotNull List<String> getScripts() {
        return scripts;
    }

    /**
     * @return the token of each region, in the order of {@link #getScripts()}
     */
    public @NotNull List<String> getTokens() {
        return tokens;
    }

    /**
     * @param rendered the HTML which Polarion rendered from {@link #getSource()}
     * @param htmls    the output of each script, in the order of {@link #getScripts()}
     */
    public @NotNull String restore(@NotNull String rendered, @NotNull List<String> htmls) {
        String result = rendered;
        for (int i = 0; i < tokens.size(); i++) {
            result = result.replace(tokens.get(i), htmls.get(i));
        }
        return result;
    }
}
