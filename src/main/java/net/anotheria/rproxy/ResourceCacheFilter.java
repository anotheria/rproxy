package net.anotheria.rproxy;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.cache.resources.ResourceCacheConfig;
import net.anotheria.rproxy.cache.resources.ResourceCacheManager;
import net.anotheria.rproxy.cache.resources.bean.CacheableResource;
import net.anotheria.rproxy.cache.resources.bean.ResourceCacheKey;

@Monitor
public class ResourceCacheFilter implements Filter {

    private ResourceCacheConfig config = new ResourceCacheConfig();
    private ResourceCacheManager cacheManager = ResourceCacheManager.getInstance();

    private static final Logger LOG = LoggerFactory.getLogger(ResourceCacheFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private boolean isCacheable(String resourceUrl) {
        if (StringUtils.isBlank(resourceUrl)) {
            return false;
        }

        boolean ret = false;

        for (String value : config.getExcludedResourcesSuffix()) {
            if (value.equals(ResourceCacheConfig.ALL) || resourceUrl.endsWith(value)) {
                return false;
            }
        }

        for (String value : config.getCacheableResourcesSuffix()) {
            if (value.equals(ResourceCacheConfig.ALL) || resourceUrl.endsWith(value)) {
                ret = true;
                break;
            }
        }

        return ret;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        String requestUrl = req.getRequestURL().toString();

        if (!isCacheable(requestUrl)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        CacheableResource cacheableResource = cacheManager.get(new ResourceCacheKey(requestUrl));
        if (cacheableResource == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //handle return type, only write out on wrong return type.
        res.setContentType(cacheableResource.getContentType());
        res.getOutputStream().write(FileUtils.readFileToByteArray(new File(cacheableResource.getStoragePath())));
        res.getOutputStream().flush();
    }

    @Override
    public void destroy() {
    }

}