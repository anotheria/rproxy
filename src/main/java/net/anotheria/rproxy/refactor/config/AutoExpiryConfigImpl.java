package net.anotheria.rproxy.refactor.config;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;
import org.configureme.annotations.ConfigureMe;

@ConfigureMe(allfields = true)
public class AutoExpiryConfigImpl implements StrategyConfig {

    private Long intervalSeconds;
    private Long timeToLiveSeconds;

    public Long getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Long intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public Long getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(Long timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    @Override
    public CacheStrategyEnum getStrategy() {
        return CacheStrategyEnum.AUTOEXPIRY_MEMORY;
    }

    @Override
    public String toString() {
        return "AutoExpiryConfigImpl{" +
                "intervalSeconds=" + intervalSeconds +
                ", timeToLiveSeconds=" + timeToLiveSeconds +
                '}';
    }
}
