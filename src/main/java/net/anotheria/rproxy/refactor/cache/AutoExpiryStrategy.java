package net.anotheria.rproxy.refactor.cache;

import net.anotheria.rproxy.refactor.conf.AutoExpiryConfig;

import java.util.LinkedHashMap;

/**
 * TODO comment, implement
 *
 * @param <K>
 * @param <V>
 */
public class AutoExpiryStrategy<K, V> implements ICacheStrategy<K, V> {

    public AutoExpiryStrategy(AutoExpiryConfig c) {
    }

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
