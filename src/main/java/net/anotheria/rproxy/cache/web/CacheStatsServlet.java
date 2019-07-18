package net.anotheria.rproxy.cache.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.anotheria.moskito.core.predefined.CacheStats;
import net.anotheria.rproxy.cache.resources.ResourceCacheManager;

/**
 * @author
 */
public class CacheStatsServlet extends HttpServlet {

    private static final String TEXT_PLAIN = "text/plain";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        CacheStats cacheStats = ResourceCacheManager.getInstance().getStats();

        resp.setContentType(TEXT_PLAIN);
        resp.getOutputStream().write(cacheStats.toStatsString().getBytes());
        resp.getOutputStream().flush();

    }
}
