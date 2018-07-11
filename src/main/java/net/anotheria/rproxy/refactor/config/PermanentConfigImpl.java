package net.anotheria.rproxy.refactor.config;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;

public class PermanentConfigImpl implements StrategyConfig {

    @Override
    public CacheStrategyEnum getStrategy() {
        return CacheStrategyEnum.PERMANENT;
    }
}
