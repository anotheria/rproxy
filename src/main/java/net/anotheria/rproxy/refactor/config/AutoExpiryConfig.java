package net.anotheria.rproxy.refactor.config;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;

public class AutoExpiryConfig implements IConfig{

    private Long scanInterval;

    public Long getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(Long scanInterval) {
        this.scanInterval = scanInterval;
    }

    @Override
    public CacheStrategyEnum getStrategy() {
        return CacheStrategyEnum.AUTOEXPIRY;
    }
}
