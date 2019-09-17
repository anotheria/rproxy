package net.anotheria.rproxy.cache.resources.bean;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author
 */
public class ResourceCacheKey implements Serializable {
    private String url;

    public ResourceCacheKey(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceCacheKey cacheKey = (ResourceCacheKey) o;
        return Objects.equals(url, cacheKey.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
