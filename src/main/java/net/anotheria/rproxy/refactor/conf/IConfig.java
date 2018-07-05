package net.anotheria.rproxy.refactor.conf;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;

/**
 * Interface to mark up Configs for Cache Strategies.
 */
public interface IConfig {

    CacheStrategyEnum getStrategy();
}
