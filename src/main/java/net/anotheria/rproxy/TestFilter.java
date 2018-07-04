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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;

@Monitor
public class TestFilter implements Filter {

    private static RProxy<String, HttpProxyResponse> proxy = new RProxy<>();

    public void init(FilterConfig filterConfig) {

    }


    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try {

            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            String requestURL = httpServletRequest.getRequestURL().toString();

            System.out.println("Request URL : " + requestURL);
            String siteName = URLUtils.getTopPath(requestURL);
            String[] ext = httpServletRequest.getPathInfo().split("\\.");
            String fileExtension = "";
            if (ext.length != 0) {
                fileExtension = "." + ext[ext.length - 1];
            }
//================
            if(proxy.siteConfigurationPresent(siteName)){
                if(proxy.retrieveFromCache(siteName, fileExtension, requestURL) != null){
                    HttpProxyResponse r = proxy.retrieveFromCache(siteName, fileExtension, requestURL);
                    prepareHttpServletResponseFromCache(httpServletResponse, r);
                    System.out.println(requestURL + " ++++++ from cache!");
                }else {
                    String targetPath = proxy.getProxyConfig().getSiteConfigMap().get(siteName).getTargetPath();
                    String queryString = httpServletRequest.getQueryString();

                    String path = requestURL.replaceAll(proxy.getProxyConfig().getSiteConfigMap().get(siteName).getSourcePath(), "");
                    targetPath = prepareTargetPath(targetPath, path, queryString);
                    HttpProxyRequest httpProxyRequest = new HttpProxyRequest(targetPath);
                    prepareProxyRequestHeaders(httpProxyRequest, httpServletRequest, proxy.getProxyConfig().getSiteHelperMap().get(siteName));

                    HttpProxyResponse httpProxyResponse = null;
                    if (proxy.getProxyConfig().getSiteConfigMap().get(siteName).getSiteCredentials() != null) {
                        httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest, proxy.getProxyConfig().getSiteConfigMap().get(siteName).getSiteCredentials());
                    } else {
                        httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest);
                    }

                    if (httpProxyResponse != null) {
                        prepareHttpServletResponseNew(httpServletResponse, httpProxyResponse, siteName);
                        proxy.addToCache(requestURL, httpProxyResponse, siteName, fileExtension);
                        System.out.println(fileExtension + " ->>>>>>>>> add to cache! " + requestURL);
                    }
                }
            }
//================
//
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareHttpServletResponseFromCache(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse) {
        doServletResponse(httpServletResponse, httpProxyResponse);
    }

    private void prepareHttpServletResponseNew(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse, String key) {
        if(httpProxyResponse.isHtml()) {
            try {
                String oldData = new String(httpProxyResponse.getData(), httpProxyResponse.getContentEncoding());
                String newData = prepareProxyResponse(oldData, key, proxy.getProxyConfig().getSiteConfigMap());
                httpProxyResponse.setData(newData.getBytes());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        doServletResponse(httpServletResponse, httpProxyResponse);
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

    private String prepareProxyResponse(String data, String siteKey, Map<String, SiteConfig> siteConfigMap) {
        data = data.replaceAll(siteConfigMap.get(siteKey).getTargetPath(), siteConfigMap.get(siteKey).getSourcePath());
        data = data.replaceAll("href=\"/", "href=\"" + "/" + siteKey + "/");
        data = AttrParser.addSubFolderToRelativePathesInSrcSets(data, "/" + siteKey);
        data = data.replaceAll("src=\"/", "src=\"" + "/" + siteKey + "/");
        return data;
    }

    private void prepareProxyRequestHeaders(HttpProxyRequest httpProxyRequest, HttpServletRequest httpServletRequest, SiteHelper siteHelper) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String hName = headerNames.nextElement();
            String hValue = httpServletRequest.getHeader(hName);
            if (hName.equals("referer")) {
                URLHelper source = siteHelper.getSourceUrlHelper();
                hValue = source.getProtocol() + "://" + source.getHost();
                if (source.getPort() != -1) {
                    hValue += source.getPort();
                }
                hValue += "/";
            }
            if (hName.equals("host")) {
                URLHelper target = siteHelper.getTargetUrlHelper();
                hValue = target.getHost();
            }
            httpProxyRequest.addHeader(hName, hValue);
        }
    }

}