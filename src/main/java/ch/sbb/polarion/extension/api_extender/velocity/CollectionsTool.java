package ch.sbb.polarion.extension.api_extender.velocity;

import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.model.IPObjectList;
import com.polarion.platform.persistence.spi.PObjectList;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
     * Puts the Map in order of values in a SortedMap
     *
     * @param shuffled  The Map to sort
     * @param ascending Whether to sort the values in ascending order
     * @return A SortedMap of the same type as the provided map
     */
    public <T> SortedMap<T, Integer> sortMap(Map<T, Integer> shuffled, boolean ascending) {
        Comparator<T> comparator = Comparator.comparing(shuffled::get);
        Comparator<T> treeComparator = ascending ? comparator : comparator.reversed();
        SortedMap<T, Integer> sortedMap = new TreeMap<>(treeComparator);

        sortedMap.putAll(shuffled);
        return sortedMap;
    }

}
