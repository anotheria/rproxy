package net.anotheria.rproxy;

import java.net.URL;

/**
 * Class represents data for requests.
 */
public class ProxyHelper {

    private String hostBase;
    private String baseLink;
    private String meSubFolder;
    private String subFolder;
    private String topDomain;
    private String hostProtocol;
    private String hostMe;
    private String me;

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

    public String getHostProtocol() {
        return hostProtocol;
    }

    public void setHostProtocol(String hostProtocol) {
        this.hostProtocol = hostProtocol;
    }

    public String getHostMe() {
        return hostMe;
    }

    public void setHostMe(String hostMe) {
        this.hostMe = hostMe;
    }

    public String getMe() {
        return me;
    }

    public void setMe(String me) {
        this.me = me;
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
        if (topDomain != null ? !topDomain.equals(that.topDomain) : that.topDomain != null) return false;
        if (hostProtocol != null ? !hostProtocol.equals(that.hostProtocol) : that.hostProtocol != null) return false;
        if (hostMe != null ? !hostMe.equals(that.hostMe) : that.hostMe != null) return false;
        return me != null ? me.equals(that.me) : that.me == null;
    }

    @Override
    public int hashCode() {
        int result = hostBase != null ? hostBase.hashCode() : 0;
        result = 31 * result + (baseLink != null ? baseLink.hashCode() : 0);
        result = 31 * result + (meSubFolder != null ? meSubFolder.hashCode() : 0);
        result = 31 * result + (subFolder != null ? subFolder.hashCode() : 0);
        result = 31 * result + (topDomain != null ? topDomain.hashCode() : 0);
        result = 31 * result + (hostProtocol != null ? hostProtocol.hashCode() : 0);
        result = 31 * result + (hostMe != null ? hostMe.hashCode() : 0);
        result = 31 * result + (me != null ? me.hashCode() : 0);
        return result;
    }

    public void subFolderUpdate(String currentSubFolder) {
        System.out.println("!! " + currentSubFolder);
        subFolder = currentSubFolder;
        meSubFolder = me + subFolder;
    }

    @Override
    public String toString() {
        return "ProxyHelper{" +
                "hostBase='" + hostBase + '\'' +
                ", baseLink='" + baseLink + '\'' +
                ", meSubFolder='" + meSubFolder + '\'' +
                ", subFolder='" + subFolder + '\'' +
                ", topDomain='" + topDomain + '\'' +
                ", hostProtocol='" + hostProtocol + '\'' +
                ", hostMe='" + hostMe + '\'' +
                ", me='" + me + '\'' +
                '}';
    }
}
