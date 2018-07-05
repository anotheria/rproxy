package net.anotheria.rproxy.refactor.conf;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;

public class AutoExpiryConfig implements IConfig{

    @Override
    public CacheStrategyEnum getStrategy() {
        return CacheStrategyEnum.AUTOEXPIRY;
    }
}
