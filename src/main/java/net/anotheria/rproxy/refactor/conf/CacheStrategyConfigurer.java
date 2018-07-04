package net.anotheria.rproxy.refactor.conf;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;
import org.configureme.ConfigurationManager;

public class CacheStrategyConfigurer {

    public static IConfig getByStrategyEnumAndConfigName(CacheStrategyEnum name, String configName) {
        if (name == null || configName == null)
            return null;

        switch (name) {
            case LRU:
                LRUConfig config = new LRUConfig();
                ConfigurationManager.INSTANCE.configureAs(config, configName);
                return config;
        }

        return null;
    }
}
