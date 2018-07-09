package net.anotheria.rproxy.refactor;

import org.configureme.ConfigurationManager;


public final class RProxy<K, V> {

    public static final String DEFAULT_CONFIG_FILE_NAME = "proxyConfig";

    private ProxyConfig<K, V> proxyConfig;

    public RProxy() {
        try {
            proxyConfig = new ProxyConfig<>();
            ConfigurationManager.INSTANCE.configureAs(proxyConfig, DEFAULT_CONFIG_FILE_NAME);
        } catch (Exception e) {

        }
    }

    public RProxy(String configFileName) {
        proxyConfig = new ProxyConfig<>();
        ConfigurationManager.INSTANCE.configureAs(proxyConfig, configFileName);
    }

    public ProxyConfig<K, V> getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(ProxyConfig<K, V> proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    /**
     * If there are present configuration for site with name
     *
     * @param siteName
     * @return true if configuration present, otherwise false.
     */
    public boolean siteConfigurationPresent(String siteName) {
        if (proxyConfig.getSiteConfigMap().containsKey(siteName)) {
            return true;
        }

        return false;
    }

    /**
     * If site should be provided with cache.
     *
     * @param siteName
     * @return true if cache provide required, otherwise false.
     */
    public boolean siteProvidesCaching(String siteName) {
        if (proxyConfig.getCache() != null && proxyConfig.getCache().containsKey(siteName)) {
            return true;
        }

        return false;
    }

    /**
     * Check if file extension must be added to cache.
     *
     * @param siteName
     * @param fileExtension
     * @return true if file should be cached for this site, otherwise false.
     */
    public boolean fileMustBeCached(String siteName, String fileExtension) {
        if (proxyConfig.getCache().get(siteName).containsKey(fileExtension)) {
            return true;
        }
        return false;
    }

    /**
     * Get value from cache.
     *
     * @param siteName      from which Value was cached.
     * @param fileExtension extension of requested file
     * @param key
     * @return Value retrieved from cache if present or null.
     */
    public V retrieveFromCache(String siteName, String fileExtension, K key) {
        //System.out.println(siteName + " " + fileExtension + " " + key);
        if (siteConfigurationPresent(siteName) && siteProvidesCaching(siteName) && proxyConfig.getCache().get(siteName).containsKey(fileExtension)) {
            return proxyConfig.getCache().get(siteName).get(fileExtension).get(key);
        }

        return null;
    }

    /**
     * Add value to cache if site with specified name supports caching and file extension.
     *
     * @param key           to get Value from cache later.
     * @param value         to store in cache.
     * @param siteName      from which was downloaded resource.
     * @param fileExtension to store
     */
    public void addToCache(K key, V value, String siteName, String fileExtension) {
        if (siteConfigurationPresent(siteName) && siteProvidesCaching(siteName) && fileMustBeCached(siteName, fileExtension)) {
            proxyConfig.getCache().get(siteName).get(fileExtension).add(key, value);
            //System.out.println(siteName + " " + fileExtension + " " + key + " " + value);
        }
    }

}
