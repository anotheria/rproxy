package net.anotheria.rproxy.refactor.config;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;

public class AutoExpiryConfig implements IConfig{

    @Override
    public CacheStrategyEnum getStrategy() {
        return CacheStrategyEnum.AUTOEXPIRY;
    }
}
