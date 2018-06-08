package net.anotheria.rproxy;

public class RuleTopDomain {
    private String topDomain;
    private ProxyHelper proxyHelper;

    public String getTopDomain() {
        return topDomain;
    }

    public void setTopDomain(String topDomain) {
        this.topDomain = topDomain;
    }

    public ProxyHelper getProxyHelper() {
        return proxyHelper;
    }

    public void setProxyHelper(ProxyHelper proxyHelper) {
        this.proxyHelper = proxyHelper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RuleTopDomain that = (RuleTopDomain) o;

        if (topDomain != null ? !topDomain.equals(that.topDomain) : that.topDomain != null) return false;
        return proxyHelper != null ? proxyHelper.equals(that.proxyHelper) : that.proxyHelper == null;
    }

    @Override
    public int hashCode() {
        int result = topDomain != null ? topDomain.hashCode() : 0;
        result = 31 * result + (proxyHelper != null ? proxyHelper.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RuleTopDomain{" +
                "topDomain='" + topDomain + '\'' +
                ", proxyHelper=" + proxyHelper +
                '}';
    }
}
