package ch.sbb.polarion.extension.api_extender.velocity;

import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.model.IPObjectList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CollectionsToolTest {

    private final CollectionsTool collectionsTool = new CollectionsTool();

    @Test
    void convertArrayListToWeakPObjectList_testReturnsData() {
        IWorkItem workItem1 = mock(IWorkItem.class);
        IWorkItem workItem2 = mock(IWorkItem.class);

        IPObjectList<IPObject> objectList = collectionsTool.convertArrayListToWeakPObjectList(List.of(workItem1, workItem2));
        assertEquals(workItem1, objectList.get(0));
        assertEquals(workItem2, objectList.get(1));
    }

    @Test
    void sortMap_shouldSortInAscendingOrder() {
        Map<String, Integer> unsorted = new HashMap<>();
        unsorted.put("apple", 3);
        unsorted.put("banana", 1);
        unsorted.put("cherry", 2);

        SortedMap<String, Integer> sorted = collectionsTool.sortMap(unsorted, true);

        assertEquals(3, sorted.size());
        List<String> keys = new ArrayList<>(sorted.keySet());
        assertEquals("banana", keys.get(0));
        assertEquals("cherry", keys.get(1));
        assertEquals("apple", keys.get(2));
    }

    @Test
    void sortMap_shouldSortInDescendingOrder() {
        Map<String, Integer> unsorted = new HashMap<>();
        unsorted.put("apple", 3);
        unsorted.put("banana", 1);
        unsorted.put("cherry", 2);

        SortedMap<String, Integer> sorted = collectionsTool.sortMap(unsorted, false);

        assertEquals(3, sorted.size());
        List<String> keys = new ArrayList<>(sorted.keySet());
        assertEquals("apple", keys.get(0));
        assertEquals("cherry", keys.get(1));
        assertEquals("banana", keys.get(2));
    }

    @Test
    void sortMap_shouldHandleEmptyMap() {
        Map<String, Integer> empty = new HashMap<>();

        SortedMap<String, Integer> sorted = collectionsTool.sortMap(empty, true);

        assertTrue(sorted.isEmpty());
    }

    @Test
    void sortMap_shouldHandleSingleEntry() {
        Map<String, Integer> single = new HashMap<>();
        single.put("only", 42);

        SortedMap<String, Integer> sorted = collectionsTool.sortMap(single, true);

        assertEquals(1, sorted.size());
        assertTrue(sorted.containsKey("only"));
        assertEquals(42, sorted.get("only"));
    }

    @Test
    void sortMap_shouldHandleEqualValues() {
        Map<String, Integer> unsorted = new HashMap<>();
        unsorted.put("first", 5);
        unsorted.put("second", 5);
        unsorted.put("third", 5);

        SortedMap<String, Integer> sorted = collectionsTool.sortMap(unsorted, true);

        // When all values are equal, TreeMap with value-based comparator treats keys as equivalent
        // and only retains one entry (last one put based on comparison order)
        assertEquals(1, sorted.size());
        // The retained entry should have value 5
        assertTrue(sorted.containsValue(5));
    }

    @Test
    void sortMap_shouldHandleNegativeValues() {
        Map<String, Integer> unsorted = new HashMap<>();
        unsorted.put("negative", -5);
        unsorted.put("zero", 0);
        unsorted.put("positive", 5);

        SortedMap<String, Integer> sortedAsc = collectionsTool.sortMap(unsorted, true);
        List<String> keysAsc = new ArrayList<>(sortedAsc.keySet());
        assertEquals("negative", keysAsc.get(0));
        assertEquals("zero", keysAsc.get(1));
        assertEquals("positive", keysAsc.get(2));

        SortedMap<String, Integer> sortedDesc = collectionsTool.sortMap(unsorted, false);
        List<String> keysDesc = new ArrayList<>(sortedDesc.keySet());
        assertEquals("positive", keysDesc.get(0));
        assertEquals("zero", keysDesc.get(1));
        assertEquals("negative", keysDesc.get(2));
    }

    @Test
    void sortMap_shouldWorkWithIntegerKeys() {
        Map<Integer, Integer> unsorted = new HashMap<>();
        unsorted.put(1, 100);
        unsorted.put(2, 50);
        unsorted.put(3, 75);

        SortedMap<Integer, Integer> sorted = collectionsTool.sortMap(unsorted, true);

        assertEquals(3, sorted.size());
        List<Integer> keys = new ArrayList<>(sorted.keySet());
        assertEquals(2, keys.get(0)); // value 50
        assertEquals(3, keys.get(1)); // value 75
        assertEquals(1, keys.get(2)); // value 100
    }
}
