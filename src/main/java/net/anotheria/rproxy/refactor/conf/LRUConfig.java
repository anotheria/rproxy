package net.anotheria.rproxy.refactor.conf;

import org.configureme.annotations.ConfigureMe;

@ConfigureMe(allfields = true)
public class LRUConfig implements IConfig{
    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
