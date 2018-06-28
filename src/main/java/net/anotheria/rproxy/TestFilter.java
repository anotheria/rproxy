package net.anotheria.rproxy;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.refactor.ProxyConfig;
import net.anotheria.rproxy.refactor.SiteConfig;
import net.anotheria.rproxy.refactor.SiteHelper;
import net.anotheria.rproxy.refactor.URLHelper;
import net.anotheria.rproxy.refactor.cache.CacheStrategy;
import net.anotheria.rproxy.refactor.cache.CachingPolicy;
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

    private static Map<String, Map<String, ICacheStrategy<String, HttpProxyResponse>>> test = new HashMap<>();

    //private static Map<String, CachingPolicy> fileTypeCachingPolicyMap = new HashMap<>();

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

            if (!test.containsKey(site)) {
                Map<String, ICacheStrategy<String, HttpProxyResponse>> temp = new HashMap<>();
                test.put(site, temp);
            }

            if (sc.getCachingPolicy() != null) {
                for (CachingPolicy cachingPolicy : sc.getCachingPolicy()) {
                    for (String fileType : cachingPolicy.getFileType()) {
                        if (!test.get(site).containsKey(fileType)) {
                            test.get(site).put(fileType, cachingPolicy.getCacheStrategy().getConcreteStrategy());
                        }
                    }
                }
            }
        }

        for (SiteConfig sc : siteConfigs)
            System.out.println(sc);
    }


    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try {
//            if (!(servletRequest instanceof HttpServletRequest)) {
//                filterChain.doFilter(servletRequest, servletResponse);
//                return;
//            }

            //servletResponse.getOutputStream().flush();

            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            String requestURL = httpServletRequest.getRequestURL().toString();

            System.out.println("Request URL : " + requestURL);
            String key = URLUtils.getTopPath(requestURL);
            String[] ext = httpServletRequest.getPathInfo().split("\\.");
            String fileExtension = "";
            if (ext.length != 0) {
                fileExtension = "." + ext[ext.length - 1];
            }
            //test lru cache for concrete site
            if (siteConfigMap.containsKey(key)) {
                if (test.get(key).keySet().contains(fileExtension) && test.get(key).get(fileExtension).get(requestURL) != null) {
                    System.out.println(requestURL + " ++++++ from cache!");
                    prepareHttpServletResponseFromCache(httpServletResponse, test.get(key).get(fileExtension).get(requestURL));
                } else {
                    String targetPath = siteConfigMap.get(key).getTargetPath();
                    String path = httpServletRequest.getPathInfo().replaceAll("/" + key, "");

                    System.out.println("request path " + httpServletRequest.getPathInfo());
                    String queryString = httpServletRequest.getQueryString();

                    targetPath = prepareTargetPath(targetPath, path, queryString);
                    System.out.println("Redirect URL : " + targetPath);
                    HttpProxyRequest httpProxyRequest = new HttpProxyRequest(targetPath);
                    //now we need to change headers for httpProxyRequest
                    prepareProxyRequestHeaders(httpProxyRequest, httpServletRequest, key);


                    System.out.println(requestURL + " ----- loaded!");
                    HttpProxyResponse httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest);

                    if (httpProxyResponse != null) {
                        prepareHttpServletResponseNew(httpServletResponse, httpProxyResponse, key);
                    }
                    //lruCache.add(requestURL, httpProxyResponse);


                    System.out.println("File extension : " + fileExtension);
                    //System.out.println(test.keySet());
                    // System.out.println(test.get(key).keySet());
                    //System.out.println(test.get(key).get(fileExtension));
                    if (test.get(key).containsKey(fileExtension)) {
                        test.get(key).get(fileExtension).add(requestURL, httpProxyResponse);
                        System.out.println(fileExtension + " ->>>>>>>>> add to cache! " + requestURL);
                    }
                }

            }

//            if (lruCache.get(requestURL) != null) {
//                System.out.println(requestURL + " ++++++ from cache!");
//                //if (httpProxyResponse != null) {
//
//                //test lru cache
//                prepareHttpServletResponseFromCache(httpServletResponse, lruCache.get(requestURL));
//                // }
//            } else {
//
//                if (key != null && siteConfigMap.get(key) != null) {
//                    String targetPath = siteConfigMap.get(key).getTargetPath();
//
//                    //request path should be cached
//                    String path = httpServletRequest.getPathInfo().replaceAll("/" + key, "");
//                    // path = httpServletRequest.getpa
//
//
//                    System.out.println("request path " + httpServletRequest.getPathInfo());
//                    String queryString = httpServletRequest.getQueryString();
//
//                    targetPath = prepareTargetPath(targetPath, path, queryString);
//                    System.out.println("Redirect URL : " + targetPath);
//                    HttpProxyRequest httpProxyRequest = new HttpProxyRequest(targetPath);
//                    //now we need to change headers for httpProxyRequest
//                    prepareProxyRequestHeaders(httpProxyRequest, httpServletRequest, key);
//
//
//                    System.out.println(requestURL + " ----- new!");
//                    HttpProxyResponse httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest);
//
//                    if (httpProxyResponse != null) {
//                        prepareHttpServletResponseNew(httpServletResponse, httpProxyResponse, key);
//                    }
//                    lruCache.add(requestURL, httpProxyResponse);
//                }
//
//
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareHttpServletResponseFromCache(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse) {
        doo(httpServletResponse, httpProxyResponse);
    }

    private void prepareHttpServletResponseNew(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse, String key) {
        prepareProxyResponse(httpProxyResponse, key);
        doo(httpServletResponse, httpProxyResponse);
    }

    private void doo(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse) {
        //if (httpProxyResponse.getContentType() != null) {
        //System.out.println(httpProxyResponse.getContentType());

        //httpServletResponse.setCharacterEncoding(httpProxyResponse.getContentEncoding());
        //}
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

    private void prepareProxyResponse(HttpProxyResponse httpProxyResponse, String siteKey) {
        try {
            if (httpProxyResponse.isHtml()) {

                SiteHelper siteHelper = siteHelperMap.get(siteKey);

                //System.out.println(siteHelper);

                String data = new String(httpProxyResponse.getData(), httpProxyResponse.getContentEncoding());

                data = data.replaceAll(siteConfigMap.get(siteKey).getTargetPath(), siteConfigMap.get(siteKey).getSourcePath());

                data = data.replaceAll("href=\"/", "href=\"" + "/" + siteKey + "/");

                data = AttrParser.addSubFolderToRelativePathesInSrcSets(data, "/" + siteKey);

                data = data.replaceAll("src=\"/", "src=\"" + "/" + siteKey + "/");

                //data = data.replaceAll("src=\"/" + siteKey + "/" + siteKey, "src=\"" + "/" + siteKey);

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