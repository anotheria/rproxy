package net.anotheria.rproxy.refactor.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Autoexpiry in memory implementation
 *
 * @param <K> key
 * @param <V> value
 */
public class AutoExpiryStrategy<K, V> implements ICacheStrategy<K, V>, Runnable {

    /**
     * Default value lifetime in seconds
     */
    public static final Long DEFAULT_INTERVAL_SECONDS = 20L;

    /**
     * Interval in seconds to check Values for expiration.
     */
    private Long intervalSeconds;
    /**
     * Cache map. K - key of object, V - object to store in cache.
     */
    private Map<K, V> cache;
    /**
     * Map contains Keys and creation timestamps of objects in cache.
     */
    private Map<K, Long> keyTimeMap;

    /**
     * Initializes autoexpiring in memory strategy instance.
     * <p>Value lifetime will be set to default value.</p>
     */
    public AutoExpiryStrategy() {
        intervalSeconds = DEFAULT_INTERVAL_SECONDS;
        cache = new ConcurrentHashMap<>();
        keyTimeMap = new ConcurrentHashMap<>();
        new Thread(this).start();
    }

    /**
     * Initializes autoexpiring in memory strategy instance.
     *
     * @param seconds value lifetime in seconds.
     */
    public AutoExpiryStrategy(Long seconds) {
        intervalSeconds = seconds;
        cache = new ConcurrentHashMap<>();
        keyTimeMap = new ConcurrentHashMap<>();
        new Thread(this).start();
    }

    @Override
    public void add(K key, V value) {
        cache.put(key, value);
        /**
         * add to map K and CreateTimeStamp
         */
        keyTimeMap.put(key, System.currentTimeMillis());
    }

    @Override
    public V get(K key) {
        if (keyTimeMap.containsKey(key)) {
            updateTimeFor(key);
            return cache.get(key);
        }

        return null;
    }

    @Override
    public void remove(K key) {
        keyTimeMap.remove(key);
        cache.remove(key);
    }

    @Override
    public LinkedHashMap<K, V> getAllElements() {
        return null;
    }

    @Override
    public void printElements() {
        for (K key : cache.keySet())
            System.out.println("K: " + key + " expires in " + (intervalSeconds - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - keyTimeMap.get(key))));
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(intervalSeconds * 1000);
                removeOld();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove expired values from cache.
     */
    private void removeOld() {
        for (K key : keyTimeMap.keySet()) {
            if (expired(key)) {
                //System.out.println("Removed " + key + "; " + cache.get(key));
                keyTimeMap.remove(key);
                cache.remove(key);
            }
        }
    }

    /**
     * Check if key expired.
     *
     * @param key
     * @return true if expired, false if not.
     */
    private boolean expired(K key) {
        Long currentTimeStamp = System.currentTimeMillis();
        if (TimeUnit.MILLISECONDS.toSeconds(currentTimeStamp - keyTimeMap.get(key)) >= intervalSeconds) {
            return true;
        }
        return false;
    }

    /**
     * Update create timestamp for value.
     *
     * @param key of value to update create time.
     */
    private void updateTimeFor(K key) {
        keyTimeMap.put(key, System.currentTimeMillis());
    }
}
