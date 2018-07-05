package net.anotheria.rproxy.refactor.conf;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;
import org.configureme.ConfigurationManager;

public class CacheStrategyConfigConfigurer {

    public static IConfig getByStrategyEnumAndConfigName(CacheStrategyEnum name, String configName) {
        if (name == null || configName == null)
            return null;

        IConfig config;
        switch (name) {
            case LRU:
                config = new LRUConfig();
                ConfigurationManager.INSTANCE.configureAs(config, configName);
                return config;
            case AUTOEXPIRY:
                config = new AutoExpiryConfig();
                ConfigurationManager.INSTANCE.configureAs(config, configName);
                return null;
            case PERMANENT:
                config = new PermanentConfig();
                ConfigurationManager.INSTANCE.configureAs(config, configName);
                return null;
        }

        return null;
    }
}
