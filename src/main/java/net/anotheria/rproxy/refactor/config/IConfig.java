package net.anotheria.rproxy.refactor.config;

import net.anotheria.rproxy.refactor.cache.CacheStrategyEnum;

/**
 * Interface to mark up Configs for Cache Strategies.
 */
public interface IConfig {

    CacheStrategyEnum getStrategy();
}
