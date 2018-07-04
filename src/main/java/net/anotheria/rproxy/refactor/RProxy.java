package net.anotheria.rproxy.refactor;

import org.configureme.ConfigurationManager;


public final class RProxy<K, V> {

    public static final String DEFAULT_CONFIG_FILE_NAME = "proxyConfig";

    private ProxyConfig<K, V> proxyConfig;

    public RProxy() {
        proxyConfig = new ProxyConfig<>();
        ConfigurationManager.INSTANCE.configureAs(proxyConfig, DEFAULT_CONFIG_FILE_NAME);
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

    public boolean siteConfigurationPresent(String siteName) {
        if (proxyConfig.getSiteConfigMap().containsKey(siteName)) {
            return true;
        }

        return false;
    }

    public boolean siteProvidesCaching(String siteName) {
        if (proxyConfig.getCache() != null && proxyConfig.getCache().containsKey(siteName)) {
            return true;
        }

        return false;
    }

    public boolean fileMustBeCached(String siteName, String fileExtension){
        if(proxyConfig.getCache().get(siteName).containsKey(fileExtension)){
            return true;
        }
        return false;
    }

    public V retrieveFromCache(String siteName, String fileExtension, K key) {
        //System.out.println(siteName + " " + fileExtension + " " + key);
        if (siteConfigurationPresent(siteName) && siteProvidesCaching(siteName) && proxyConfig.getCache().get(siteName).containsKey(fileExtension)) {
            return proxyConfig.getCache().get(siteName).get(fileExtension).get(key);
        }

        return null;
    }

    public void addToCache(K key, V value, String siteName, String fileExtension){
        if(siteConfigurationPresent(siteName) && siteProvidesCaching(siteName) && fileMustBeCached(siteName, fileExtension)){
            proxyConfig.getCache().get(siteName).get(fileExtension).add(key, value);
            //System.out.println(siteName + " " + fileExtension + " " + key + " " + value);
        }
    }

}
