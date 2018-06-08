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

    private List<Rule> defaultRules;
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

            System.out.println(topDomain + " -> top domain here");

            HttpProxyResponse response = null;

            if (defaultRules != null) {
                //if top path is present
                System.out.println("defRules != null");
                for (Rule defRule : defaultRules) {
                    System.out.println(defRule.getSubDomain() + " <???> " + topPath);
                    //search rule where subdom equals to current request top path
                    if (defRule.getSubDomain().equals(topPath)) {
                        System.out.println("defRules found by topPath!");
                        //found rule! now check if it has top domain subrules
                        if (!defRule.getTopDomainList().isEmpty()) {
                            System.out.println("subRules != null");
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
                            System.out.println("subRules == null");
                            System.out.println(defRule.getProxyHelperDefault().toString());
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

//    private Rule ruleForTopDomain(String topDomain) {
//        if (rules == null) {
//            return null;
//        }
//        for (Rule rule : rules) {
//            if (getTopDomain(u.getHost()).equals(r.getTopDomain())) {
//                response = getResponse(path, req, r.getProxyHelper());
//                break;
//            }
//
//            response = getResponse(path, req, helpers.get(0));
//        }
//    }

    /**
     * Do request to the resource and gets response.
     *
     * @param path - Servlet request URI
     * @param req  - Http Servlet Request
     * @param p    - ProxyHelper instance
     * @return HttpProxyResponse instance
     * @throws IOException
     */
    private HttpProxyResponse getResponse(String path, HttpServletRequest req, ProxyHelper p) throws IOException {

        //System.out.println(p.toString());

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
            //take helper by index and find his def rule then add top domain rule
//            ProxyHelper ph = this.helpers.get(Integer.parseInt(ruleString[i]) - 1);
//            for (Rule r : this.defaultRules) {
//                if (r.getProxyHelperDefault().equals(ph)) {
//                    r.addTopDomainRule(rule);
//                    System.out.println("Found!!!");
//                    break;
//                }
//            }
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
        return p;
    }

    /**
     * Search for top part of url path. Example: http://website.com/path/to/something?p=1 path will be returned.
     *
     * @param url string representation of url
     * @return first part of the url`s path
     */
    private String getTopPath(String url) {
        //System.out.println(url);
        if (url == null) {
            return null;
        }

        try {
            URL u = new URL(url);
            String path = u.getPath();
            String[] pathParts = path.split("/");
            String res = null;
            // System.out.println(Arrays.toString(pathParts));
            for (String part : pathParts) {
                if (part != null && !part.equals("")) {
                    //   System.out.println(part);
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

}