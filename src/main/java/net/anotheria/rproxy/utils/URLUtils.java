package net.anotheria.rproxy.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class contains useful methods for interaction with URLs.
 */
public final class URLUtils {

    public static String getTopPath(String url) {
        if (url == null) {
            return null;
        }

        try {
            URL u = new URL(url);
            String path = u.getPath();
            String[] pathParts = path.split("/");
            String res = null;
            for (String part : pathParts) {
                if (part != null && !part.equals("")) {
                    res = part;
                    break;
                }
            }
            return res;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
