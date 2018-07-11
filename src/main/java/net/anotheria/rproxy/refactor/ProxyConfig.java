package net.anotheria.rproxy.refactor;

import net.anotheria.rproxy.refactor.cache.CacheStorage;
import net.anotheria.rproxy.refactor.cache.ICacheStrategy;
import net.anotheria.rproxy.refactor.config.CacheConfigurer;
import net.anotheria.rproxy.refactor.config.CacheStrategyConfigConfigurer;
import net.anotheria.rproxy.refactor.config.StrategyConfig;
import org.configureme.ConfigurationManager;
import org.configureme.annotations.AfterConfiguration;
import org.configureme.annotations.ConfigureMe;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO singleton maybe?
 *
 * @param <K> type of Key for cache
 * @param <V> type of Value for cache
 */
@ConfigureMe(allfields = true)
public class ProxyConfig<K, V> {

    private String[] sites;
    private CacheStorage[] cacheStorages;

    //---------
    /**
     * Map where Key - site name and Value - SiteConfig entity
     */
    private Map<String, SiteConfig> siteConfigMap;
    /**
     * Map where Key - site name and Value - SiteHelper entity
     */
    private Map<String, SiteHelper> siteHelperMap;
    /**
     * Proxy cache.
     * <p>Key - site name, Value - Map where Key - file extension,
     * Value - reference to the Cache Strategy entity selected in site configuration by user.</p>
     */
    private Map<String, Map<String, ICacheStrategy<K, V>>> cache;

    /**
     * Storage map. Key - alias, Value - reference to storage.
     */
    private Map<String, CacheStorage> storageMap;

    public ProxyConfig() {
        siteConfigMap = new HashMap<>();
        siteHelperMap = new HashMap<>();
        cache = new HashMap<>();
        storageMap = new HashMap<>();
    }

    public String[] getSites() {
        return sites;
    }

    public void setSites(String[] sites) {
        this.sites = sites;
    }

    public Map<String, SiteConfig> getSiteConfigMap() {
        return siteConfigMap;
    }

    public void setSiteConfigMap(Map<String, SiteConfig> siteConfigMap) {
        this.siteConfigMap = siteConfigMap;
    }

    public Map<String, SiteHelper> getSiteHelperMap() {
        return siteHelperMap;
    }

    public void setSiteHelperMap(Map<String, SiteHelper> siteHelperMap) {
        this.siteHelperMap = siteHelperMap;
    }

    public Map<String, Map<String, ICacheStrategy<K, V>>> getCache() {
        return cache;
    }

    public void setCache(Map<String, Map<String, ICacheStrategy<K, V>>> cache) {
        this.cache = cache;
    }

    public CacheStorage[] getCacheStorages() {
        return cacheStorages;
    }

    public void setCacheStorages(CacheStorage[] cacheStorages) {
        this.cacheStorages = cacheStorages;
    }

    public Map<String, CacheStorage> getStorageMap() {
        return storageMap;
    }

    public void setStorageMap(Map<String, CacheStorage> storageMap) {
        this.storageMap = storageMap;
    }

    /**
     * This method initialises {@link ProxyConfig#siteConfigMap}, {@link ProxyConfig#siteHelperMap} and
     * {@link ProxyConfig#cache}. Also fills {@link ProxyConfig#siteConfigMap} and
     * {@link ProxyConfig#siteHelperMap} with objects taken from configuration json files whose names
     * are listed in {@link ProxyConfig#sites}.
     */
    @AfterConfiguration
    public void initializeConfiguration() {
        for (CacheStorage storage : cacheStorages) {
            storageMap.put(storage.getAlias(), storage);
            createStorage(storage.getFolder());
        }
        for (String site : sites) {
            SiteConfig sc = new SiteConfig();
            ConfigurationManager.INSTANCE.configureAs(sc, site);
            //System.out.println("Config : " + site + " -> " + sc.getSiteCredentials());
            siteConfigMap.put(site, sc);
            SiteHelper siteHelper = new SiteHelper(new URLHelper(sc.getSourcePath()), new URLHelper(sc.getTargetPath()));
            siteHelperMap.put(site, siteHelper);

            if (sc.getCachingPolicy() != null && sc.getCachingPolicy().getCacheStrategy() != null) {
                StrategyConfig curConfig = CacheStrategyConfigConfigurer.getByStrategyEnumAndConfigName(sc.getCachingPolicy().getCacheStrategy().getName(), sc.getCachingPolicy().getCacheStrategy().getConfigName());
                //if (curConfig != null) {
                if (cache.get(site) == null) {
                    Map<String, ICacheStrategy<K, V>> tmp = new HashMap<>();
                    cache.put(site, tmp);
                }
                ICacheStrategy<K, V> cacheInstance;
                //System.out.println(sc + " -> Strategy : " + sc.getCachingPolicy().getCacheStrategy().getName());
                switch (sc.getCachingPolicy().getCacheStrategy().getName()) {

                    case LRU:
                        cacheInstance = new CacheConfigurer<K, V>().configureLRU(curConfig);
                        for (String fileType : sc.getCachingPolicy().getFileType()) {
                            cache.get(site).put(fileType, cacheInstance);
                        }
                        break;
                    case PERMANENT:
                        cacheInstance = new CacheConfigurer<K, V>().configurePermanent(storageMap.get(sc.getAlias()).getFolder());
                        for (String fileType : sc.getCachingPolicy().getFileType()) {
                            cache.get(site).put(fileType, cacheInstance);
                        }
                        break;
                    case AUTOEXPIRY_MEMORY:
                        //undone
                        cacheInstance = new CacheConfigurer<K, V>().configureAutoExpiryMemory(curConfig);
                        for (String fileType : sc.getCachingPolicy().getFileType()) {
                            cache.get(site).put(fileType, cacheInstance);
                        }
                        break;
                    case AUTOEXPIRY_DISK:
                        //undone
                        cacheInstance = new CacheConfigurer<K, V>().configureAutoExpiryDisk(curConfig, storageMap.get(sc.getAlias()).getFolder());
                        for (String fileType : sc.getCachingPolicy().getFileType()) {
                            cache.get(site).put(fileType, cacheInstance);
                        }
                        break;
                }
                // }
            }
        }
    }

    private void createStorage(String path) {
        new File(path).mkdirs();
        //System.out.println("Trying to create ... " + path);
    }
}
