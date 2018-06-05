package net.anotheria.rproxy;

import java.net.MalformedURLException;
import java.net.URL;

public class Parser {

    public static void main(String... args) throws MalformedURLException {
        LinkEntity l = new LinkEntity("https://www.cms.casual.com");
        URL test = new URL("https://www.cms.casual.com");
        System.out.println(test.getProtocol());
        System.out.println(test.getHost());
        System.out.println(test.getAuthority());
        System.out.println(test.getRef());
    }


    public static final String DOT = ".";
    public static final String SLASH = "/";

    public static void parse(LinkEntity link) throws MalformedURLException {

        URL aURL = new URL(link.getOrigin());

        link.setProtocol(aURL.getProtocol());
        link.setHost(aURL.getAuthority());
        //search for subdomain and domain
        String[] d = link.getHost().split("\\.");
        if(d.length == 3){
            link.setSubdomain(d[0]);
            link.setDomain(d[1] + DOT + d[2]);
            link.setTopLevelDomain(d[2]);
        }else{
            link.setTopLevelDomain(d[1]);
            link.setDomain(link.getHost());
        }

        link.setPath(aURL.getPath());

        System.out.println(link.toString());
    }
}
