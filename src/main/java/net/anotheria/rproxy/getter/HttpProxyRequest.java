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

	public void removeHeader(String name){
		HttpProxyHeader r = null;
		for(HttpProxyHeader h : headers){
			if(h.getName().equalsIgnoreCase(name)){
				r = h;
				break;
			}
		}
		headers.remove(r);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HttpProxyRequest that = (HttpProxyRequest) o;

		if (url != null ? !url.equals(that.url) : that.url != null) return false;
		return headers != null ? headers.equals(that.headers) : that.headers == null;
	}

	@Override
	public int hashCode() {
		int result = url != null ? url.hashCode() : 0;
		result = 31 * result + (headers != null ? headers.hashCode() : 0);
		return result;
	}
}
