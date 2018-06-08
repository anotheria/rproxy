package net.anotheria.rproxy;

import java.util.LinkedList;
import java.util.List;

/**
 * Class represents general rules configured via web.xml
 */
public class Rule {
    private String subDomain;
    private ProxyHelper proxyHelperDefault;
    private List<RuleTopDomain> topDomainList;

    public Rule() {
        topDomainList = new LinkedList<>();
    }

    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public ProxyHelper getProxyHelperDefault() {
        return proxyHelperDefault;
    }

    public void setProxyHelperDefault(ProxyHelper proxyHelperDefault) {
        this.proxyHelperDefault = proxyHelperDefault;
    }

    public List<RuleTopDomain> getTopDomainList() {
        return topDomainList;
    }

    public void setTopDomainList(List<RuleTopDomain> topDomainList) {
        this.topDomainList = topDomainList;
    }

    public void addTopDomainRule(RuleTopDomain r) {
        this.topDomainList.add(r);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;

        if (subDomain != null ? !subDomain.equals(rule.subDomain) : rule.subDomain != null) return false;
        if (proxyHelperDefault != null ? !proxyHelperDefault.equals(rule.proxyHelperDefault) : rule.proxyHelperDefault != null)
            return false;
        return topDomainList != null ? topDomainList.equals(rule.topDomainList) : rule.topDomainList == null;
    }

    @Override
    public int hashCode() {
        int result = subDomain != null ? subDomain.hashCode() : 0;
        result = 31 * result + (proxyHelperDefault != null ? proxyHelperDefault.hashCode() : 0);
        result = 31 * result + (topDomainList != null ? topDomainList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "subDomain='" + subDomain + '\'' +
                ", proxyHelperDefault=" + proxyHelperDefault +
                ", topDomainList=" + topDomainList +
                '}';
    }
}
