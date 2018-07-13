package net.anotheria.rproxy.refactor.config;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;
import org.configureme.ConfigurationManager;

public class CacheStrategyConfigConfigurer {

    public static StrategyConfig getByStrategyEnumAndConfigName(CacheStrategyEnum name, String configName) {
        if (name == null || configName == null)
            return null;

        StrategyConfig config;
        switch (name) {
            case LRU:
                config = new LRUConfigImpl();
                ConfigurationManager.INSTANCE.configureAs(config, configName);
                return config;
            case AUTOEXPIRY_MEMORY:
                config = new AutoExpiryConfigImpl();
                ConfigurationManager.INSTANCE.configureAs(config, configName);
                return config;
            case AUTOEXPIRY_DISK:
                config = new AutoExpiryConfigImpl();
                ConfigurationManager.INSTANCE.configureAs(config, configName);
                return config;
            case PERMANENT:
                config = new PermanentConfigImpl();
                ConfigurationManager.INSTANCE.configureAs(config, configName);
                return config;
        }

        return null;
    }
}
