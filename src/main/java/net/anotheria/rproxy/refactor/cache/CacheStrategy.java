package net.anotheria.rproxy.refactor.cache;

import net.anotheria.rproxy.refactor.conf.IConfig;
import org.configureme.ConfigurationManager;
import org.configureme.annotations.ConfigureMe;

/**
 * Cache strategy class.
 */
@ConfigureMe(allfields = true)
public class CacheStrategy {

    private CacheStrategyEnum name;
    private String configName;

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

    @Override
    public String toString() {
        return "CacheStrategy{" +
                ", name=" + name +
                ", configName='" + configName + '\'' +
                '}';
    }

    private void init(IConfig conf, String name){
        ConfigurationManager.INSTANCE.configureAs(conf, name);
    }
}
