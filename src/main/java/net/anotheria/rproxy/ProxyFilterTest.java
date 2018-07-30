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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Monitor
public class ProxyFilterTest implements Filter {

    private static RProxy<String, HttpProxyResponse> proxy = new RProxy<>();
    private static String URL = null;

    private Map<String, URLHelper> temp = new HashMap<>();

    public void init(FilterConfig filterConfig) {

    }


    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try {

            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            String requestURL = httpServletRequest.getRequestURL().toString();
            String locale = URLUtils.getLocaleFromHost(new java.net.URL(requestURL).getHost());

            URL = requestURL;
            //System.out.println("Request URL : " + requestURL);
            String requestURLMD5 = URLUtils.getMD5Hash(requestURL);
            String siteName = URLUtils.getTopPath(requestURL);
            String siteNameLocale = siteName + "." + locale;

            String fileExtension = URLUtils.getFileExtensionFromPath(httpServletRequest.getPathInfo());

            if (proxy.siteConfigurationPresent(siteName)) {
                /**
                 * cache part must be changed bcs of locale
                 */
                if (proxy.retrieveFromCache(siteName, fileExtension, requestURLMD5) != null) {
                    HttpProxyResponse r = proxy.retrieveFromCache(siteName, fileExtension, requestURLMD5);
                    //proxy.appplyRedirects(httpServletResponse);
                    prepareHeadersForCaching(r, httpServletResponse);
                    //prepareHttpServletResponseFromCache(httpServletResponse, r);
                    doServletResponse(httpServletResponse, r);
                    //System.out.println(requestURL + " ++++++ from cache! " + requestURLMD5);
                } else {
                    String targetPath = proxy.getProxyConfig().getSiteConfigMap().get(siteName).getTargetPath();
                    String queryString = httpServletRequest.getQueryString();

                    String path = requestURL.replaceAll(proxy.getProxyConfig().getSiteConfigMap().get(siteName).getSourcePath(), "");
                    targetPath = prepareTargetPath(targetPath, path, queryString);
                    HttpProxyRequest httpProxyRequest = new HttpProxyRequest(targetPath);
                    /**
                     * .getSiteHelperMap().get(siteNameLocale) ==== siteNameLocale -> siteName
                     */

                    URLHelper source = null;
                    URLHelper target = null;

                    if(temp.get(siteNameLocale) == null){
                        source = new URLHelper(proxy.getProxyConfig().getSiteHelperMap().get(siteName).getSourceUrlHelper(), locale);
                        target = proxy.getProxyConfig().getSiteHelperMap().get(siteName).getTargetUrlHelper();
                        temp.put(siteNameLocale, source);
                    }else{
                        source = temp.get(siteNameLocale);
                        target = proxy.getProxyConfig().getSiteHelperMap().get(siteName).getTargetUrlHelper();
                    }

                    //SiteHelper currentSiteHelper = proxy.getProxyConfig().getSiteHelperMap().get(siteName);
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
                            prepareHeadersForCaching(httpProxyResponse, httpServletResponse);
                            //System.out.println("prepared after request ... !!!!!!!!!!!");
                            //proxy.addToCache(requestURLMD5, httpProxyResponse, siteName, fileExtension);
                            //System.out.println(fileExtension + " ->>>>>>>>> add to cache! " + requestURL);
                        }
                        doServletResponse(httpServletResponse, httpProxyResponse);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareHeadersForCaching(HttpProxyResponse httpProxyResponse, HttpServletResponse httpServletResponse) {
        for (String h : httpServletResponse.getHeaderNames()) {
            //System.out.println(" Hhhhhhhh ->> " + h + " + " + httpServletResponse.getHeader(h));
        }
        //System.out.println(httpProxyResponse.toString());
        for (Header h : httpProxyResponse.getHeaders()) {
            //System.out.println("-----" + h.getName() + " : " + h.getValue());
            if (h.getName().equalsIgnoreCase("expires") ||
                    h.getName().equalsIgnoreCase("last-modified") ||
                    h.getName().equalsIgnoreCase("etag") ||
                    h.getName().equalsIgnoreCase("content-encoding") ||
                    h.getName().equalsIgnoreCase("keep-alive") ||
                    h.getName().equalsIgnoreCase("cache-Control")) {
                httpServletResponse.addHeader(h.getName(), h.getValue());
            }
        }

    }

    private void prepareHttpServletResponseNew(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse, String key, String locale) {
        /**
         * Ssl links in CSS files.
         */
        if (httpProxyResponse.isHtml() || httpProxyResponse.isCss()) {
            //if (httpProxyResponse.isHtml()) {
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

            /**
             * permanent cache is cached in browser? -y
             * redirect by url?
             */
//            if (false && URL.endsWith("profil-bild-foto-upload/")) {
//                System.out.println(URL.endsWith("profil-bild-foto-upload/"));
//                //httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
//                //httpServletResponse.addHeader("Location", "http://localhost:8080/rproxy-test/");
//            }
//            System.out.println("=================================");
//
//            System.out.println("URL : " + URL);
//            for(String s : httpServletResponse.getHeaderNames()){
//                System.out.println("Response final : " + s + " -> " + httpServletResponse.getHeader(s));
//            }
            httpServletResponse.setContentType(httpProxyResponse.getContentType());
            httpServletResponse.getOutputStream().write(httpProxyResponse.getData());
            httpServletResponse.getOutputStream().flush();
            for (String h : httpServletResponse.getHeaderNames()) {
                //System.out.println(" After... ->> " + h + " + " + httpServletResponse.getHeader(h));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String prepareTargetPath(String targetPath, String path, String queryString) {
        //System.out.println(targetPath + " aaaaaaa");
        targetPath += path;
        if (queryString != null && queryString.length() > 0) {
            targetPath += "?" + queryString;
        }
        return targetPath;
    }

//    private String prepareProxyResponse(String data, String siteKey, Map<String, SiteConfig> siteConfigMap, Map<String, SiteHelper> siteHelperMap) {
//        data = data.replaceAll(siteConfigMap.get(siteKey).getTargetPath(), siteConfigMap.get(siteKey).getSourcePath());
//        data = data.replaceAll("href=\"/", "href=\"" + "/" + siteKey + "/");
//        //data = data.replaceAll("//", "/");
//        data = AttrParser.addSubFolderToRelativePathesInSrcSets(data, "/" + siteKey);
//        data = data.replaceAll("src=\"/", "src=\"" + "/" + siteKey + "/");
//        return data;
//    }
    private String prepareProxyResponse(String data, String siteKey, SiteConfig siteConfig, SiteHelper siteHelper, String locale) {
        URLHelper temp = new URLHelper(siteHelper.getSourceUrlHelper(), locale);
        data = data.replaceAll(siteConfig.getTargetPath(), temp.getLink());
        data = data.replaceAll("href=\"/", "href=\"" + "/" + siteKey + "/");
        //data = data.replaceAll("//", "/");
        data = AttrParser.addSubFolderToRelativePathesInSrcSets(data, "/" + siteKey);
        data = data.replaceAll("src=\"/", "src=\"" + "/" + siteKey + "/");
        return data;
    }

    private void prepareProxyRequestHeaders(HttpProxyRequest httpProxyRequest, HttpServletRequest httpServletRequest, URLHelper source, URLHelper target) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String hName = headerNames.nextElement();
            String hValue = httpServletRequest.getHeader(hName);
            //System.out.println(hName + " " + hValue);
            if (hName.equals("referer")) {
                //URLHelper source = siteHelper.getSourceUrlHelper();
                hValue = source.getProtocol() + "://" + source.getHost();
                if (source.getPort() != -1) {
                    hValue += source.getPort();
                }
                hValue += "/";
            }
            if (hName.equals("host")) {
                //URLHelper target = siteHelper.getTargetUrlHelper();
                hValue = target.getHost();
            }
            httpProxyRequest.addHeader(hName, hValue);
        }
    }

}