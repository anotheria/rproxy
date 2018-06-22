package net.anotheria.rproxy.conf;

import org.configureme.annotations.ConfigureMe;
import org.configureme.annotations.SetIf;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * ConfigurationEntity info from json file.
 */
@ConfigureMe(allfields = true, name = "rproxy-config")
public class ConfigurationEntity {

    private static final String KEYWORD_REPLACEMENT = "replacement.";
    private String[] baseUrl;
    private String hostUrl;
    private String[] subDomainRules;
    private String[] topDomainRules;
    private List<String[]> contentReplacement;
    private Credentials[] credentials;

    public ConfigurationEntity() {
        contentReplacement = new LinkedList<>();
    }

    public String[] getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String[] baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public String[] getSubDomainRules() {
        return subDomainRules;
    }

    public void setSubDomainRules(String[] subDomainRules) {
        this.subDomainRules = subDomainRules;
    }

    public String[] getTopDomainRules() {
        return topDomainRules;
    }

    public void setTopDomainRules(String[] topDomainRules) {
        this.topDomainRules = topDomainRules;
    }

    public List<String[]> getContentReplacement() {
        return contentReplacement;
    }

    public void setContentReplacement(List<String[]> contentReplacement) {
        this.contentReplacement = contentReplacement;
    }

    public Credentials[] getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials[] credentials) {
        this.credentials = credentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigurationEntity that = (ConfigurationEntity) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(baseUrl, that.baseUrl)) return false;
        if (hostUrl != null ? !hostUrl.equals(that.hostUrl) : that.hostUrl != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(subDomainRules, that.subDomainRules)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(topDomainRules, that.topDomainRules)) return false;
        if (contentReplacement != null ? !contentReplacement.equals(that.contentReplacement) : that.contentReplacement != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(credentials, that.credentials);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(baseUrl);
        result = 31 * result + (hostUrl != null ? hostUrl.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(subDomainRules);
        result = 31 * result + Arrays.hashCode(topDomainRules);
        result = 31 * result + (contentReplacement != null ? contentReplacement.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(credentials);
        return result;
    }

    @SetIf(condition = SetIf.SetIfCondition.startsWith, value = KEYWORD_REPLACEMENT)
    public void add(final String name, final String[] value) {
        contentReplacement.add(value);
       // System.out.println(Arrays.toString(value));
    }
}
