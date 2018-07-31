package net.anotheria.rproxy.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class contains useful methods for interaction with URLs.
 */
public final class URLUtils {

    public static String getLocaleFromHost(String host) {
        String[] s = host.split("\\.");
        if (s.length == 0) {
            return null;
        }
        return s[s.length - 1];
    }

    /**
     * Gets file extension from URL path.
     *
     * @param path path to resource from URL
     * @return String file extension with dot at the beginning or empty string
     */
    public static String getFileExtensionFromPath(String path) {

        //System.out.println("!!!!!! ->> " + path + path.contains("."));
        if (path != null && path.contains(".")) {
            String[] parts = path.split("\\.");
            if (parts.length > 0) {
                return "." + parts[parts.length - 1];
            }
        }

        return "";
    }

    /**
     * Generates md5 hash of URL
     *
     * @param url to hash
     * @return MD5 hash
     */
    public static String getMD5Hash(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());

            byte byteData[] = md.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * TODO remove duplicate code in ProxyFilter.class
     *
     * @param url
     * @return
     */
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

    /**
     * Replaces locale in given String host
     * @param host i.e. www.site.de
     * @param locale i.e. de, ch, at etc...
     * @return host String with new locale if success, otherwise host String without changes
     */
    public static String replaceLocaleForHost(String host, String locale) {
        String[] s = host.split("\\.");

        if (s.length == 0 || s.length == 1) {
            return host;
        }

        s[s.length - 1] = locale;
        String res = s[0];
        for (int i = 1; i < s.length; i++) {
            res += "." + s[i];
        }
        return res;
    }
}
