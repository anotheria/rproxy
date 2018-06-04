package net.anotheria.rproxy;

import java.net.MalformedURLException;
import java.net.URL;

public class Parser {

    public static void main(String... args){
        String hostBase = "sub.hello.com";
        String hostBase1 = "hello.com";
        String hostMe = "localhost:8080";
        String[] parts = hostBase.split("\\.");
        if (parts.length > 2) {
            hostMe += "/" + parts[0];
        }

        System.out.println(hostBase);
        System.out.println(hostMe);
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
