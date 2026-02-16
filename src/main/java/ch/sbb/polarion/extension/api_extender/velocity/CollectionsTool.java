package ch.sbb.polarion.extension.api_extender.velocity;

import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.model.IPObjectList;
import com.polarion.platform.persistence.spi.PObjectList;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class CollectionsTool {

    /**
     * Returns the IPObjectList of given list of objects
     *
     * @param items list of objects
     * @return IPObjectList (WeakPObjectList)
     */
    @SuppressWarnings({"unchecked"})
    @NotNull
    public IPObjectList<IPObject> convertArrayListToWeakPObjectList(@NotNull List<IPObject> items) {
        return new PObjectList(null, items);
    }

    /**
     * Returns a new Map sorted by values, preserving all entries including those with duplicate values.
     *
     * @param shuffled  The Map to sort
     * @param ascending Whether to sort the values in ascending order
     * @return A LinkedHashMap with entries ordered by value
     */
    public <T> Map<T, Integer> sortMap(Map<T, Integer> shuffled, boolean ascending) {
        Comparator<Map.Entry<T, Integer>> comparator = Map.Entry.comparingByValue();
        if (!ascending) {
            comparator = comparator.reversed();
        }

        return shuffled.entrySet().stream()
                .sorted(comparator)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

}
