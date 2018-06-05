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
import java.util.Enumeration;

@Monitor
public class ProxyFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyFilter.class);

    private String hostBase;
    private String hostMe;

    private String base;
    private String me;
    private String meSubFolder;
    private String subFolder;

    private static final String HTTP = "http://";

    public void init(FilterConfig filterConfig) {
        hostBase = filterConfig.getInitParameter("baseHost");
        hostMe = filterConfig.getInitParameter("myHost");
        subFolder = getSubFolder(hostBase);
        me = HTTP + hostMe;
        meSubFolder = me + subFolder;
        base = HTTP + hostBase;
        hostMe += subFolder;
    }

    private String getSubFolder(String hostBase) {
        String[] parts = hostBase.split("\\.");
        if (parts.length > 2) {
            return "/" + parts[0];
        }
        return "";
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
            String subFolder = this.subFolder;
            if (!subFolder.equals("")) {
                path = path.replaceAll(subFolder, "");
            }
            String queryString = req.getQueryString();
            String pathToGet = path;
            if (queryString != null && queryString.length() > 0)
                pathToGet += "?" + queryString;

            //System.out.println("Filter: " + pathToGet);

            //LOG.info(pathToGet);
            HttpProxyRequest proxyRequest = new HttpProxyRequest(base + pathToGet);
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String hName = headerNames.nextElement();
                String hValue = req.getHeader(hName);

                if (hName.equals("referer")) {
                    hValue = StringUtils.replace(hValue, meSubFolder, base);
                }
                if (hName.equals("host")) {
                    hValue = hostBase;
                }


                proxyRequest.addHeader(hName, hValue);
            }

            HttpProxyResponse response = HttpGetter.getUrlContent(proxyRequest);


            //check for images and urls.

            if (response.isHtml()) {
                String data = new String(response.getData(), response.getContentEncoding());
                data = data.replaceAll(base, meSubFolder);
                //relative hrefs replacing
                data = data.replaceAll("<a href=\"/", "<a href=\"" + subFolder + "/");
                response.setData(URLReplacementUtil.replace(
                        response.getData(),
                        response.getContentEncoding(), //TODO this must be dynamic
                        base,
                        meSubFolder
                ));

                response.setData(data.getBytes());
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

}