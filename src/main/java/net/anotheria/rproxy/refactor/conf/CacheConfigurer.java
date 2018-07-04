package net.anotheria.rproxy.refactor.conf;

import net.anotheria.rproxy.refactor.cache.LRUStrategy;

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
        return new LRUStrategy<>(c.getSize());
    }

}
