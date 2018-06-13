package net.anotheria.rproxy;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.conf.ConfigJSON;
import net.anotheria.rproxy.conf.ContentReplace;
import net.anotheria.rproxy.conf.JsonConfigurer;
import net.anotheria.rproxy.conf.XMLParser;
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

    //private static List<ContentReplace> configRules = getConfigRulesFromXml();

    private static final Logger LOG = LoggerFactory.getLogger(ProxyFilter.class);

    private List<Rule> defaultRules;
    private List<ProxyHelper> helpers;
    private List<ContentReplace> configRules;

    public void init(FilterConfig filterConfig) {

        ConfigJSON conf = JsonConfigurer.parseConfigurationFile("conf.json");

        if (conf == null) {
            //parse from web.xml
            parseWebXml(filterConfig);
            System.out.println("Configuring from web.xml");
        } else {
            //get from conf.json
            System.out.println("Configuring via conf.json" + conf.toString());
            configure(conf);
            configRules = JsonConfigurer.getReplacementRules();
        }
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

            URL u = new URL(appUrl);

            String topPath = getTopPath(appUrl);
            String topDomain = getTopDomain(u.getHost());

            // System.out.println(topDomain + " -> top domain here");

            HttpProxyResponse response = null;

            if (defaultRules != null) {
                //if top path is present
                //System.out.println("defRules != null");
                for (Rule defRule : defaultRules) {
                    //System.out.println(defRule.getSubDomain() + " <???> " + topPath);
                    //search rule where subdom equals to current request top path
                    if (defRule.getSubDomain().equals(topPath)) {
                        // System.out.println("defRules found by topPath!");
                        //found rule! now check if it has top domain subrules
                        if (!defRule.getTopDomainList().isEmpty()) {
                            //System.out.println("subRules != null");
                            //it has subrules, search for current url topdomain rule
                            for (RuleTopDomain topDomRule : defRule.getTopDomainList()) {
                                //if found - do request for current rule, otherwise do without subrule
                                if (topDomRule.getTopDomain().equals(topDomain)) {
                                    response = getResponse(path, req, topDomRule.getProxyHelper());
                                    break;
                                }
                                response = getResponse(path, req, defRule.getProxyHelperDefault());
                            }
                        } else {
                            ///System.out.println("subRules == null");
                            //System.out.println(defRule.getProxyHelperDefault().toString());
                            response = getResponse(path, req, defRule.getProxyHelperDefault());
                            break;
                        }
                    }
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
     * @param rulesArr       array of String subDomain name and URL one by one pairs
     * @param allProxyHelper List of proxy helper objects
     * @return list of prepared Rule objects
     */
    private List<Rule> parseSubDomainRules(String[] rulesArr, List<ProxyHelper> allProxyHelper) {
        if (rulesArr.length % 2 != 0) {
            return null;
        }

        List<Rule> rules = new LinkedList<>();
        for (int i = 0; i < rulesArr.length; i += 2) {
            Rule rule = new Rule();
            rule.setSubDomain(rulesArr[i]);
            int index = Integer.parseInt(rulesArr[i + 1]) - 1;
            if (index > allProxyHelper.size() - 1) {
                rules = null;
                break;
            }
            rule.setProxyHelperDefault(allProxyHelper.get(index));
            rules.add(rule);
        }

        return rules;
    }

    /**
     * Do request to the resource and gets response.
     *
     * @param path - Servlet request URI
     * @param req  - Http Servlet Request
     * @param p    - ProxyHelper instance with data
     * @return HttpProxyResponse instance
     * @throws IOException
     */
    private HttpProxyResponse getResponse(String path, HttpServletRequest req, ProxyHelper p) throws IOException {

        String subFolder = p.getSubFolder();
        if (!subFolder.equals("")) {
            path = path.replaceAll(subFolder, "");
        }
        String queryString = req.getQueryString();
        String pathToGet = path;
        if (queryString != null && queryString.length() > 0)
            pathToGet += "?" + queryString;

        HttpProxyRequest proxyRequest = new HttpProxyRequest(p.getBaseLink() + pathToGet);
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hName = headerNames.nextElement();
            String hValue = req.getHeader(hName);
            System.out.println("Request header " + hName + " : " + hValue);
            if (hName.equals("referer")) {
                hValue = StringUtils.replace(hValue, p.getMeSubFolder(), p.getBaseLink());
                //hValue = p.getBaseLink();
            }
            if (hName.equals("host")) {
                hValue = p.getHostBase();
            }
//            if (hName.equals("origin")) {
//                hValue = "http://localhost:8080/faq";
//            }


            proxyRequest.addHeader(hName, hValue);

        }

        //proxyRequest.removeHeader("cookie");
        //proxyRequest.addHeader("cookie", "hblid=agtvDF1Y5Fn2ljDN7c9fe0M67TEd6AB3; olfsk=olfsk05741559938510821; _ga=GA1.1.1726846344.1528898076; _gid=GA1.1.1315206542.1528898076; JSESSIONID=5C747B0CF8B579F7297532BA335F5F6F; _gat=1");
        //proxyRequest.removeHeader("origin");
        //proxyRequest.addHeader("origin", "http://localhost:8080/faq/");
       // proxyRequest.addHeader("cache-control", "no-cache");
        HttpProxyResponse response = HttpGetter.getUrlContent(proxyRequest);


        //check for images and urls.

        if (response.isHtml()) {
            String data = new String(response.getData(), response.getContentEncoding());
            data = data.replaceAll(p.getBaseLink(), p.getMeSubFolder());
            //relative hrefs replacing
            data = data.replaceAll("href=\"/", "href=\"" + subFolder + "/");


            data = data.replaceAll("src=\"/", "src=\"" + subFolder + "/");


            data = data.replaceAll("localhost/", "localhost:8080/");

            //data = data.replaceAll("woff2", "woff" );


            if (configRules != null) {
                data = getReplacementWithConfig(data);
            }

            response.setData(URLReplacementUtil.replace(
                    response.getData(),
                    response.getContentEncoding(),
                    p.getBaseLink(),
                    p.getMeSubFolder()
            ));
            //System.out.println("Response Content encoding ++++++++++++   " + response.getContentEncoding());

            response.setData(data.getBytes());
        }
        //System.out.println("----> Response Content encoding ++++++++++++   " + response.getContentEncoding());
        return response;
    }

    private String getReplacementWithConfig(String data) {
        for (ContentReplace c : configRules) {
            data = c.applyReplacement(data);
        }
        return data;
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
     * @param ruleString     String[] of topDomains and numbers of link from BaseURL parameter.
     * @param allProxyHelper list of ProxyHelper objects
     */
    private void parseTopDomainRules(String[] ruleString, List<ProxyHelper> allProxyHelper) {
        if (ruleString.length % 3 != 0) {
            return;
        }

        for (int i = 0; i < ruleString.length; i += 3) {
            RuleTopDomain rule = new RuleTopDomain();
            rule.setTopDomain(ruleString[i + 1]);
            rule.setProxyHelper(allProxyHelper.get(Integer.parseInt(ruleString[i + 2]) - 1));
            this.defaultRules.get(Integer.parseInt(ruleString[i]) - 1).addTopDomainRule(rule);

        }

    }

    /**
     * @param hostMe       current host
     * @param hostProtocol current host protocol
     * @param base         URL of resource
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

        //System.out.println(p.toString());
        return p;
    }

    /**
     * Search for top part of url path. Example: http://website.com/path/to/something?p=1 path will be returned.
     *
     * @param url string representation of url
     * @return first part of the url`s path
     */
    private String getTopPath(String url) {
        if (url == null) {
            return null;
        }

        try {
            URL u = new URL(url);
            String path = u.getPath();
            String[] pathParts = path.split("/");
            String res = null;
            for (String part : pathParts) {
                if (part != null && !part.equals("")) {
                    res = part;
                    break;
                }
            }
            return res;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<ContentReplace> getConfigRulesFromXml() {
        XMLParser p = new XMLParser();
        return p.parseConfig("conf.xml", XMLParser.getTgNames());
    }

    private void configure(ConfigJSON conf) {
        URL host = null;
        String hostMe = null;
        String hostProtocol = null;
        try {
            host = new URL(conf.getHostUrl());
            hostMe = host.getHost();
            hostProtocol = host.getProtocol() + "://";
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        List<ProxyHelper> allProxyHelper = new LinkedList<>();
        for (String baseUrl : conf.getBaseUrl()) {
            URL base = null;

            try {
                base = new URL(baseUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            ProxyHelper p = getProxyInstance(hostMe, hostProtocol, base);
            allProxyHelper.add(p);
        }

        if (conf.getSubDomainRules() != null) {
            this.defaultRules = parseSubDomainRules(conf.getSubDomainRules(), allProxyHelper);
        }

        if (conf.getTopDomainRules() != null) {
            parseTopDomainRules(conf.getTopDomainRules(), allProxyHelper);
        }
        this.helpers = new LinkedList<>(allProxyHelper);
    }

    private void parseWebXml(FilterConfig filterConfig) {
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

        //prepared links from BaseUrl param
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

        //fill rules for default behaviour for sub domains
        String defRule = filterConfig.getInitParameter("SubDomainRule");
        if (defRule != null) {
            this.defaultRules = parseSubDomainRules(defRule.split(","), allProxyHelper);
        }

        //now add rules to sub domains for top domains
        String topDomainRulesString = filterConfig.getInitParameter("TopDomainRule");
        if (topDomainRulesString != null) {
            parseTopDomainRules(topDomainRulesString.split(","), allProxyHelper);
        }
        this.helpers = new LinkedList<>(allProxyHelper);
    }

}