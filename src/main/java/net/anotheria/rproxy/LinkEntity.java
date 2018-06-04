package net.anotheria.rproxy;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of the common link.
 * <p>
 * For example, www.youtube.com/watch?v=shdsGHhskK
 * </p>
 * <p>www - subdomain.</p> +
 * <p>youtube.com - domain.</p> +
 * <p>subdomain + domain = host.</p> +
 * <p>com - top-level domain.</p> +
 * <p>watch - the path.</p> +
 * <p>v - parameter.</p>
 * <p>shdsGHhskK - parameter value.</p>
 */
public class LinkEntity {

    private String protocol;
    private String subdomain;
    private String domain;
    private String host;
    private String topLevelDomain;
    private String path;
    private String query;
    private String origin;

    public LinkEntity(String link) {
        origin = link;
        try {
            Parser.parse(this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTopLevelDomain() {
        return topLevelDomain;
    }

    public void setTopLevelDomain(String topLevelDomain) {
        this.topLevelDomain = topLevelDomain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return "LinkEntity{" +
                "protocol='" + protocol + '\'' +
                ", subdomain='" + subdomain + '\'' +
                ", domain='" + domain + '\'' +
                ", host='" + host + '\'' +
                ", topLevelDomain='" + topLevelDomain + '\'' +
                ", path='" + path + '\'' +
                ", query='" + query + '\'' +
                ", origin='" + origin + '\'' +
                '}';
    }


}
