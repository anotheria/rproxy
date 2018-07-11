package net.anotheria.rproxy.refactor.cache.autoexpiry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class BaseAutoExpiry<K, V> implements Runnable {

    private Map<K, Long> expiryMap;
    private Map<K, V> cache;
    private int intervalSeconds;
    private int timeToLiveSeconds;

    protected BaseAutoExpiry(int intervalSeconds, int timeToLiveSeconds) {
        this.intervalSeconds = intervalSeconds;
        this.timeToLiveSeconds = timeToLiveSeconds;
        expiryMap = new ConcurrentHashMap<>();
        cache = new ConcurrentHashMap<>();
        new Thread(this).start();
    }

    public Map<K, Long> getExpiryMap() {
        return expiryMap;
    }

    public void setExpiryMap(Map<K, Long> expiryMap) {
        this.expiryMap = expiryMap;
    }

    public Map<K, V> getCache() {
        return cache;
    }

    public void setCache(Map<K, V> cache) {
        this.cache = cache;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public int getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(int timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    protected void removeExpiredValues() {
        for (K key : expiryMap.keySet()) {
            if (isExpired(key)) {
                remove(key);
            }
        }
    }

    protected void add(K key, V value) {
        Long currentTimeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        cache.put(key, value);
        expiryMap.put(key, currentTimeStamp);
    }

    protected void remove(K key) {
        cache.remove(key);
        expiryMap.remove(key);
    }

    protected V get(K key) {
        V value = null;
        if (expiryMap.containsKey(key)) {
            value = cache.get(key);
            updateCreationTimestamp(key);
        }

        return value;
    }

    protected void updateCreationTimestamp(K key){
        Long currentTimeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        expiryMap.put(key, currentTimeStamp);
    }


    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(intervalSeconds * 1000);
                removeExpiredValues();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean isExpired(K key) {
        Long currentTimeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (currentTimeStamp - expiryMap.get(key) >= timeToLiveSeconds) {
            return true;
        }
        return false;
    }

    protected boolean isFileInCache(K key){
        return expiryMap.containsKey(key);
    }

}
