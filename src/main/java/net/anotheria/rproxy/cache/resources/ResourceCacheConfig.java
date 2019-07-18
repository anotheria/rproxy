package net.anotheria.rproxy.cache.resources;

/**
 * @author
 */
public class ResourceCacheConfig {

    private String sourceDomain = "www.thecasuallounge.ch";

    private String[] cacheableResourcesSuffix = new String[]{
            ".js",
            ".png",
            ".jpeg",
            ".font",
            ".css"
    };

    private String[] excludedResourcesSuffix = new String[]{};

    private String fsStoragePath = "/Users/dima/work/nazax/ano/storage";

    public static final String ALL = "*";

    public String getSourceDomain() {
        return sourceDomain;
    }

    public void setSourceDomain(String sourceDomain) {
        this.sourceDomain = sourceDomain;
    }

    public String[] getCacheableResourcesSuffix() {
        return cacheableResourcesSuffix;
    }

    public void setCacheableResourcesSuffix(String[] cacheableResourcesSuffix) {
        this.cacheableResourcesSuffix = cacheableResourcesSuffix;
    }

    public String[] getExcludedResourcesSuffix() {
        return excludedResourcesSuffix;
    }

    public void setExcludedResourcesSuffix(String[] excludedResourcesSuffix) {
        this.excludedResourcesSuffix = excludedResourcesSuffix;
    }

    public String getFsStoragePath() {
        return fsStoragePath;
    }

    public void setFsStoragePath(String fsStoragePath) {
        this.fsStoragePath = fsStoragePath;
    }
}
