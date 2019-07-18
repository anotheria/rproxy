package net.anotheria.rproxy.cache;

/**
 * @author
 */
public interface CacheLoader<K, V> {

    V load(K key) throws Exception;
}
