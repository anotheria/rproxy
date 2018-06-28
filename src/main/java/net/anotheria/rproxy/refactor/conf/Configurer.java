package net.anotheria.rproxy.refactor.conf;

import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.refactor.cache.LRUStrategy;

public class Configurer {

    public static LRUStrategy<String, HttpProxyResponse> configureLRU(LRUConfig config){
        return new LRUStrategy<>(config.getSize());
    }
}
