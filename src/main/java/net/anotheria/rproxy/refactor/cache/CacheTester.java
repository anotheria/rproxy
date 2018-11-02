package net.anotheria.rproxy.refactor.cache;

import net.anotheria.rproxy.utils.URLUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class CacheTester {

    public static void main(String... args) throws MalformedURLException {

        String[] url = new String[]{
                "https://oss.sonatype.org/content/repositories/snapshots/net/anotheria/rproxy/1.0-SNAPSHOT/maven-metadata.xml",
                "http://www.cms.tcl.ch/faq",
                "https://www.cms.tcl.ch/faq/semi-colon/"
        };

        for (String s : url) {
            String e = URLUtils.getFileExtensionFromPath(new URL(s).getPath());
            System.out.println(e);
        }

//        ICacheStrategy<Integer, String> cache = new LRUStrategyImpl<>(3);
//
//        String[] names = new String[] {"Alina", "Ivan", "Oleg", "Grisha", "Vasja", "Lolka"};
//
//        for(int i=1; i<names.length+1; i++){
//            cache.add(i, names[i-1]);
//            System.out.println("added " + names[i-1]);
//            cache.printElements();
//            //break;
//        }
//
//        System.out.println(cache.get(5));
//        cache.printElements();
//
//        System.out.println(cache.get(4));
//        cache.printElements();
//
//        cache.remove(6);
//        cache.printElements();
//
//        cache.add(1, "test");
//
//        cache.printElements();
//
//        for(Integer k : cache.getAllElements().keySet()){
//            System.out.println(k + " -> " + cache.get(k));
//        }

    }
}
