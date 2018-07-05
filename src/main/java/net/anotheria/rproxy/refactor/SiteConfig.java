package net.anotheria.rproxy.refactor;

import net.anotheria.rproxy.refactor.cache.CachingPolicy;
import org.configureme.annotations.ConfigureMe;

import java.util.Arrays;

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

    @Override
    public String toString() {
        return "SiteConfig{" +
                "sourcePath='" + sourcePath + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", rewriteRules=" + Arrays.toString(rewriteRules) +
                ", cachingPolicy=" + cachingPolicy +
                '}';
    }
}

