package net.anotheria.rproxy;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.refactor.RProxy;
import net.anotheria.rproxy.refactor.SiteConfig;
import net.anotheria.rproxy.refactor.SiteHelper;
import net.anotheria.rproxy.refactor.URLHelper;
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

//@Monitor
public class ProxyFilterTest implements Filter {

    private static RProxy<String, HttpProxyResponse> proxy = new RProxy<>();
    //private static String URL = null;

    private Map<String, URLHelper> temp = new HashMap<>();

    private Map<String, String> permitedSourceLocales = new HashMap<>();

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

            if (proxy.siteConfigurationPresent(siteName)) {
                if (proxy.retrieveFromCache(siteName, fileExtension, requestURLMD5) != null) {
                    HttpProxyResponse r = proxy.retrieveFromCache(siteName, fileExtension, requestURLMD5);
                    prepareHeadersForCaching(r, httpServletResponse);
                    if(r.isGzip()){
                        headerForGzip(httpServletResponse);
                    }
                    doServletResponse(httpServletResponse, r);
                } else {
                    String targetPath = proxy.getProxyConfig().getSiteConfigMap().get(siteName).getTargetPath();
                    String queryString = httpServletRequest.getQueryString();
                    String path = originalPath.replaceAll("/" + siteName, "");
                    targetPath = prepareTargetPath(targetPath, path, queryString);
                    HttpProxyRequest httpProxyRequest = new HttpProxyRequest(targetPath);
                    /**
                     * .getSiteHelperMap().get(siteNameLocale) ==== siteNameLocale -> siteName
                     */

                    URLHelper source = null;
                    URLHelper target = null;

                    if (!sourceLocaleIsPermited(siteName, locale)) {
                        //locale restricted, throw 404
                        httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    } else {
                        if (temp.get(siteNameLocale) == null) {
                            source = new URLHelper(proxy.getProxyConfig().getSiteHelperMap().get(siteName).getSourceUrlHelper(), locale);
                            target = proxy.getProxyConfig().getSiteHelperMap().get(siteName).getTargetUrlHelper();
                            temp.put(siteNameLocale, source);
                        } else {
                            source = temp.get(siteNameLocale);
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
                        prepareHttpServletResponseNew(httpServletResponse, httpProxyResponse, siteName, locale);
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

    private void headerForGzip(HttpServletResponse httpServletResponse){
        httpServletResponse.addHeader("Content-Encoding", "gzip");
    }

    private boolean hostExcluded(String host, String siteName) {
        for (String h : proxy.getProxyConfig().getSiteConfigMap().get(siteName).getExcludeHosts()) {
            if (host.equals(h)) {
                return true;
            }
        }

        return false;
    }

    private boolean sourceLocaleIsPermited(String siteName, String locale) {
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

    private void prepareHttpServletResponseNew(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse, String key, String locale) {
        /**
         * Ssl links in CSS files.
         */
        if (httpProxyResponse.isHtml() || httpProxyResponse.isCss()) {
            try {
                String oldData = new String(httpProxyResponse.getData(), httpProxyResponse.getContentEncoding());
                String newData = prepareProxyResponse(oldData, key, proxy.getProxyConfig().getSiteConfigMap().get(key), proxy.getProxyConfig().getSiteHelperMap().get(key), locale);
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

    private String prepareProxyResponse(String data, String siteKey, SiteConfig siteConfig, SiteHelper siteHelper, String locale) {
        URLHelper temp = new URLHelper(siteHelper.getSourceUrlHelper(), locale);
        data = data.replaceAll(siteConfig.getTargetPath(), temp.getLink());
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