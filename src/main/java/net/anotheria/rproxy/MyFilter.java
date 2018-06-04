package net.anotheria.filter;

import net.anotheria.rproxy.PageContent;
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
import java.util.Map;

public class MyFilter implements Filter {

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
		String page = PageContent.getPageContent(base+pathToGet);
		//first path simply replace the url.
		net.anotheria.util.StringUtils.replace(page, base, me);

		//System.out.println(page);
		servletResponse.getWriter().print(page);
//            if (urlMustBeReplaced(url)) {
//                //get page with new url
//                String page = PageContent.getPageContent("https://www.google.com");
//                //System.out.println(page);
//                servletResponse.getWriter().print(page);
//            }
//            if (urlRelative(url)) {
//                HttpClient cl = HttpClientBuilder.create().build();
//                HttpGet re = new HttpGet(url);
//
//                HttpResponse response = cl.execute(re);
//                HttpEntity entity = response.getEntity();
//            }
		//PageContent.proxy(url);
		//filterChain.doFilter(servletRequest, servletResponse);

    }

    private boolean urlRelative(String url) {
        if (!url.equals("http://localhost:8080/")) {
            System.out.println("Relative!");
            return true;
        }
        return false;

    }

    private boolean urlMustBeReplaced(String url) {
        return true;
    }


}