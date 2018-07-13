package net.anotheria.rproxy;

import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.rproxy.conf.*;
import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.refactor.cache.ICacheStrategy;
import net.anotheria.rproxy.refactor.cache.LRUStrategyImpl;
import net.anotheria.rproxy.replacement.AttrParser;
import net.anotheria.rproxy.replacement.URLReplacementUtil;
import net.anotheria.util.StringUtils;
import org.apache.http.Header;
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

    private static String url;
    private static String currentSubFolder;


    private static final Logger LOG = LoggerFactory.getLogger(ProxyFilter.class);

    private List<Rule> defaultRules;
    private List<ProxyHelper> helpers;
    private Map<Integer, List<ContentReplace>> configRules;
    private Map<Integer, Credentials> cred;

    private static ICacheStrategy<String, HttpProxyResponse> cache = null; //new LRUStrategyImpl<>(150);

    public void init(FilterConfig filterConfig) {

        ConfigurationEntity conf = Configurer.getJsonConfiguration();//CacheConfigurer.parseConfigurationFile("config.json");
        cred = new HashMap<>();
        if (conf == null) {
            //parse from web.xml
            parseWebXml(filterConfig);
            //System.out.println("Configuring from web.xml");
        } else {
            //get from config.json
            //System.out.println("Configuring via config.json" + conf.toString());
            if (conf.getCredentials() != null && conf.getCredentials().length != 0) {
                for (Credentials c : conf.getCredentials()) {
                    cred.put(c.getLinkNum(), c);
                }
            }
            configure(conf);
            configRules = Configurer.getReplacementRules();
        }
    }


    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try {


            HttpServletRequest req = (HttpServletRequest) servletRequest;
            HttpServletResponse res = (HttpServletResponse) servletResponse;

            String path = req.getRequestURI();
            String appUrl = req.getRequestURL().toString();

            //System.out.println("Route : " + appUrl);
            //url = req.getRequestURL().toString();
            String top = getTopPath(appUrl);
            if (top != null) {
                currentSubFolder = "/" + top;
            }
//            System.out.println(currentSubFolder);
//            System.out.println(appUrl);

            if (!(servletRequest instanceof HttpServletRequest)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            HttpProxyResponse response = null;

            if (cache != null && cache.get(appUrl) != null) {
                response = cache.get(appUrl);
                System.out.println(appUrl + " ++++++++++++++++++ ");
            } else {
                System.out.println(appUrl + " ------------------ ");
                URL u = new URL(appUrl);

                String topPath = getTopPath(appUrl);
                String topDomain = getTopDomain(u.getHost());

                if (defaultRules != null) {
                    //if top path is present
                    //System.out.println("defRules != null");
                    for (Rule defRule : defaultRules) {
                        //search rule where subdom equals to current request top path
                        if (defRule.getSubDomain().equals(topPath)) {
                            //found rule! now check if it has top domain subrules
                            if (!defRule.getTopDomainList().isEmpty()) {
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
                                response = getResponse(path, req, defRule.getProxyHelperDefault());
                                break;
                            }
                        }
                    }

                } else {
                    response = getResponse(path, req, helpers.get(0));
                }

            }
            if (response != null) {
                if(cache != null) {
                    cache.add(appUrl, response);
                }
                /**
                 * set expires header from wp
                 */
                for (Header h : response.getHeaders()) {
                    if (h.getName().equalsIgnoreCase("expires")) {
                        res.addHeader(h.getName(), h.getValue());
                    }
                }
                //handle return type, only write out on wrong return type.
                res.setContentType(response.getContentType());
                res.getOutputStream().write(response.getData());
                res.getOutputStream().flush();
            }
        } catch (ServletException | UnsupportedEncodingException e) {
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

        //String subFolder = p.getSubFolder();

        //System.out.println("Current sub ->>>>> " + currentSubFolder);
        p.subFolderUpdate(currentSubFolder);
        //System.out.println(p.toString());

        if (!currentSubFolder.equals("")) {
            path = path.replaceAll(currentSubFolder, "");
        }
        String queryString = req.getQueryString();
        String pathToGet = path;
        if (queryString != null && queryString.length() > 0)
            pathToGet += "?" + queryString;

        String proxyRequestURL;

        proxyRequestURL = p.getBaseLink() + pathToGet;

        HttpProxyRequest proxyRequest = new HttpProxyRequest(proxyRequestURL);
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hName = headerNames.nextElement();
            String hValue = req.getHeader(hName);
            if (hName.equals("referer")) {
                hValue = StringUtils.replace(hValue, p.getMeSubFolder(), p.getBaseLink());
            }
            if (hName.equals("host")) {
                hValue = p.getHostBase();
            }
            proxyRequest.addHeader(hName, hValue);

        }
        HttpProxyResponse response;
        int index = helpers.indexOf(p) + 1;
        if (index == -1 || cred.get(index) == null) {
            response = HttpGetter.getUrlContent(proxyRequest);
        } else {
            response = HttpGetter.getUrlContent(proxyRequest, cred.get(index));
        }

        //check for images and urls.

        if (response.isHtml()) {

            String data = new String(response.getData(), response.getContentEncoding());
            data = data.replaceAll(p.getBaseLink(), p.getMeSubFolder());
            //relative hrefs replacing


            data = data.replaceAll("href=\"/", "href=\"" + currentSubFolder + "/");

            data = AttrParser.addSubFolderToRelativePathesInSrcSets(data, currentSubFolder);

            data = data.replaceAll("src=\"/", "src=\"" + currentSubFolder + "/");


            data = data.replaceAll("localhost/", "localhost:8080/");

            //data = data.replaceAll("/category", "");

            data = data.replaceAll(p.getBaseLink(), p.getMeSubFolder());


            if (index != -1 && configRules != null && configRules.get(index) != null) {
                data = getReplacementWithConfig(data, configRules.get(index));
            }

            response.setData(URLReplacementUtil.replace(
                    response.getData(),
                    response.getContentEncoding(),
                    p.getBaseLink(),
                    p.getMeSubFolder()
            ));
            response.setData(data.getBytes());
        }
        return response;
    }

    private String getReplacementWithConfig(String data, List<ContentReplace> replaceRules) {
        for (ContentReplace c : replaceRules) {
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

    private ProxyHelper getProxyInstance(URL host, URL base) {

        ProxyHelper p = new ProxyHelper();

        String hostMe = host.getHost();
        String hostProtocol = host.getProtocol() + "://";

        String hostBase = base.getHost();
        String baseProtocol = base.getProtocol() + "://";
        String subFolder = getSubFolder(hostBase);
        String me = hostProtocol + hostMe;
        String meSubFolder = me + subFolder;
        String baseLink = baseProtocol + hostBase;


        p.setHostBase(hostBase);
        p.setBaseLink(baseLink);
        p.setMeSubFolder(meSubFolder);
        p.setSubFolder(subFolder);
        p.setTopDomain(getTopDomain(base.getHost()));
        p.setHostProtocol(hostProtocol);
        p.setHostMe(hostMe);
        p.setMe(me);


//        p.setTopDomain(getTopDomain(base.getHost()));
//        p.setHostBase(hostBase);
//        p.setSubFolder(subFolder);
//        p.setMeSubFolder(meSubFolder);
//        p.setBaseLink(baseLink);

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

//    private static List<ContentReplace> getConfigRulesFromXml() {
//        XMLParser p = new XMLParser();
//        return p.parseConfig("config.xml", XMLParser.getTgNames());
//    }

    private void configure(ConfigurationEntity conf) {
        URL host = null;
        //String hostMe = null;
        //String hostProtocol = null;
        try {
            host = new URL(conf.getHostUrl());
            // hostMe = host.getHost();
            //hostProtocol = host.getProtocol() + "://";
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


            ProxyHelper p = getProxyInstance(host, base);
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
        //String hostMe = null;
        //String hostProtocol = null;
        try {
            host = new URL(filterConfig.getInitParameter("HostURL"));
            //hostMe = host.getHost();
            //hostProtocol = host.getProtocol() + "://";
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

            //System.out.println("Base url : " + baseUrl);
            ProxyHelper p = getProxyInstance(host, base);
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