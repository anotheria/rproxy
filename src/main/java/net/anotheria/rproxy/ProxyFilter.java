package net.anotheria.rproxy;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.replacement.URLReplacementUtil;
import net.anotheria.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Monitor
public class ProxyFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyFilter.class);

    private List<Rule> rules;
    private List<ProxyHelper> helpers;

    public void init(FilterConfig filterConfig) {


        String[] baseUrls = filterConfig.getInitParameter("BaseURL").split(",");
        URL host = null;
        String hostMe = null;
        String hostProtocol = null;
        try {
            host = new URL(filterConfig.getInitParameter("HostURL"));
            hostMe = host.getHost();
            hostProtocol = host.getProtocol() + "://";
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        List<ProxyHelper> allProxyHelper = new LinkedList<>();

        for (String baseUrl : baseUrls) {
            URL base = null;

            try {
                base = new URL(baseUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            ProxyHelper p = getProxyInstance(hostMe, hostProtocol, base);
            allProxyHelper.add(p);

        }

        String ruleParam = filterConfig.getInitParameter("RuleURL");
        if(ruleParam != null){
            this.rules = parseRules(ruleParam.split(","), allProxyHelper);
        }
        this.helpers = new LinkedList<>(allProxyHelper);
    }



    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {

        try {
            if (!(servletRequest instanceof HttpServletRequest)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            HttpServletRequest req = (HttpServletRequest) servletRequest;
            HttpServletResponse res = (HttpServletResponse) servletResponse;

            String path = req.getRequestURI();
            String appUrl = req.getRequestURL().toString();

            //URL u = new URL(path);
            //System.out.println("app url --------------->" + req.getRequestURL());
            //System.out.println(u.toString());

            //top domain

            URL u = new URL(appUrl);

            //System.out.println("top domain->>>>>>>>>>>" + getTopDomain(u.getHost()));

            HttpProxyResponse response = null;
            if (rules != null && u.getHost() != null && getTopDomain(u.getHost()) != null) {
                //search for top domain in list for redirect
                // if present use proxyEntity, otherwise use default
                for (Rule r : rules) {
                    if (getTopDomain(u.getHost()).equals(r.getTopDomain())) {
                        response = getResponse(path, req, r.getProxyHelper());
                        break;
                    }
//                    if(getTopDomain(u.getHost()).equals("localhost")){
//                        response = getResponse(path, req, r.getProxyHelper());
//                        break;
//                    }
                    //System.out.println("First and def element is " + helpers.get(0));
                    //System.out.println("path " + path + " req " + req.getRequestURL());
                    response = getResponse(path, req, helpers.get(0));
                }
            } else {
                response = getResponse(path, req, helpers.get(0));
            }


            //handle return type, only write out on wrong return type.
            res.setContentType(response.getContentType());
            res.getOutputStream().write(response.getData());
            res.getOutputStream().flush();
        } catch (ServletException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

    }

    /**
     * Do request to the resource and gets response.
     * @param path - Servlet request URI
     * @param req - Http Servlet Request
     * @param p - ProxyHelper instance
     * @return HttpProxyResponse instance
     * @throws IOException
     */
    private HttpProxyResponse getResponse(String path, HttpServletRequest req, ProxyHelper p) throws IOException {

        System.out.println(p.toString());

        String subFolder = p.getSubFolder();
        if (!subFolder.equals("")) {
            path = path.replaceAll(subFolder, "");
        }
        String queryString = req.getQueryString();
        String pathToGet = path;
        if (queryString != null && queryString.length() > 0)
            pathToGet += "?" + queryString;

        HttpProxyRequest proxyRequest = new HttpProxyRequest(p.getBaseLink() + pathToGet);
        //headers problem
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hName = headerNames.nextElement();
            String hValue = req.getHeader(hName);
            //System.out.println("Header name : "+ hName);
            //System.out.println("Header val : "+ hValue);
            if (hName.equals("referer")) {
                hValue = StringUtils.replace(hValue, p.getMeSubFolder(), p.getBaseLink());
            }
            if (hName.equals("host")) {
                hValue = p.getHostBase();
            }
            proxyRequest.addHeader(hName, hValue);
        }

        HttpProxyResponse response = HttpGetter.getUrlContent(proxyRequest);


        //check for images and urls.

        if (response.isHtml()) {
            String data = new String(response.getData(), response.getContentEncoding());
            data = data.replaceAll(p.getBaseLink(), p.getMeSubFolder());
            //relative hrefs replacing
            data = data.replaceAll("href=\"/", "href=\"" + subFolder + "/");
            response.setData(URLReplacementUtil.replace(
                    response.getData(),
                    response.getContentEncoding(), //TODO this must be dynamic
                    p.getBaseLink(),
                    p.getMeSubFolder()
            ));

            data = data.replaceAll("src=\"/", "src=\"" + subFolder + "/");
            response.setData(URLReplacementUtil.replace(
                    response.getData(),
                    response.getContentEncoding(), //TODO this must be dynamic
                    p.getBaseLink(),
                    p.getMeSubFolder()
            ));

            response.setData(data.getBytes());
        }

        return response;
    }

    private static String getTopDomain(String host) {
        String[] a = host.split("\\.");
        return a[a.length - 1];
    }

    private String getSubFolder(String hostBase) {
        String[] parts = hostBase.split("\\.");
        if (parts.length > 2) {
            return "/" + parts[0];
        }
        return "";
    }

    /**
     *
     * @param ruleURLS String[] of topDomains and numbers of link from BaseURL parameter.
     * @param allProxyHelper list of ProxyHelper objects
     * @return list of Rule objects
     */
    private List<Rule> parseRules(String[] ruleURLS, List<ProxyHelper> allProxyHelper) {
        if (ruleURLS.length % 2 != 0) {
            return null;
        }

        List<Rule> rules = new LinkedList<>();
        for (int i = 0; i < ruleURLS.length; i += 2) {
            Rule rule = new Rule();
            rule.setTopDomain(ruleURLS[i]);

            int index = Integer.parseInt(ruleURLS[i + 1]) - 1;
            if(index > allProxyHelper.size()-1){
                rules = null;
                break;
            }
            rule.setProxyHelper(allProxyHelper.get(index));
            rules.add(rule);
        }

        return rules;
    }

    /**
     *
     * @param hostMe current host
     * @param hostProtocol current host protocol
     * @param base URL of resource
     * @return ProxyHelper instance
     */
    private ProxyHelper getProxyInstance(String hostMe, String hostProtocol, URL base) {

        ProxyHelper p = new ProxyHelper();
        String hostBase = base.getHost();
        String baseProtocol = base.getProtocol() + "://";
        String subFolder = getSubFolder(hostBase);
        String me = hostProtocol + hostMe;
        String meSubFolder = me + subFolder;
        String baseLink = baseProtocol + hostBase;

        p.setTopDomain(getTopDomain(base.getHost()));
        p.setHostBase(hostBase);
        p.setSubFolder(subFolder);
        p.setMeSubFolder(meSubFolder);
        p.setBaseLink(baseLink);
        return p;
    }

}