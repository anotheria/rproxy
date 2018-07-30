package net.anotheria.rproxy.refactor;

import org.configureme.annotations.ConfigureMe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ConfigureMe(allfields = true)
public class RewriteRule {
    private String source;
    private String target;
    private RewriteType rewriteType;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public RewriteType getRewriteType() {
        return rewriteType;
    }

    public void setRewriteType(RewriteType rewriteType) {
        this.rewriteType = rewriteType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RewriteRule that = (RewriteRule) o;

        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        return rewriteType == that.rewriteType;
    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (rewriteType != null ? rewriteType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RewriteRule{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", rewriteType=" + rewriteType +
                '}';
    }
}
