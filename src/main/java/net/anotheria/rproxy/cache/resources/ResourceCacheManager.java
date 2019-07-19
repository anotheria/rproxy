package net.anotheria.rproxy.cache.resources;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.anotheria.anoprise.cache.Cache;
import net.anotheria.anoprise.cache.Caches;
import net.anotheria.moskito.core.predefined.CacheStats;
import net.anotheria.rproxy.cache.CacheLoader;
import net.anotheria.rproxy.cache.resources.bean.CacheableResource;
import net.anotheria.rproxy.cache.resources.bean.ResourceCacheKey;

/**
 * @author
 */
public class ResourceCacheManager {

    private Cache<ResourceCacheKey, CacheableResource> cache;
    private CacheLoader<ResourceCacheKey, CacheableResource> cacheLoader;

    private static final ResourceCacheManager INSTANCE = new ResourceCacheManager();

    private static final Logger LOG = LoggerFactory.getLogger(ResourceCacheManager.class);

    private ResourceCacheManager() {
        cache = Caches.createSoftReferenceExpiringCache("resource-cache", 100, 5000, (int) TimeUnit.SECONDS.toMillis(30));
        cacheLoader = new ResourceCacheLoader();
    }

    public void put(ResourceCacheKey key, CacheableResource value) {
        cache.put(key, value);
    }

    public CacheableResource get(ResourceCacheKey key) {
        CacheableResource ret = cache.get(key);
        if (ret != null) {
            return ret;
        }

        try {
            ret = cacheLoader.load(key);
            if (ret != null) {
                put(key, ret);
            }
        } catch (Exception e) {
            LOG.error("get({}) failed: {}", key, e.getMessage(), e);
        }

        return ret;
    }

    public CacheStats getStats() {
        return cache.getCacheStats();
    }

    public static ResourceCacheManager getInstance() {
        return INSTANCE;
    }

}
