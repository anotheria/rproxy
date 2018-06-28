package net.anotheria.rproxy.refactor.cache;

import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.refactor.conf.Configurer;
import net.anotheria.rproxy.refactor.conf.IConfig;
import net.anotheria.rproxy.refactor.conf.LRUConfig;
import org.configureme.ConfigurationManager;
import org.configureme.annotations.AfterConfiguration;
import org.configureme.annotations.ConfigureMe;

/**
 * To replace with Interface? ICacheStrategy ->>>
 */
@ConfigureMe(allfields = true)
public class CacheStrategy {

    private ICacheStrategy<String, HttpProxyResponse> concreteStrategy;
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

    public ICacheStrategy<String, HttpProxyResponse> getConcreteStrategy() {
        return concreteStrategy;
    }

    public void setConcreteStrategy(ICacheStrategy<String, HttpProxyResponse> concreteStrategy) {
        this.concreteStrategy = concreteStrategy;
    }

    @Override
    public String toString() {
        return "CacheStrategy{" +
                "concreteStrategy=" + concreteStrategy +
                ", name=" + name +
                ", configName='" + configName + '\'' +
                '}';
    }
    @AfterConfiguration
    public void setConcreteStrategy(){
        switch (name){
            case LRU:
                if(configName != null) {
                    concreteStrategy = new LRUStrategy<>();
                    break;
                }
                LRUConfig conf = new LRUConfig();
                init(conf, configName);
                concreteStrategy = Configurer.configureLRU(conf);
                break;
        }

    }

    private void init(IConfig conf, String name){
        ConfigurationManager.INSTANCE.configureAs(conf, name);
    }
}
