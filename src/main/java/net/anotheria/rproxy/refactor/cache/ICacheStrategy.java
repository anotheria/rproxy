package net.anotheria.rproxy.refactor.cache;

import java.util.LinkedHashMap;

/**
 * Cache strategy interface.
 * @param <K> type of Key
 * @param <V> type of Value
 */
public interface ICacheStrategy<K, V> {

    /**
     * Add element to cache as Key, Value pair.
     * @param key
     * @param value
     */
    void add(K key, V value);

    /**
     * Get element from cache with Key.
     * @param key
     * @return element if present, otherwise null.
     */
    V get(K key);

    /**
     * Remove element from cache with Key.
     * @param key
     */
    void remove(K key);

    /**
     * TODO remove method after testing
     * Get LinkedHashMap of Key, Value pairs stored in cache.
     * @return LinkedHashMap&lt;K, V&gt; pairs
     */
    LinkedHashMap<K, V> getAllElements();

    /**
     * TODO remove method after testing
     * Print element sequence to console.
     */
    void printElements();

}
