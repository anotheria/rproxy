package net.anotheria.rproxy.conf;

import java.util.Arrays;

public class ConfigJSON {

    private String[] baseUrl;
    private String hostUrl;
    private String[] subDomainRules;
    private String[] topDomainRules;

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

    @Override
    public String toString() {
        return "ConfigJSON{" +
                "baseUrl=" + Arrays.toString(baseUrl) +
                ", hostUrl='" + hostUrl + '\'' +
                ", subDomainRules=" + Arrays.toString(subDomainRules) +
                ", topDomainRules=" + Arrays.toString(topDomainRules) +
                '}';
    }
}
