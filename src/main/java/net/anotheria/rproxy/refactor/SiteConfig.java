package net.anotheria.rproxy.refactor;

import java.util.Arrays;

import org.configureme.annotations.ConfigureMe;
import org.configureme.annotations.DontConfigure;

import net.anotheria.rproxy.refactor.cache.CachingPolicy;

/**
 * Site Configuration class.
 */
@ConfigureMe(allfields = true)
public class SiteConfig {

    /**
     * URL of our host.
     */
    private String sourcePath;
    /**
     * URL of resource to take content from.
     */
    private String targetPath;
    private RewriteRule[] rewriteRules;
    /**
     * Caching policy for this site.
     */
    private CachingPolicy cachingPolicy;
    /**
     * Credentials for for this site.
     */
    private SiteCredentials siteCredentials;

    private String alias;

    private String[] baseLocales;

    private String[] excludeHosts;

    private LocaleSpecialTarget[] localeSpecialTargets;

    private HostLocaleMapping[] hostLocaleMapping;

    private String[] cacheableResourcesSuffix = new String[]{
            ".js",
            ".png",
            ".jpg",
            ".jpeg",
            ".font",
            ".css"
    };
    private String[] excludedCecheableResourcesSuffix = new String[]{};
    private String cacheableResourcesFsStoragePath = "/tmp/cache";
    private int resourceCacheTtlSeconds = 1800;
    private int resourceCacheMaxSize = 100;
    private int resourceCacheStartSize = 1000;

    @DontConfigure
    public static final String ALL = "*";

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public SiteConfig() {
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public RewriteRule[] getRewriteRules() {
        return rewriteRules;
    }

    public void setRewriteRules(RewriteRule[] rewriteRules) {
        this.rewriteRules = rewriteRules;
    }

    public CachingPolicy getCachingPolicy() {
        return cachingPolicy;
    }

    public void setCachingPolicy(CachingPolicy cachingPolicy) {
        this.cachingPolicy = cachingPolicy;
    }

    public SiteCredentials getSiteCredentials() {
        return siteCredentials;
    }

    public void setSiteCredentials(SiteCredentials siteCredentials) {
        this.siteCredentials = siteCredentials;
    }

    public String[] getBaseLocales() {
        return baseLocales;
    }

    public void setBaseLocales(String[] baseLocales) {
        this.baseLocales = baseLocales;
    }

    public String[] getExcludeHosts() {
        return excludeHosts;
    }

    public void setExcludeHosts(String[] excludeHosts) {
        this.excludeHosts = excludeHosts;
    }

    @Override
    public String toString() {
        return "SiteConfig{" +
                "sourcePath='" + sourcePath + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", rewriteRules=" + Arrays.toString(rewriteRules) +
                ", cachingPolicy=" + cachingPolicy +
                '}';
    }

    public LocaleSpecialTarget[] getLocaleSpecialTargets() {
        return localeSpecialTargets;
    }

    public void setLocaleSpecialTargets(LocaleSpecialTarget[] localeSpecialTargets) {
        this.localeSpecialTargets = localeSpecialTargets;
    }

    public HostLocaleMapping[] getHostLocaleMapping() {
        return hostLocaleMapping;
    }

    public void setHostLocaleMapping(HostLocaleMapping[] hostLocaleMapping) {
        this.hostLocaleMapping = hostLocaleMapping;
    }

    public String[] getCacheableResourcesSuffix() {
        return cacheableResourcesSuffix;
    }

    public void setCacheableResourcesSuffix(String[] cacheableResourcesSuffix) {
        this.cacheableResourcesSuffix = cacheableResourcesSuffix;
    }

    public String[] getExcludedCecheableResourcesSuffix() {
        return excludedCecheableResourcesSuffix;
    }

    public void setExcludedCecheableResourcesSuffix(String[] excludedCecheableResourcesSuffix) {
        this.excludedCecheableResourcesSuffix = excludedCecheableResourcesSuffix;
    }

    public String getCacheableResourcesFsStoragePath() {
        return cacheableResourcesFsStoragePath;
    }

    public void setCacheableResourcesFsStoragePath(String cacheableResourcesFsStoragePath) {
        this.cacheableResourcesFsStoragePath = cacheableResourcesFsStoragePath;
    }

    public int getResourceCacheTtlSeconds() {
        return resourceCacheTtlSeconds;
    }

    public void setResourceCacheTtlSeconds(int resourceCacheTtlSeconds) {
        this.resourceCacheTtlSeconds = resourceCacheTtlSeconds;
    }

    public int getResourceCacheMaxSize() {
        return resourceCacheMaxSize;
    }

    public void setResourceCacheMaxSize(int resourceCacheMaxSize) {
        this.resourceCacheMaxSize = resourceCacheMaxSize;
    }

    public int getResourceCacheStartSize() {
        return resourceCacheStartSize;
    }

    public void setResourceCacheStartSize(int resourceCacheStartSize) {
        this.resourceCacheStartSize = resourceCacheStartSize;
    }
}

