package net.anotheria.rproxy;

import java.net.MalformedURLException;
import java.net.URL;

public class ProxyTest {

    /**
     * So we need
     * http://www.thecasuallounge.ch/faq/kuendigung/
     * from
     * http://faq.thecasuallounge.ch/kuendigung/
     *
     * i.e. host + subDomain2 + path2.
     * @param args
     */
    public static void main(String... args) throws MalformedURLException {

        new LinkEntity("https://www.stackoverflow.com/questions/16702357/how-to-replace-a-substring-of-a-string");
        new LinkEntity("http://localhost:1929//test");
    }
}
