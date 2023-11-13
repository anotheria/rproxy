package net.anotheria.rproxy;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.cache.resources.ResourceCacheManager;
import net.anotheria.rproxy.cache.resources.bean.CacheableResource;
import net.anotheria.rproxy.cache.resources.bean.ResourceCacheKey;
import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.refactor.*;
import net.anotheria.rproxy.replacement.AttrParser;
import net.anotheria.rproxy.utils.URLUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

@Monitor
public class ProxyFilter implements Filter {

    private RProxy<String, HttpProxyResponse> proxy = RProxyFactory.getInstance();
    private Map<String, URLHelper> temp = new HashMap<>();
    private Map<String, String> sitenameLocaleSpecialTargetRule = new HashMap<>();
    private static final String W3TC_MINIFY = "w3tc_minify";
    private ResourceCacheManager cacheManager = ResourceCacheManager.getInstance();

    public void init(FilterConfig filterConfig) {
    }


    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        filter(servletRequest, servletResponse, filterChain);
    }

    private void filter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try {

            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            String requestURL = httpServletRequest.getRequestURL().toString();
            String host = new java.net.URL(requestURL).getHost();
            String locale = URLUtils.getLocaleFromHost(host);

            //URL = requestURL;
            String requestURLMD5 = URLUtils.getMD5Hash(requestURL);
            String siteName = URLUtils.getTopPath(requestURL);

            SiteConfig siteConfig = getSiteConfig(siteName);
            HostLocaleMapping mappedHost = getHostMappingPresent(siteConfig, host);
            if (mappedHost != null) {
                locale = mappedHost.getLocale();
            }
            String siteNameLocale = siteName + "." + locale;
            if (hostExcluded(siteConfig, host)) {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String originalPath = new java.net.URL(requestURL).getPath();

            String fileExtension = URLUtils.getFileExtensionFromPath(originalPath);

//            if(fileExtension.equals("")){
//                fileExtension = probablyW3TotalCahcePluginHasMinifiedFile(httpServletRequest);
//            }

            if (proxy.siteConfigurationPresent(siteName)) {
                //if locale permitted
                if (!sourceLocaleIsPermited(siteConfig, locale)) {
                    //locale restricted, throw 404
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                //if has spec rule
                LocaleSpecialTarget currentLocaleSpecRule = hasSpecRule(siteConfig, locale);
                if (proxy.retrieveFromCache(siteName, fileExtension, requestURLMD5) != null) {
                    HttpProxyResponse r = proxy.retrieveFromCache(siteName, fileExtension, requestURLMD5);
                    prepareHeadersForCaching(r, httpServletResponse);
                    if (r.isGzip()) {
                        headerForGzip(httpServletResponse);
                    }
                    doServletResponse(httpServletResponse, r);
                } else {
                    String targetPath;
                    if (currentLocaleSpecRule == null) {
                        targetPath = siteConfig.getTargetPath();
                    } else {
                        targetPath = currentLocaleSpecRule.getCustomTarget();
                    }
                    String queryString = httpServletRequest.getQueryString();
                    String path = originalPath.replaceAll("/" + siteName, "");
                    /**
                     * In case we have path in configured target url, we need
                     * to access resources without this path
                     * (https://www.example.com/test/resource.png -> https://www.example.com/resource.png)
                     */
                    if (!fileExtension.equals("")) {
                        targetPath = URLUtils.removePathFromTarget(targetPath);
                    }
                    targetPath = prepareTargetPath(targetPath, path, queryString);
                    HttpProxyRequest httpProxyRequest = new HttpProxyRequest(targetPath);
                    /**
                     * .getSiteHelperMap().get(siteNameLocale) ==== siteNameLocale -> siteName
                     */

                    URLHelper source = null;
                    URLHelper target = null;

                    if (temp.get(siteNameLocale) == null) {
                        source = new URLHelper(proxy.getProxyConfig().getSiteHelperMap().get(siteName).getSourceUrlHelper(), locale);
                        if (currentLocaleSpecRule == null) {
                            target = proxy.getProxyConfig().getSiteHelperMap().get(siteName).getTargetUrlHelper();
                        } else {
                            target = new URLHelper(currentLocaleSpecRule.getCustomTarget());
                        }
                        temp.put(siteNameLocale, source);
                    } else {
                        source = temp.get(siteNameLocale);
                        if (currentLocaleSpecRule != null) {
                            target = new URLHelper(targetPath);
                        } else {
                            target = proxy.getProxyConfig().getSiteHelperMap().get(siteName).getTargetUrlHelper();
                        }
                    }


                    prepareProxyRequestHeaders(httpProxyRequest, httpServletRequest, source, target);

                    HttpProxyResponse httpProxyResponse = getUrlContent(siteConfig, requestURL, siteName, httpProxyRequest);

                    if (httpProxyResponse != null
                            && httpProxyResponse.getStatusCode() != 500) {
                        prepareHttpServletResponseNew(httpServletResponse, httpProxyResponse, siteName, locale, currentLocaleSpecRule, mappedHost);
                        if (!fileExtension.equals("")) {
                            addGzipEncoding(httpServletResponse, httpProxyResponse);
                            prepareHeadersForCaching(httpProxyResponse, httpServletResponse);
                        }
                        proxy.addToCache(requestURLMD5, httpProxyResponse, siteName, fileExtension);
                        doServletResponse(httpServletResponse, httpProxyResponse);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HostLocaleMapping getHostMappingPresent(SiteConfig siteConfig, String host) {
        if (siteConfig != null && siteConfig.getHostLocaleMapping() != null) {
            for (HostLocaleMapping mappedHost : siteConfig.getHostLocaleMapping()) {
                if (mappedHost.getHost() != null && mappedHost.getHost().equals(host)) {
                    return mappedHost;
                }
            }
        }
        return null;
    }

    private HttpProxyResponse fetchUrlContent(SiteConfig siteConfig, HttpProxyRequest httpProxyRequest) throws IOException {
        if (siteConfig.getSiteCredentials() != null) {
            return HttpGetter.getUrlContent(httpProxyRequest, siteConfig);
        }
        return HttpGetter.getUrlContent(httpProxyRequest, siteConfig);
    }

    private HttpProxyResponse getUrlContent(SiteConfig siteConfig, String resourceUrl, String siteName, HttpProxyRequest httpProxyRequest) throws IOException {
        if (!isCacheable(siteConfig, resourceUrl)) {
            return fetchUrlContent(siteConfig, httpProxyRequest);
        }

        ResourceCacheKey cacheKey = new ResourceCacheKey(resourceUrl);
        CacheableResource cacheableResource = cacheManager.get(siteName, cacheKey);
        if (cacheableResource != null) {
            try {
                HttpProxyResponse ret = map(cacheableResource);

                byte[] content = FileUtils.readFileToByteArray(new File(cacheableResource.getStoragePath()));
                ret.setData(content);
                return ret;
            } catch (FileNotFoundException e) {
                //ignored
            }
        }

        HttpProxyResponse ret = fetchUrlContent(siteConfig, httpProxyRequest);
        try {
            String storagePath = saveContentToFS(siteConfig, resourceUrl, ret.getData());
            cacheableResource = map(ret);
            cacheableResource.setStoragePath(storagePath);
            cacheManager.put(siteName, cacheKey, cacheableResource);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private HttpProxyResponse map(CacheableResource instance) {
        if (instance == null) {
            return null;
        }

        HttpProxyResponse ret = new HttpProxyResponse();

        ret.setStatusCode(instance.getStatusCode());
        ret.setStatusMessage(instance.getStatusMessage());
        ret.setContentType(instance.getContentType());
        ret.setHeaders(instance.getHeaders());
        ret.setGzip(instance.isGzip());

        return ret;
    }

    private CacheableResource map(HttpProxyResponse instance) {
        if (instance == null) {
            return null;
        }

        CacheableResource ret = new CacheableResource();

        ret.setStatusCode(instance.getStatusCode());
        ret.setStatusMessage(instance.getStatusMessage());
        ret.setContentType(instance.getContentType());
        ret.setHeaders(instance.getHeaders());
        ret.setGzip(instance.isGzip());

        return ret;
    }

    private String saveContentToFS(SiteConfig siteConfig, String uri, byte[] body) throws IOException {
        String storagePath = siteConfig.getCacheableResourcesFsStoragePath();
        File file = new File(storagePath + File.separator + new URL(uri).getFile());
        FileUtils.writeByteArrayToFile(file, body);

        return file.getPath();
    }

    private SiteConfig getSiteConfig(String siteName) {
        return proxy.getProxyConfig().getSiteConfigMap().get(siteName);
    }

    private boolean isCacheable(SiteConfig siteConfig, String resourceUrl) {
        if (StringUtils.isBlank(resourceUrl)) {
            return false;
        }

        boolean ret = false;
        for (String value : siteConfig.getExcludedCecheableResourcesSuffix()) {
            if (value.equals(SiteConfig.ALL) || resourceUrl.endsWith(value)) {
                return false;
            }
        }

        for (String value : siteConfig.getCacheableResourcesSuffix()) {
            if (value.equals(SiteConfig.ALL) || resourceUrl.endsWith(value)) {
                ret = true;
                break;
            }
        }

        return ret;
    }

    private String probablyW3TotalCahcePluginHasMinifiedFile(HttpServletRequest httpServletRequest) {
        String value = httpServletRequest.getParameter(W3TC_MINIFY);
        if (value != null) {
            String[] values = value.split("\\.");
            if (values.length > 0) {
                return "." + values[values.length - 1];
            }
        }

        return "";
    }

    private LocaleSpecialTarget hasSpecRule(SiteConfig siteConfig, String locale) {
        LocaleSpecialTarget[] rules = siteConfig.getLocaleSpecialTargets();
        if (rules == null || rules.length == 0) {
            return null;
        }
        for (LocaleSpecialTarget rule : rules) {
            if (rule.getLocale().equals(locale)) {
                return rule;
            }
        }
        return null;
    }

    private void addGzipEncoding(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse) {
        try {
            OutputStream outputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
            gzip.write(httpProxyResponse.getData());
            gzip.close();
            httpProxyResponse.setData(((ByteArrayOutputStream) outputStream).toByteArray());
            httpProxyResponse.setGzip(true);
            headerForGzip(httpServletResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void headerForGzip(HttpServletResponse httpServletResponse) {
        httpServletResponse.addHeader("Content-Encoding", "gzip");
    }

    private boolean hostExcluded(SiteConfig siteConfig, String host) {
        if (siteConfig.getExcludeHosts() == null) {
            return false;
        }
        for (String h : siteConfig.getExcludeHosts()) {
            if (host.equals(h)) {
                return true;
            }
        }

        return false;
    }

    private boolean sourceLocaleIsPermited(SiteConfig siteConfig, String locale) {
        if (siteConfig.getBaseLocales() == null) {
            return true;
        }
        final String[] locales = siteConfig.getBaseLocales();
        for (String loc : locales) {
            if (loc.equals(locale)) {
                return true;
            }
        }
        return false;
    }

    private void prepareHeadersForCaching(HttpProxyResponse httpProxyResponse, HttpServletResponse httpServletResponse) {
        for (Header h : httpProxyResponse.getHeaders()) {
            if (h.getName().equalsIgnoreCase("expires") ||
                    h.getName().equalsIgnoreCase("last-modified") ||
                    h.getName().equalsIgnoreCase("etag") ||
                    h.getName().equalsIgnoreCase("content-encoding") ||
                    h.getName().equalsIgnoreCase("keep-alive") ||
                    h.getName().equalsIgnoreCase("cache-Control") ||
                    h.getName().equalsIgnoreCase("vary")) {
                httpServletResponse.addHeader(h.getName(), h.getValue());
            }
        }
    }

    private void prepareHttpServletResponseNew(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse, String key, String locale, LocaleSpecialTarget rule, HostLocaleMapping mapping) {
        /**
         * Ssl links in CSS files.
         */
        if (httpProxyResponse.isHtml() || httpProxyResponse.isCss()) {
            try {
                String oldData = new String(httpProxyResponse.getData(), httpProxyResponse.getContentEncoding());
                String newData = prepareProxyResponse(oldData, key, proxy.getProxyConfig().getSiteConfigMap().get(key), proxy.getProxyConfig().getSiteHelperMap().get(key), locale, rule, mapping);
                httpProxyResponse.setData(newData.getBytes());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void doServletResponse(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse) {
        try {
            httpServletResponse.setContentType(httpProxyResponse.getContentType());
            httpServletResponse.getOutputStream().write(httpProxyResponse.getData());
            httpServletResponse.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String prepareTargetPath(String targetPath, String path, String queryString) {
        targetPath += path;
        if (queryString != null && queryString.length() > 0) {
            targetPath += "?" + queryString;
        }
        return targetPath;
    }

    private String prepareProxyResponse(String data, String siteKey, SiteConfig siteConfig, SiteHelper siteHelper, String locale, LocaleSpecialTarget rule, HostLocaleMapping mapping) {
        URLHelper temp = new URLHelper(siteHelper.getSourceUrlHelper(), locale);
        String sourceURL = temp.getLink();
        String path = URLUtils.removePathFromTarget(siteConfig.getTargetPath());
        if (rule != null) {
            path = rule.getCustomTarget();
        }
        if (mapping != null && mapping.getHost() != null) {
            sourceURL = "https://" + mapping.getHost() + "/" + siteKey;
        }
        data = data.replaceAll(path, sourceURL);
        if (path.startsWith("http:")) {
            data = data.replaceAll(path.replace("http:", "https:"), sourceURL);
        } else {
            data = data.replaceAll(path.replace("https:", "http:"), sourceURL);
        }
        data = data.replaceAll("href=\"/", "href=\"" + "/" + siteKey + "/");
        //same as for common href but without quotes
        data = data.replaceAll("href=/", "href=" + "/" + siteKey + "/");
        data = AttrParser.addSubFolderToRelativePathesInSrcSets(data, "/" + siteKey);
        data = data.replaceAll("src=\"/", "src=\"" + "/" + siteKey + "/");
        //same as for common src but without quotes
        data = data.replaceAll("src=/", "src=" + "/" + siteKey + "/");
        data = data.replaceAll("data-alt=", "data-alt=" + "/" + siteKey);
        data = data.replaceAll("value=\"/", "value=\"" + "/" + siteKey + "/");
        data = data.replaceAll("action=\"/", "action=\"" + "/" + siteKey + "/");
        return data;
    }

    private void prepareProxyRequestHeaders(HttpProxyRequest httpProxyRequest, HttpServletRequest httpServletRequest, URLHelper source, URLHelper target) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String hName = headerNames.nextElement();
            String hValue = httpServletRequest.getHeader(hName);
            if (hName.equals("referer")) {
                hValue = source.getProtocol() + "://" + source.getHost();
                if (source.getPort() != -1) {
                    hValue += source.getPort();
                }
                hValue += "/";
            }
            if (hName.equals("host")) {
                hValue = target.getHost();
            }
            httpProxyRequest.addHeader(hName, hValue);
        }
    }

}
