package net.anotheria.rproxy.refactor.config;

import net.anotheria.rproxy.refactor.cache.AutoExpiryStrategyImpl;
import net.anotheria.rproxy.refactor.cache.LRUStrategyImpl;
import net.anotheria.rproxy.refactor.cache.PermanentStrategyImpl;

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
     * @return LRUStrategyImpl instance
     */
    public LRUStrategyImpl<K, V> configureLRU(StrategyConfig config) {
        LRUConfigImpl c = (LRUConfigImpl) config;
        if (c != null) {
            return new LRUStrategyImpl<>(c.getSize());
        } else {
            return new LRUStrategyImpl<>();
        }
    }

    public PermanentStrategyImpl<K, V> configurePermanent(StrategyConfig config) {
        PermanentConfigImpl c = (PermanentConfigImpl) config;
        return new PermanentStrategyImpl<>(c);
    }

    public PermanentStrategyImpl<K, V> configurePermanent(String path) {
        PermanentStrategyImpl<K, V> cache = new PermanentStrategyImpl<>(path);
        cache.fillAfterRestart(path);
        return cache;
    }

    public AutoExpiryStrategyImpl<K, V> configureAutoExpiry(StrategyConfig config) {
        AutoExpiryConfigImpl c = (AutoExpiryConfigImpl) config;
        if (c == null) {
            return new AutoExpiryStrategyImpl<>();
        } else {
            return new AutoExpiryStrategyImpl<>(c.getScanInterval());
        }
    }

}
