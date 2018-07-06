package net.anotheria.rproxy.refactor.cache;

import org.configureme.annotations.ConfigureMe;

/**
 * Cache strategy class.
 */
@ConfigureMe(allfields = true)
public class CacheStrategy {

    private CacheStrategyEnum name;
    private String configName;
    private String storageAlias;

    public CacheStrategyEnum getName() {
        return name;
    }

    public void setName(CacheStrategyEnum name) {
        this.name = name;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getStorageAlias() {
        return storageAlias;
    }

    public void setStorageAlias(String storageAlias) {
        this.storageAlias = storageAlias;
    }

    @Override
    public String toString() {
        return "CacheStrategy{" +
                ", name=" + name +
                ", configName='" + configName + '\'' +
                '}';
    }
}
