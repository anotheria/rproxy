package net.anotheria.rproxy.utils;

import javax.servlet.http.HttpServletResponse;

public final class RewriteUtils {

    public static void permanentRedirect(HttpServletResponse httpServletResponse, String location){
        httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        httpServletResponse.addHeader("Location", location);
    }
}
