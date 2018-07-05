package net.anotheria.rproxy.refactor.conf;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;

public class PermanentConfig implements IConfig {

    @Override
    public CacheStrategyEnum getStrategy() {
        return CacheStrategyEnum.PERMANENT;
    }
}
