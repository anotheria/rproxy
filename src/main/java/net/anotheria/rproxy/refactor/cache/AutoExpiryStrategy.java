package net.anotheria.rproxy.refactor.cache;

import java.util.LinkedHashMap;

/**
 * TODO comment, implement
 * @param <K>
 * @param <V>
 */
public class AutoExpiryStrategy<K, V> implements ICacheStrategy<K, V> {
    @Override
    public void add(K key, V value) {

    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public void remove(K key) {

    }

    @Override
    public LinkedHashMap<K, V> getAllElements() {
        return null;
    }

    @Override
    public void printElements() {

    }
}
