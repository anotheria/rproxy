package net.anotheria.rproxy.refactor;

import org.configureme.annotations.ConfigureMe;

@ConfigureMe(allfields = true)
public class ProxyConfig {

    private String[] sites;

    public String[] getSites() {
        return sites;
    }

    public void setSites(String[] sites) {
        this.sites = sites;
    }
}
