package net.anotheria.rproxy.refactor;

import net.anotheria.rproxy.refactor.cache.CachingPolicy;
import org.configureme.annotations.ConfigureMe;

import java.util.Arrays;

@ConfigureMe(allfields = true)
public class SiteConfig {

    private String sourcePath;
    private String targetPath;
    private RewriteRule[] rewriteRules;
    private CachingPolicy[] cachingPolicy;

    public SiteConfig(String sourcePath, String targetPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
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

    public CachingPolicy[] getCachingPolicy() {
        return cachingPolicy;
    }

    public void setCachingPolicy(CachingPolicy[] cachingPolicy) {
        this.cachingPolicy = cachingPolicy;
    }

    @Override
    public String toString() {
        return "SiteConfig{" +
                "sourcePath='" + sourcePath + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", rewriteRules=" + Arrays.toString(rewriteRules) +
                ", cachingPolicy=" + Arrays.toString(cachingPolicy) +
                '}';
    }
}

