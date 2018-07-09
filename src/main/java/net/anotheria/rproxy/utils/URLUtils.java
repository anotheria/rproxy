package net.anotheria.rproxy.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class contains useful methods for interaction with URLs.
 */
public final class URLUtils {


    /**
     * Gets file extension from URL path.
     *
     * @param path path to resource from URL
     * @return String file extension with dot at the beginning or null
     */
    public static String getFileExtensionFromPath(String path) {

        if (path != null) {
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

}
