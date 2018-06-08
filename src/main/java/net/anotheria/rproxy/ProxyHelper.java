package net.anotheria.rproxy;

/**
 * Class represents data for requests.
 */
public class ProxyHelper {

    private String hostBase;
    private String baseLink;
    private String meSubFolder;
    private String subFolder;
    private String topDomain;

    public String getHostBase() {
        return hostBase;
    }

    public void setHostBase(String hostBase) {
        this.hostBase = hostBase;
    }

    public String getBaseLink() {
        return baseLink;
    }

    public void setBaseLink(String baseLink) {
        this.baseLink = baseLink;
    }

    public String getMeSubFolder() {
        return meSubFolder;
    }

    public void setMeSubFolder(String meSubFolder) {
        this.meSubFolder = meSubFolder;
    }

    public String getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(String subFolder) {
        this.subFolder = subFolder;
    }

    public String getTopDomain() {
        return topDomain;
    }

    public void setTopDomain(String topDomain) {
        this.topDomain = topDomain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProxyHelper that = (ProxyHelper) o;

        if (hostBase != null ? !hostBase.equals(that.hostBase) : that.hostBase != null) return false;
        if (baseLink != null ? !baseLink.equals(that.baseLink) : that.baseLink != null) return false;
        if (meSubFolder != null ? !meSubFolder.equals(that.meSubFolder) : that.meSubFolder != null) return false;
        if (subFolder != null ? !subFolder.equals(that.subFolder) : that.subFolder != null) return false;
        return topDomain != null ? topDomain.equals(that.topDomain) : that.topDomain == null;
    }

    @Override
    public int hashCode() {
        int result = hostBase != null ? hostBase.hashCode() : 0;
        result = 31 * result + (baseLink != null ? baseLink.hashCode() : 0);
        result = 31 * result + (meSubFolder != null ? meSubFolder.hashCode() : 0);
        result = 31 * result + (subFolder != null ? subFolder.hashCode() : 0);
        result = 31 * result + (topDomain != null ? topDomain.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProxyHelper{" +
                "hostBase='" + hostBase + '\'' +
                ", baseLink='" + baseLink + '\'' +
                ", meSubFolder='" + meSubFolder + '\'' +
                ", subFolder='" + subFolder + '\'' +
                ", topDomain='" + topDomain + '\'' +
                '}';
    }
}
