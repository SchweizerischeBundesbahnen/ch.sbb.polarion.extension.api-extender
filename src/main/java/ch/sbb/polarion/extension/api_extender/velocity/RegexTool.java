package ch.sbb.polarion.extension.api_extender.velocity;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
public class RegexTool {

    /**
     * Finds and extracts regex matches in the given text, returning the matches and their respective groups.
     *
     * @param regex regular expression pattern to search for matches in the text
     * @param text  input text
     * @return a map where the key is the match index and the value is an array of strings representing the groups extracted from the respective match; an empty map is returned if no matches are found
     */
    @NotNull
    public Map<Integer, String[]> findMatches(@NotNull String regex, @NotNull String text) {
        Map<Integer, String[]> matches = new HashMap<>();
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(text);
        int matchIndex = 0;
        while (matcher.find()) {
            int groupCount = matcher.groupCount();
            String[] groups = new String[groupCount];
            for (int i = 0; i < groupCount; i++) {
                groups[i] = matcher.group(i);
            }
            matches.put(matchIndex, groups);
            matchIndex++;
        }

        return matches;
    }

}
