package net.anotheria.rproxy;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.refactor.ProxyConfig;
import net.anotheria.rproxy.refactor.SiteConfig;
import net.anotheria.rproxy.refactor.SiteHelper;
import net.anotheria.rproxy.refactor.URLHelper;
import net.anotheria.rproxy.refactor.cache.ICacheStrategy;
import net.anotheria.rproxy.refactor.cache.LRUStrategy;
import net.anotheria.rproxy.replacement.AttrParser;
import net.anotheria.rproxy.replacement.URLReplacementUtil;
import net.anotheria.rproxy.utils.URLUtils;
import org.configureme.ConfigurationManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;

@Monitor
public class TestFilter implements Filter {

    private static List<SiteConfig> siteConfigs;
    private static Map<String, SiteConfig> siteConfigMap = new HashMap<>();
    private static Map<String, SiteHelper> siteHelperMap = new HashMap<>();

    //private static Map<String, HttpProxyResponse> testCache = new HashMap<>();

    private static ICacheStrategy<String, HttpProxyResponse> lruCache = new LRUStrategy<>(50);

    public void init(FilterConfig filterConfig) {

        ProxyConfig proxyConfig = new ProxyConfig();
        ConfigurationManager.INSTANCE.configureAs(proxyConfig, "proxyConfig");

        siteConfigs = new LinkedList<>();
        for (String site : proxyConfig.getSites()) {
            SiteConfig sc = new SiteConfig();
            ConfigurationManager.INSTANCE.configureAs(sc, site);
            siteConfigs.add(sc);
            siteConfigMap.put(site, sc);
            SiteHelper siteHelper = new SiteHelper(new URLHelper(sc.getSourcePath()), new URLHelper(sc.getTargetPath()));
            siteHelperMap.put(site, siteHelper);
        }

        for (SiteConfig sc : siteConfigs)
            System.out.println(sc);
    }


    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try {
            if (!(servletRequest instanceof HttpServletRequest)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            String requestURL = httpServletRequest.getRequestURL().toString();

            System.out.println("Request URL : " + requestURL);

            //test lru cache
            if (lruCache.get(requestURL) != null) {
                System.out.println(requestURL + " ++++++ from cache!");
                //if (httpProxyResponse != null) {

                //test lru cache
                prepareHttpServletResponseFromCache(httpServletResponse, lruCache.get(requestURL));
                // }
            } else {
            String key = URLUtils.getTopPath(requestURL);
            if (key != null && siteConfigMap.get(key) != null) {
                String targetPath = siteConfigMap.get(key).getTargetPath();

                //request path should be cached
                String path = httpServletRequest.getPathInfo().replaceAll("/" + key, "");
                // path = httpServletRequest.getpa


                System.out.println("request path " + httpServletRequest.getPathInfo());
                String queryString = httpServletRequest.getQueryString();

                targetPath = prepareTargetPath(targetPath, path, queryString);
                System.out.println("Redirect URL : " + targetPath);
                HttpProxyRequest httpProxyRequest = new HttpProxyRequest(targetPath);
                //now we need to change headers for httpProxyRequest
                prepareProxyRequestHeaders(httpProxyRequest, httpServletRequest, key);

//                if (testCache.containsKey(requestURL) && testCache.get(requestURL) != null) {
//                    System.out.println(requestURL + " ++++++ from cache!");
//                    //if (httpProxyResponse != null) {
//                    prepareHttpServletResponseFromCache(httpServletResponse, testCache.get(requestURL));
//                    // }
//                } else {
//                    System.out.println(requestURL + " ----- new!");
//                    HttpProxyResponse httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest);
//
//                    if (httpProxyResponse != null) {
//                        prepareHttpServletResponseNew(httpServletResponse, httpProxyResponse, key);
//                    }
//                    testCache.put(requestURL, httpProxyResponse);
//                }


                    System.out.println(requestURL + " ----- new!");
                    HttpProxyResponse httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest);

                    if (httpProxyResponse != null) {
                        prepareHttpServletResponseNew(httpServletResponse, httpProxyResponse, key);
                    }
                    lruCache.add(requestURL, httpProxyResponse);
                }


            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareHttpServletResponseFromCache(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse) {
        try {
            doo(httpServletResponse, httpProxyResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareHttpServletResponseNew(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse, String key) {
        try {
            prepareProxyResponse(httpProxyResponse, key);
            doo(httpServletResponse, httpProxyResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doo(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse) throws IOException {
        //if (httpProxyResponse.getContentType() != null) {
            httpServletResponse.setContentType(httpProxyResponse.getContentType());
        //}
        httpServletResponse.getOutputStream().write(httpProxyResponse.getData());
        httpServletResponse.getOutputStream().flush();
    }

    private String prepareTargetPath(String targetPath, String path, String queryString) {
        targetPath += path;
        if (queryString != null && queryString.length() > 0) {
            targetPath += "?" + queryString;
        }
        return targetPath;
    }

    private void prepareProxyResponse(HttpProxyResponse httpProxyResponse, String siteKey) {
        try {
            if (httpProxyResponse.isHtml()) {
                SiteHelper siteHelper = siteHelperMap.get(siteKey);

                System.out.println(siteHelper);

                String data = new String(httpProxyResponse.getData(), httpProxyResponse.getContentEncoding());

                data = data.replaceAll(siteConfigMap.get(siteKey).getTargetPath(), siteConfigMap.get(siteKey).getSourcePath());

                data = data.replaceAll("href=\"/", "href=\"" + "/" + siteKey + "/");

                data = AttrParser.addSubFolderToRelativePathesInSrcSets(data, "/" + siteKey);

                data = data.replaceAll("src=\"/", "src=\"" + "/" + siteKey + "/");

                httpProxyResponse.setData(URLReplacementUtil.replace(
                        httpProxyResponse.getData(),
                        httpProxyResponse.getContentEncoding(),
                        siteHelper.getTargetUrlHelper().getHost(),
                        siteHelper.getSourceUrlHelper().getHost() + "/" + siteKey + "/"
                ));

                httpProxyResponse.setData(data.getBytes());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void prepareProxyRequestHeaders(HttpProxyRequest httpProxyRequest, HttpServletRequest httpServletRequest, String siteKey) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        SiteHelper siteHelper = siteHelperMap.get(siteKey);
        while (headerNames.hasMoreElements()) {
            String hName = headerNames.nextElement();
            String hValue = httpServletRequest.getHeader(hName);
            //System.out.println(hName + " ->>>> " + hValue);
            if (hName.equals("referer")) {
                //hValue = "http://localhost:8080/";
                URLHelper source = siteHelper.getSourceUrlHelper();
                hValue = source.getProtocol() + "://" + source.getHost();
                if (source.getPort() != -1) {
                    hValue += source.getPort();
                }
                hValue += "/";
                //hValue = siteHelperMap.get(siteKey).getSourceUrlHelper().get
                //hValue = hValue.replaceAll("hhtp://localhost:8080/test", "http://faq.thecasuallounge.ch");
                //hValue = StringUtils.replace(hValue, p.getMeSubFolder(), p.getBaseLink());
            }
            if (hName.equals("host")) {
                URLHelper target = siteHelper.getTargetUrlHelper();
                //hValue = "faq.thecasuallounge.ch";
                hValue = target.getHost();
            }
            //System.out.println("!!!! " + hName + " ->>>> " + hValue);
            httpProxyRequest.addHeader(hName, hValue);
        }
    }

}