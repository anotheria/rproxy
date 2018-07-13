package net.anotheria.rproxy.refactor.cache.autoexpiry;

import net.anotheria.rproxy.refactor.cache.ICacheStrategy;

import java.util.LinkedHashMap;

public class MemoryAutoExpiry<K, V> extends BaseAutoExpiry<K, V> implements ICacheStrategy<K, V> {

    public MemoryAutoExpiry(Long intervalSeconds, Long timeToLiveSeconds) {
        super(intervalSeconds, timeToLiveSeconds);
    }

    public MemoryAutoExpiry() {
        super();
    }


    @Override
    public void add(K key, V value) {
        super.add(key, value);
    }

    @Override
    public V get(K key) {
        return super.get(key);
    }

    @Override
    public void remove(K key) {
        super.remove(key);
    }

    @Override
    public LinkedHashMap<K, V> getAllElements() {
        return null;
    }

    @Override
    public void printElements() {
        for (K key : super.getCache().keySet()) {
            System.out.println(key + " " + super.getExpiryMap().get(key));
        }
    }

    @Override
    protected void removeExpiredValues() {
        super.removeExpiredValues();
        //System.out.println("Scan for expired...");
    }

}
