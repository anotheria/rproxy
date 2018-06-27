package net.anotheria.rproxy.refactor.cache;

import java.util.LinkedHashMap;

/**
 * Cache Strategy interface.
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
     * Get LinkedHashMap of Key, Value pairs stored in cache.
     * @return LinkedHashMap<K, V> pairs
     */
    LinkedHashMap<K, V> getAllElements();

    /**
     * Print element sequence to console.
     */
    void printElements();

}