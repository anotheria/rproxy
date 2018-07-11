package net.anotheria.rproxy.refactor.config;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;
import org.configureme.annotations.ConfigureMe;

/**
 * Configuration for LRU strategy.
 */
@ConfigureMe(allfields = true)
public class LRUConfigImpl implements StrategyConfig {

    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public CacheStrategyEnum getStrategy() {
        return CacheStrategyEnum.LRU;
    }
}
