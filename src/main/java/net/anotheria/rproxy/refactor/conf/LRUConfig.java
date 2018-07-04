package net.anotheria.rproxy.refactor.conf;

import org.configureme.annotations.ConfigureMe;

/**
 * Configuration for LRU strategy.
 */
@ConfigureMe(allfields = true)
public class LRUConfig implements IConfig {
    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
