package net.anotheria.rproxy.cache.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.anotheria.anoprise.cache.Cache;
import net.anotheria.anoprise.cache.Caches;
import net.anotheria.moskito.core.predefined.CacheStats;
import net.anotheria.rproxy.cache.resources.bean.CacheableResource;
import net.anotheria.rproxy.cache.resources.bean.ResourceCacheKey;
import net.anotheria.rproxy.refactor.RProxyFactory;
import net.anotheria.rproxy.refactor.SiteConfig;

/**
 * @author
 */
public class ResourceCacheManager {

    private Map<String, Cache<ResourceCacheKey, CacheableResource>> caches = new ConcurrentHashMap<>();
    private static final ResourceCacheManager INSTANCE = new ResourceCacheManager();

    private ResourceCacheManager() {
    }

    public void put(String cacheName, ResourceCacheKey key, CacheableResource value) {
        getCache(cacheName).put(key, value);
    }

    public CacheableResource get(String cacheName, ResourceCacheKey key) {
        return getCache(cacheName).get(key);
    }

    public void remove(String cacheName, ResourceCacheKey key) {
        getCache(cacheName).remove(key);
    }

    public Map<String, CacheStats> getStats() {
        Map<String, CacheStats> ret = new HashMap<>();
        for (Map.Entry<String, Cache<ResourceCacheKey, CacheableResource>> cacheEntry : caches.entrySet()) {
            ret.put(cacheEntry.getKey(), cacheEntry.getValue().getCacheStats());
        }

        return ret;
    }

    public static ResourceCacheManager getInstance() {
        return INSTANCE;
    }

    private Cache<ResourceCacheKey, CacheableResource> getCache(String cacheName) {
        Cache<ResourceCacheKey, CacheableResource> ret = caches.get(cacheName);
        if (ret != null) {
            return ret;
        }
        //return caches.computeIfAbsent(cacheName, v -> createCache(cacheName));//todo doesn't work with jersey 1.8

        return createCache(cacheName);
    }

    private synchronized Cache<ResourceCacheKey, CacheableResource> createCache(String cacheName) {
        Cache<ResourceCacheKey, CacheableResource> ret = caches.get(cacheName);
        if (ret != null) {
            return ret;
        }

        SiteConfig config = RProxyFactory.getInstance().getProxyConfig().getSiteConfigMap().get(cacheName);
        if (config != null) {
            ret = Caches.createSoftReferenceExpiringCache("resource-cache_" + cacheName,
                    config.getResourceCacheStartSize(),
                    config.getResourceCacheMaxSize(),
                    (int) TimeUnit.SECONDS.toMillis(config.getResourceCacheTtlSeconds()));
            caches.put(cacheName, ret);
            return ret;
        }
        ret = Caches.createSoftReferenceExpiringCache("resource-cache_" + cacheName, 100, 1000, (int) TimeUnit.MINUTES.toMillis(300));
        caches.put(cacheName, ret);
        return ret;
    }
}
