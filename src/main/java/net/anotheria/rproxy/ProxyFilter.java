package net.anotheria.rproxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.cache.resources.ResourceCacheManager;
import net.anotheria.rproxy.cache.resources.bean.CacheableResource;
import net.anotheria.rproxy.cache.resources.bean.ResourceCacheKey;
import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.refactor.LocaleSpecialTarget;
import net.anotheria.rproxy.refactor.RProxy;
import net.anotheria.rproxy.refactor.RProxyFactory;
import net.anotheria.rproxy.refactor.SiteConfig;
import net.anotheria.rproxy.refactor.SiteHelper;
import net.anotheria.rproxy.refactor.URLHelper;
import net.anotheria.rproxy.replacement.AttrParser;
import net.anotheria.rproxy.utils.URLUtils;

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
            String siteNameLocale = siteName + "." + locale;
            SiteConfig siteConfig = getSiteConfig(siteName);

            if (hostExcluded(siteConfig, host)) {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            System.out.println(requestURL);
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
                    if(currentLocaleSpecRule == null){
                        targetPath = siteConfig.getTargetPath();
                    }else {
                        targetPath = currentLocaleSpecRule.getCustomTarget();
                    }
                    String queryString = httpServletRequest.getQueryString();
                    String path = originalPath.replaceAll("/" + siteName, "");
                    targetPath = prepareTargetPath(targetPath, path, queryString);
                    HttpProxyRequest httpProxyRequest = new HttpProxyRequest(targetPath);
                    /**
                     * .getSiteHelperMap().get(siteNameLocale) ==== siteNameLocale -> siteName
                     */

                    URLHelper source = null;
                    URLHelper target = null;

                    if (temp.get(siteNameLocale) == null) {
                        source = new URLHelper(proxy.getProxyConfig().getSiteHelperMap().get(siteName).getSourceUrlHelper(), locale);
                        if(currentLocaleSpecRule == null) {
                            target = proxy.getProxyConfig().getSiteHelperMap().get(siteName).getTargetUrlHelper();
                        }else {
                            target = new URLHelper(currentLocaleSpecRule.getCustomTarget());
                        }
                        temp.put(siteNameLocale, source);
                    } else {
                        source = temp.get(siteNameLocale);
                        if(currentLocaleSpecRule != null){
                            target = new URLHelper(targetPath);
                        }else {
                            target = proxy.getProxyConfig().getSiteHelperMap().get(siteName).getTargetUrlHelper();
                        }
                    }


                    prepareProxyRequestHeaders(httpProxyRequest, httpServletRequest, source, target);

                    HttpProxyResponse httpProxyResponse = getUrlContent(siteConfig, requestURL, siteName, httpProxyRequest);

                    if (httpProxyResponse != null) {
                        prepareHttpServletResponseNew(httpServletResponse, httpProxyResponse, siteName, locale, currentLocaleSpecRule);
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

    private HttpProxyResponse fetchUrlContent(SiteConfig siteConfig, HttpProxyRequest httpProxyRequest) throws IOException {
        if (siteConfig.getSiteCredentials() != null) {
            return HttpGetter.getUrlContent(httpProxyRequest, siteConfig.getSiteCredentials());
        }
        return HttpGetter.getUrlContent(httpProxyRequest);
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
        String storagePath = saveContentToFS(siteConfig, resourceUrl, ret.getData());
        cacheableResource = map(ret);
        cacheableResource.setStoragePath(storagePath);
        cacheManager.put(siteName, cacheKey, cacheableResource);

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
        if(value != null){
            String[] values = value.split("\\.");
            if(values.length > 0) {
                return "." + values[values.length - 1];
            }
        }

        return "";
    }

    private LocaleSpecialTarget hasSpecRule(SiteConfig siteConfig, String locale) {
        LocaleSpecialTarget[] rules = siteConfig.getLocaleSpecialTargets();
        if(rules == null || rules.length == 0){
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void headerForGzip(HttpServletResponse httpServletResponse) {
        httpServletResponse.addHeader("Content-Encoding", "gzip");
    }

    private boolean hostExcluded(SiteConfig siteConfig, String host) {
        if(siteConfig.getExcludeHosts() == null){
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
        if(siteConfig.getBaseLocales() == null){
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

    private void prepareHttpServletResponseNew(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse, String key, String locale, LocaleSpecialTarget rule) {
        /**
         * Ssl links in CSS files.
         */
        if (httpProxyResponse.isHtml() || httpProxyResponse.isCss()) {
            try {
                String oldData = new String(httpProxyResponse.getData(), httpProxyResponse.getContentEncoding());
                String newData = prepareProxyResponse(oldData, key, proxy.getProxyConfig().getSiteConfigMap().get(key), proxy.getProxyConfig().getSiteHelperMap().get(key), locale, rule);
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
        } catch (IOException e) {
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

    private String prepareProxyResponse(String data, String siteKey, SiteConfig siteConfig, SiteHelper siteHelper, String locale, LocaleSpecialTarget rule) {
        URLHelper temp = new URLHelper(siteHelper.getSourceUrlHelper(), locale);
        String path = siteConfig.getTargetPath();
        if(rule != null){
           path = rule.getCustomTarget();
        }
        if(data.contains("bg-red")){
            System.out.println("");
        }
        data = data.replaceAll(path, temp.getLink());
        data = data.replaceAll("href=\"/", "href=\"" + "/" + siteKey + "/");
        data = AttrParser.addSubFolderToRelativePathesInSrcSets(data, "/" + siteKey);
        data = data.replaceAll("src=\"/", "src=\"" + "/" + siteKey + "/");
        data = data.replaceAll("data-alt=\"/", "data-alt=\"" + "/" + siteKey + "/");
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