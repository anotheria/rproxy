package net.anotheria.rproxy.refactor.cache.autoexpiry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Base class for AutoExpiry strategies.
 * @param <K>
 * @param <V>
 */
public abstract class BaseAutoExpiry<K, V> implements Runnable {

    public static final Long DEFAULT_SCAN_INTERVAL_SECONDS = 30L;
    /**
     * 1h
     */
    public static final Long DEFAULT_TIME_TO_LIVE_SECONDS = 3600L;

    private Map<K, Long> expiryMap;
    private Map<K, V> cache;
    private Long intervalSeconds;
    private Long timeToLiveSeconds;

    /**
     *
     * @param intervalSeconds scan interval for expired objects
     * @param timeToLiveSeconds for objects
     */
    protected BaseAutoExpiry(Long intervalSeconds, Long timeToLiveSeconds) {
        this.intervalSeconds = intervalSeconds;
        this.timeToLiveSeconds = timeToLiveSeconds;
        expiryMap = new ConcurrentHashMap<>();
        cache = new ConcurrentHashMap<>();
        new Thread(this).start();
    }

    /**
     * Default configuration
     */
    protected BaseAutoExpiry() {
        this.intervalSeconds = DEFAULT_SCAN_INTERVAL_SECONDS;
        this.timeToLiveSeconds = DEFAULT_TIME_TO_LIVE_SECONDS;
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

    public Long getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Long intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public Long getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(Long timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    /**
     * Search for expired values
     */
    protected void removeExpiredValues() {
        System.out.println("Scan for expired... ");
        for (K key : expiryMap.keySet()) {
            if (isExpired(key)) {
                Long cur = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                System.out.println(key + " expired! lived for " + (cur - expiryMap.get(key)));
                remove(key);
            }
        }
    }

    /**
     * Add new value to cache
     * @param key
     * @param value
     */
    protected void add(K key, V value) {
        Long currentTimeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        cache.put(key, value);
        expiryMap.put(key, currentTimeStamp);
    }

    /**
     * Remove value from cache
     * @param key
     */
    protected void remove(K key) {
        cache.remove(key);
        expiryMap.remove(key);
    }

    /**
     *
     * @param key
     * @return value if present, otherwise null
     */
    protected V get(K key) {
        V value = null;
        if (expiryMap.containsKey(key)) {
            value = cache.get(key);
            updateCreationTimestamp(key);
        }

        return value;
    }

    /**
     * Updates creation time for object in cache.
     * @param key
     */
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
