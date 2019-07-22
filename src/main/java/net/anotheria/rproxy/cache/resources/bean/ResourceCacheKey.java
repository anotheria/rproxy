package net.anotheria.rproxy.cache.resources.bean;

import java.util.Objects;

/**
 * @author
 */
public class ResourceCacheKey {
    private String url;
    private String siteName;

    public ResourceCacheKey(String siteName, String url) {
        this.siteName = siteName;
        this.url = url;
    }

    public String getSiteName() {
        return siteName;
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
        ResourceCacheKey that = (ResourceCacheKey) o;
        return Objects.equals(url, that.url) && Objects.equals(siteName, that.siteName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, siteName);
    }
}
