package net.anotheria.rproxy;

import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;
import net.anotheria.rproxy.replacement.URLReplacementUtil;
import org.apache.http.HttpEntity;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

public class ProxyFilter implements Filter {

    private static Map<String, HttpEntity> s = null;

    private ServletContext context;

    //TOBE Configured
    String base = "http://faq.thecasuallounge.ch";
    String me = "http://localhost:8080";

    public void init(FilterConfig filterConfig) throws ServletException {
        this.context = filterConfig.getServletContext();
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
		//get page with new url

		HttpProxyRequest proxyRequest = new HttpProxyRequest(base+pathToGet);
		Enumeration<String> headerNames = req.getHeaderNames();
		String hName;
		while (headerNames.hasMoreElements()){
			hName = headerNames.nextElement();
			proxyRequest.addHeader(hName, req.getHeader(hName));
		}

		HttpProxyResponse response = HttpGetter.getUrlContent(proxyRequest);

		//check for images and urls.
		if (response.isHtml()){
			response.setData(URLReplacementUtil.replace(
					response.getData(),
					"UTF-8", //TODO this must be dynamic
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