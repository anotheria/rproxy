package net.anotheria.rproxy.cache.resources;

import java.util.concurrent.TimeUnit;

import net.anotheria.anoprise.cache.Cache;
import net.anotheria.anoprise.cache.Caches;
import net.anotheria.moskito.core.predefined.CacheStats;
import net.anotheria.rproxy.cache.resources.bean.CacheableResource;
import net.anotheria.rproxy.cache.resources.bean.ResourceCacheKey;

/**
 * @author
 */
public class ResourceCacheManager {

    private Cache<ResourceCacheKey, CacheableResource> cache;

    private static final ResourceCacheManager INSTANCE = new ResourceCacheManager();

    private ResourceCacheManager() {
        cache = Caches.createSoftReferenceExpiringCache("resource-cache", 100, 1000, (int) TimeUnit.SECONDS.toMillis(300));
    }

    public void put(ResourceCacheKey key, CacheableResource value) {
        cache.put(key, value);
    }

    public CacheableResource get(ResourceCacheKey key) {
        return cache.get(key);
    }

    public void remove(ResourceCacheKey key) {
        cache.remove(key);
    }

    public CacheStats getStats() {
        return cache.getCacheStats();
    }

    public static ResourceCacheManager getInstance() {
        return INSTANCE;
    }

}
