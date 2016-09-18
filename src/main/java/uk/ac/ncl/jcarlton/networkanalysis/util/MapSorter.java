package uk.ac.ncl.jcarlton.networkanalysis.util;

import java.util.*;

/**
 * <h1>Map Sorter</h1>
 * A generic class that performs sorting operations on
 * maps.
 *
 * @author Jonathan Carlton
 */
public class MapSorter {

    /**
     * Generic method to sort a map by its values - ascending
     *
     * @param map to be sorted
     * @param <K> generic key type
     * @param <V> generic value type
     * @return sorted map, by its values in ascending order
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> valueAscending(Map<K, V> map) {
        if (map.isEmpty()) return map;

        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> m : list)
            result.put(m.getKey(), m.getValue());
        return result;
    }

    /**
     * Generic method to sort a map by its values - descending
     *
     * @param map to be sorted
     * @param <K> generic key type
     * @param <V> generic value type
     * @return sorted map, by its values in descending order
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> valueDescending(Map<K, V> map) {
        if (map.isEmpty()) return map;

        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> m : list)
            result.put(m.getKey(), m.getValue());
        return result;
    }

    /**
     * Generic method to sort a map by its keys - descending
     *
     * @param map to be sorted
     * @param <K> generic key type
     * @param <V> generic value type
     * @return sorted map, by its keys in descending order
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> keyDescending(Map<K, V> map) {
        if (map.isEmpty()) return map;

        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.toString()).compareTo(o2.toString());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> m : list)
            result.put(m.getKey(), m.getValue());
        return result;
    }

    /**
     * Generic method to sort a map by its keys - ascending
     *
     * @param map to be sorted
     * @param <K> generic key type
     * @param <V> generic value type
     * @return sorted map, by its keys in ascending order
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> keyAscending(Map<K, V> map) {
        if (map.isEmpty()) return map;
        return new TreeMap<K, V>(map);
    }
}