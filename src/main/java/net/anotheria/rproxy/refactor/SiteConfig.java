package net.anotheria.rproxy.refactor;

import org.configureme.annotations.ConfigureMe;

import java.util.Arrays;

@ConfigureMe(allfields = true)
public class SiteConfig {

    private String sourcePath;
    private String targetPath;
    private RewriteRule[] rewriteRules;

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

    @Override
    public String toString() {
        return "SiteConfig{" +
                "sourcePath='" + sourcePath + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", rewriteRules=" + Arrays.toString(rewriteRules) +
                '}';
    }
}

