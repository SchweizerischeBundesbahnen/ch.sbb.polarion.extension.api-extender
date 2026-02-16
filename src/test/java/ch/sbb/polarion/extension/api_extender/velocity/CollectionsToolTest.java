package ch.sbb.polarion.extension.api_extender.velocity;

import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.model.IPObjectList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, Integer> sorted = collectionsTool.sortMap(unsorted, true);

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

        Map<String, Integer> sorted = collectionsTool.sortMap(unsorted, false);

        assertEquals(3, sorted.size());
        List<String> keys = new ArrayList<>(sorted.keySet());
        assertEquals("apple", keys.get(0));
        assertEquals("cherry", keys.get(1));
        assertEquals("banana", keys.get(2));
    }

    @Test
    void sortMap_shouldHandleEmptyMap() {
        Map<String, Integer> empty = new HashMap<>();

        Map<String, Integer> sorted = collectionsTool.sortMap(empty, true);

        assertTrue(sorted.isEmpty());
    }

    @Test
    void sortMap_shouldHandleSingleEntry() {
        Map<String, Integer> single = new HashMap<>();
        single.put("only", 42);

        Map<String, Integer> sorted = collectionsTool.sortMap(single, true);

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

        Map<String, Integer> sorted = collectionsTool.sortMap(unsorted, true);

        assertEquals(3, sorted.size());
        assertTrue(sorted.containsKey("first"));
        assertTrue(sorted.containsKey("second"));
        assertTrue(sorted.containsKey("third"));
    }

    @Test
    void sortMap_shouldHandleNegativeValues() {
        Map<String, Integer> unsorted = new HashMap<>();
        unsorted.put("negative", -5);
        unsorted.put("zero", 0);
        unsorted.put("positive", 5);

        Map<String, Integer> sortedAsc = collectionsTool.sortMap(unsorted, true);
        List<String> keysAsc = new ArrayList<>(sortedAsc.keySet());
        assertEquals("negative", keysAsc.get(0));
        assertEquals("zero", keysAsc.get(1));
        assertEquals("positive", keysAsc.get(2));

        Map<String, Integer> sortedDesc = collectionsTool.sortMap(unsorted, false);
        List<String> keysDesc = new ArrayList<>(sortedDesc.keySet());
        assertEquals("positive", keysDesc.get(0));
        assertEquals("zero", keysDesc.get(1));
        assertEquals("negative", keysDesc.get(2));
    }

    @Test
    void sortMap_shouldNotRemoveDuplicateValues() {
        Map<String, Integer> unsorted = new HashMap<>();
        unsorted.put("E1", 1);
        unsorted.put("E2", 0);
        unsorted.put("E3", 2);
        unsorted.put("E4", 0);

        Map<String, Integer> sortedAsc = collectionsTool.sortMap(unsorted, true);
        assertEquals(4, sortedAsc.size());
        List<String> keysAsc = new ArrayList<>(sortedAsc.keySet());
        assertEquals(0, sortedAsc.get(keysAsc.get(0)));
        assertEquals(0, sortedAsc.get(keysAsc.get(1)));
        assertEquals(1, sortedAsc.get(keysAsc.get(2)));
        assertEquals(2, sortedAsc.get(keysAsc.get(3)));

        Map<String, Integer> sortedDesc = collectionsTool.sortMap(unsorted, false);
        assertEquals(4, sortedDesc.size());
        List<String> keysDesc = new ArrayList<>(sortedDesc.keySet());
        assertEquals(2, sortedDesc.get(keysDesc.get(0)));
        assertEquals(1, sortedDesc.get(keysDesc.get(1)));
        assertEquals(0, sortedDesc.get(keysDesc.get(2)));
        assertEquals(0, sortedDesc.get(keysDesc.get(3)));
    }

    @Test
    void sortMap_shouldWorkWithIntegerKeys() {
        Map<Integer, Integer> unsorted = new HashMap<>();
        unsorted.put(1, 100);
        unsorted.put(2, 50);
        unsorted.put(3, 75);

        Map<Integer, Integer> sorted = collectionsTool.sortMap(unsorted, true);

        assertEquals(3, sorted.size());
        List<Integer> keys = new ArrayList<>(sorted.keySet());
        assertEquals(2, keys.get(0)); // value 50
        assertEquals(3, keys.get(1)); // value 75
        assertEquals(1, keys.get(2)); // value 100
    }
}
