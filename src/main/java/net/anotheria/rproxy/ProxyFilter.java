package net.anotheria.rproxy;

import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.replacement.URLReplacementUtil;
import net.anotheria.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class ProxyFilter implements Filter {

    private String base;
    private String me;

    private String hostBase;
    private String hostMe;

    private static final String HTTP = "http://";
    public void init(FilterConfig filterConfig) {
        this.hostBase = filterConfig.getInitParameter("baseHost");
        this.hostMe = filterConfig.getInitParameter("myHost");
        me = HTTP + hostMe;
        base = HTTP + hostBase;
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (!(servletRequest instanceof HttpServletRequest)){
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;


        String path = req.getRequestURI();
        String queryString = req.getQueryString();
        String pathToGet = path;
        if (queryString!=null && queryString.length()>0)
            pathToGet += "?" + queryString;

        System.out.println("Filter: "+pathToGet);

        HttpProxyRequest proxyRequest = new HttpProxyRequest(base+pathToGet);
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()){
            String hName = headerNames.nextElement();
            String hValue = req.getHeader(hName);

            if (hName.equals("referer")){
                hValue = StringUtils.replace(hValue, me, base );
            }
            if (hName.equals("host")){
                hValue = StringUtils.replace(hValue, hostMe, hostBase);
            }


            proxyRequest.addHeader(hName, hValue);
        }

        HttpProxyResponse response = HttpGetter.getUrlContent(proxyRequest);


        //check for images and urls.
        if (response.isHtml()){
            response.setData(URLReplacementUtil.replace(
                    response.getData(),
                    response.getContentEncoding(), //TODO this must be dynamic
                    base,
                    me
            ));
        }


        //handle return type, only write out on wrong return type.
        res.setContentType(response.getContentType());
        res.getOutputStream().write(response.getData());
        res.getOutputStream().flush();

    }

}