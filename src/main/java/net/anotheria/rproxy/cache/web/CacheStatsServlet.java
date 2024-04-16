package net.anotheria.rproxy.cache.web;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import net.anotheria.moskito.core.predefined.CacheStats;
import net.anotheria.rproxy.cache.resources.ResourceCacheManager;

/**
 * @author
 */
public class CacheStatsServlet extends HttpServlet {

    private static final String TEXT_PLAIN = "text/plain";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Map<String, CacheStats> cacheStats = ResourceCacheManager.getInstance().getStats();
        StringBuilder ret = new StringBuilder();
        for (Map.Entry<String, CacheStats> statsEntry : cacheStats.entrySet()) {
            ret.append("------------").append(statsEntry.getKey()).append("------------\n");
            ret.append(statsEntry.getValue().toStatsString()).append("\n\n");
        }

        resp.setContentType(TEXT_PLAIN);
        resp.getOutputStream().write(ret.toString().getBytes());
        resp.getOutputStream().flush();

    }
}
