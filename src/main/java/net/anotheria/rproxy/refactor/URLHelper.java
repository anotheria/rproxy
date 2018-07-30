package net.anotheria.rproxy.refactor;

import net.anotheria.rproxy.utils.URLUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class helps to split URL to parts.
 */
public class URLHelper {
    private String host;
    private int port;
    private String path;
    private String topPath;
    private String query;
    private String protocol;
    private String locale;

    public URLHelper(String url){
        try{
            URL u = new URL(url);
            host = u.getHost();
            port = u.getPort();
            path = u.getPath();
            topPath = URLUtils.getTopPath(url);
            query = u.getQuery();
            protocol = u.getProtocol();
            locale = URLUtils.getLocaleFromHost(host);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy Constructor for URLHelper with new locale and changes host.
     * @param toCopy
     * @param locale
     */
    public URLHelper(URLHelper toCopy, String locale){
        this.host = URLUtils.replaceLocaleForHost(toCopy.getHost(), locale);
        this.port = toCopy.getPort();
        this.path = toCopy.getPath();
        this.topPath = toCopy.getTopPath();
        this.query = toCopy.getQuery();
        this.protocol = toCopy.getProtocol();
        this.locale = locale;
    }

    public String getLocale(){
        return locale;
    }

    public void setLocale(String locale){
        this.locale = locale;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTopPath() {
        return topPath;
    }

    public void setTopPath(String topPath) {
        this.topPath = topPath;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLink(){
        String res = protocol + "://" + host;
        if(port != -1)
            res += ":" + port;

        res += path;

        if(query != null)
            res += "?" + query;

        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        URLHelper urlHelper = (URLHelper) o;

        if (port != urlHelper.port) return false;
        if (host != null ? !host.equals(urlHelper.host) : urlHelper.host != null) return false;
        if (path != null ? !path.equals(urlHelper.path) : urlHelper.path != null) return false;
        if (topPath != null ? !topPath.equals(urlHelper.topPath) : urlHelper.topPath != null) return false;
        if (query != null ? !query.equals(urlHelper.query) : urlHelper.query != null) return false;
        return protocol != null ? protocol.equals(urlHelper.protocol) : urlHelper.protocol == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (topPath != null ? topPath.hashCode() : 0);
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "URLHelper{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", path='" + path + '\'' +
                ", topPath='" + topPath + '\'' +
                ", query='" + query + '\'' +
                ", protocol='" + protocol + '\'' +
                '}';
    }
}
