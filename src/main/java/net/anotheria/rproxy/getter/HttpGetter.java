package net.anotheria.rproxy.getter;

import net.anotheria.rproxy.ProxyFilter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 04.06.18 10:40
 */
public class HttpGetter {

    private static final Logger LOG = LoggerFactory.getLogger(HttpGetter.class);
	/**
	 * HttpClient instance.
	 */
	private static AbstractHttpClient httpClient = null;

	static{
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
				new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(
				new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

		PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		// Increase max total connection to 200
		cm.setMaxTotal(200);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(20);

		httpClient = new DefaultHttpClient(cm);
	}


	public static HttpProxyResponse getUrlContent(HttpProxyRequest req) throws IOException{
		//System.out.println("Trying to get "+req);
		LOG.info(req.getUrl());
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(req.getUrl());


		for (HttpProxyHeader header : req.getHeaders()){
			request.addHeader(header.getName(), header.getValue());
		}

		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (IOException e) {
			throw e;
		}

		HttpProxyResponse ret = new HttpProxyResponse();
		ret.setStatusCode(response.getStatusLine().getStatusCode());
		ret.setStatusMessage(response.getStatusLine().getReasonPhrase());
		final HttpEntity entity = response.getEntity();
		ret.setContentType(entity.getContentType().getValue());

		if (entity != null) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				entity.writeTo(out);//call this in any case!!!
				ret.setData(out.toByteArray());
			} finally {
				//ensure entity is closed.
				EntityUtils.consume(entity);
			}
		}

		return ret;
	}


}
