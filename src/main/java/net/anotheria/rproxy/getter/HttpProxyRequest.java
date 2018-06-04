package net.anotheria.rproxy.getter;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 04.06.18 10:35
 */
public class HttpProxyRequest {
	private String url;
	private List<HttpProxyHeader> headers = new LinkedList<>();



	public HttpProxyRequest(String url){
		this.url = url;

	}

	public void addHeader(String name, String value){
		headers.add(new HttpProxyHeader(name, value));
	}

	public String getUrl() {
		return url;
	}

	public List<HttpProxyHeader> getHeaders() {
		return headers;
	}

	@Override
	public String toString() {
		return "HttpProxyRequest{" +
				"url='" + url + '\'' +
				", headers=" + headers +
				'}';
	}

}
