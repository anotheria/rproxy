package net.anotheria.rproxy;

import java.net.MalformedURLException;
import java.net.URL;

public class Parser {

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


        System.out.println("protocol = " + aURL.getProtocol());
        System.out.println("authority = " + aURL.getAuthority());
        System.out.println("host = " + aURL.getHost());
        System.out.println("port = " + aURL.getPort());
        System.out.println("path = " + aURL.getPath());
        System.out.println("query = " + aURL.getQuery());
        System.out.println("filename = " + aURL.getFile());
        System.out.println("ref = " + aURL.getRef());

        System.out.println(link.toString());
    }
}
