package net.anotheria.rproxy.refactor.cache;

import org.configureme.annotations.ConfigureMe;

import java.util.Arrays;

@ConfigureMe(allfields = true)
public class CachingPolicy {
    private String[] fileType;
    private CacheStorage cacheStorage;
    private CacheStrategy cacheStrategy;

    public String[] getFileType() {
        return fileType;
    }

    public void setFileType(String[] fileType) {
        this.fileType = fileType;
    }

    public CacheStorage getCacheStorage() {
        return cacheStorage;
    }

    public void setCacheStorage(CacheStorage cacheStorage) {
        this.cacheStorage = cacheStorage;
    }

    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }

    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    @Override
    public String toString() {
        return "CachingPolicy{" +
                "fileType=" + Arrays.toString(fileType) +
                ", cacheStorage=" + cacheStorage +
                ", cacheStrategy=" + cacheStrategy +
                '}';
    }
}
