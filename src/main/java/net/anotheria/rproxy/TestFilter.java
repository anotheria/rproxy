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
import net.anotheria.rproxy.replacement.AttrParser;
import net.anotheria.rproxy.replacement.URLReplacementUtil;
import net.anotheria.rproxy.utils.URLUtils;
import org.configureme.ConfigurationManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;

@Monitor
public class TestFilter implements Filter {

    //private static List<SiteConfig> siteConfigs;
    private static Map<String, SiteConfig> siteConfigMap;
    private static Map<String, SiteHelper> siteHelperMap;

    private static Map<String, Map<String, ICacheStrategy<String, HttpProxyResponse>>> cache;

    public void init(FilterConfig filterConfig) {

        ProxyConfig<String, HttpProxyResponse> proxyConfig = new ProxyConfig<>();
        ConfigurationManager.INSTANCE.configureAs(proxyConfig, "proxyConfig");

        siteConfigMap = proxyConfig.getSiteConfigMap();
        siteHelperMap = proxyConfig.getSiteHelperMap();
        cache = proxyConfig.getCache();
        //siteConfigs = new LinkedList<>();
//        for (String site : proxyConfig.getSites()) {
//            SiteConfig sc = new SiteConfig();
//            ConfigurationManager.INSTANCE.configureAs(sc, site);
//            //siteConfigs.add(sc);
//            siteConfigMap.put(site, sc);
//            SiteHelper siteHelper = new SiteHelper(new URLHelper(sc.getSourcePath()), new URLHelper(sc.getTargetPath()));
//            siteHelperMap.put(site, siteHelper);
//            //-------------
//
//
//            if (sc.getCachingPolicy() != null && sc.getCachingPolicy().getCacheStrategy() != null) {
//                //get one instance of CacheStrategy for each site
//                //configure it
//                IConfig curConfig = CacheStrategyConfigurer.getByStrategyEnumAndConfigName(sc.getCachingPolicy().getCacheStrategy().getName(), sc.getCachingPolicy().getCacheStrategy().getConfigName());
//                if (curConfig != null) {
//                    if (cache.get(site) == null) {
//                        Map<String, ICacheStrategy<String, HttpProxyResponse>> tmp = new HashMap<>();
//                        cache.put(site, tmp);
//                    }
//                    ICacheStrategy<String, HttpProxyResponse> cacheInstance = new CacheConfigurer<String, HttpProxyResponse>().configureLRU(curConfig);
//                    for (String fileType : sc.getCachingPolicy().getFileType()) {
//                        cache.get(site).put(fileType, cacheInstance);
//                    }
//                }
//            }
//        }
    }


    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try {

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

            if (siteConfigMap.containsKey(key)) {
                if (cache.containsKey(key) && cache.get(key).keySet().contains(fileExtension) && cache.get(key).get(fileExtension).get(requestURL) != null) {
                    System.out.println(requestURL + " ++++++ from cache!");
                    prepareHttpServletResponseFromCache(httpServletResponse, cache.get(key).get(fileExtension).get(requestURL));
                } else {
                    String targetPath = siteConfigMap.get(key).getTargetPath();
                    String queryString = httpServletRequest.getQueryString();

                    String path = requestURL.replaceAll(siteConfigMap.get(key).getSourcePath(), "");
                    targetPath = prepareTargetPath(targetPath, path, queryString);
                    HttpProxyRequest httpProxyRequest = new HttpProxyRequest(targetPath);
                    prepareProxyRequestHeaders(httpProxyRequest, httpServletRequest, key);

                    //System.out.println("Credentials : " + siteConfigMap.get(key).getSiteCredentials() + " for site " + key);
                    HttpProxyResponse httpProxyResponse = null;
                    if (siteConfigMap.get(key).getSiteCredentials() != null) {
                        httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest, siteConfigMap.get(key).getSiteCredentials());
                    } else {
                        httpProxyResponse = HttpGetter.getUrlContent(httpProxyRequest);
                    }

                    if (httpProxyResponse != null) {
                        prepareHttpServletResponseNew(httpServletResponse, httpProxyResponse, key);
                    }

                    if (cache.containsKey(key) && cache.get(key).containsKey(fileExtension)) {
                        cache.get(key).get(fileExtension).add(requestURL, httpProxyResponse);
                        System.out.println(fileExtension + " ->>>>>>>>> add to cache! " + requestURL);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareHttpServletResponseFromCache(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse) {
        doServletResonse(httpServletResponse, httpProxyResponse);
    }

    private void prepareHttpServletResponseNew(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse, String key) {
        prepareProxyResponse(httpProxyResponse, key);
        doServletResonse(httpServletResponse, httpProxyResponse);
    }

    private void doServletResonse(HttpServletResponse httpServletResponse, HttpProxyResponse httpProxyResponse) {
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