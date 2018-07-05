package net.anotheria.rproxy.refactor.cache;

import org.configureme.annotations.ConfigureMe;

@ConfigureMe(allfields = true)
public class CacheStorage {
    private String folder;
    private String alias;

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
