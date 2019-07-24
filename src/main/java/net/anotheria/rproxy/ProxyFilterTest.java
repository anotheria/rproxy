package net.anotheria.rproxy;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.refactor.*;
import net.anotheria.rproxy.replacement.AttrParser;
import net.anotheria.rproxy.utils.URLUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeader;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

@Monitor
public class ProxyFilterTest implements Filter {

    private static RProxy<String, HttpProxyResponse> proxy = new RProxy<>();
    private Map<String, URLHelper> temp = new HashMap<>();
    private Map<String, String> sitenameLocaleSpecialTargetRule = new HashMap<>();
    private static final String W3TC_MINIFY = "w3tc_minify";

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

            if (hostExcluded(host, siteName)) {
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
                if (!sourceLocaleIsPermited(siteName, locale)) {
                    //locale restricted, throw 404
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                //if has spec rule
                LocaleSpecialTarget currentLocaleSpecRule = hasSpecRule(siteName, locale);
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
                        targetPath = proxy.getProxyConfig().getSiteConfigMap().get(siteName).getTargetPath();
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

                    HttpProxyResponse httpProxyResponse = null;
                    if (proxy.getProxyConfig().getSiteConfigMap().get(siteName).getSiteCredentials() != null) {
                        httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest, proxy.getProxyConfig().getSiteConfigMap().get(siteName).getSiteCredentials());
                    } else {
                        httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest);
                    }

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

    private LocaleSpecialTarget hasSpecRule(String siteName, String locale) {
        LocaleSpecialTarget[] rules = proxy.getProxyConfig().getSiteConfigMap().get(siteName).getLocaleSpecialTargets();
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

    private boolean hostExcluded(String host, String siteName) {
        if(proxy.getProxyConfig().getSiteConfigMap().get(siteName).getExcludeHosts() == null){
            return false;
        }
        for (String h : proxy.getProxyConfig().getSiteConfigMap().get(siteName).getExcludeHosts()) {
            if (host.equals(h)) {
                return true;
            }
        }

        return false;
    }

    private boolean sourceLocaleIsPermited(String siteName, String locale) {
        if(proxy.getProxyConfig().getSiteConfigMap().get(siteName).getBaseLocales() == null){
            return true;
        }
        final String[] locales = proxy.getProxyConfig().getSiteConfigMap().get(siteName).getBaseLocales();
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