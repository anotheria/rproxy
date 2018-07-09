package net.anotheria.rproxy.refactor.config;

import net.anotheria.rproxy.refactor.cache.AutoExpiryStrategy;
import net.anotheria.rproxy.refactor.cache.LRUStrategy;
import net.anotheria.rproxy.refactor.cache.PermanentStrategy;

/**
 * Class for creation and configuration instances of Cache Strategies.
 *
 * @param <K> type of key
 * @param <V> type of value
 */
public class CacheConfigurer<K, V> {

    /**
     * Creates new instance of LRU strategy cache using configuration.
     *
     * @param config entity to configure LRU strategy cache.
     * @return LRUStrategy instance
     */
    public LRUStrategy<K, V> configureLRU(IConfig config) {
        LRUConfig c = (LRUConfig) config;
        if (c != null) {
            return new LRUStrategy<>(c.getSize());
        } else {
            return new LRUStrategy<>();
        }
    }

    public PermanentStrategy<K, V> configurePermanent(IConfig config) {
        PermanentConfig c = (PermanentConfig) config;
        return new PermanentStrategy<>(c);
    }

    public PermanentStrategy<K, V> configurePermanent(String path) {
        PermanentStrategy<K, V> cache = new PermanentStrategy<>(path);
        cache.fillAfterRestart(path);
        return cache;
    }

    public AutoExpiryStrategy<K, V> configureAutoExpiry(IConfig config) {
        AutoExpiryConfig c = (AutoExpiryConfig) config;
        if (c == null) {
            return new AutoExpiryStrategy<>();
        } else {
            return new AutoExpiryStrategy<>(c.getScanInterval());
        }
    }

}
