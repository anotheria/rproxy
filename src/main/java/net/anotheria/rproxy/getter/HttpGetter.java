package net.anotheria.rproxy.getter;

import net.anotheria.rproxy.conf.Credentials;
import net.anotheria.rproxy.refactor.SiteCredentials;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

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

    static {
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

    public static HttpProxyResponse getUrlContent(HttpProxyRequest req) throws IOException {
        return getURL(req, null);
    }

    public static HttpProxyResponse getUrlContent(HttpProxyRequest req, SiteCredentials cred) throws IOException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials(cred.getUserName(), cred.getPassword());
        return getURL(req, c);
    }

    public static HttpProxyResponse getUrlContent(HttpProxyRequest req, Credentials cred) throws IOException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials(cred.getUserName(), cred.getPassword());
        return getURL(req, c);
    }

    public static HttpProxyResponse getURL(HttpProxyRequest req, UsernamePasswordCredentials cred) throws IOException {
        LOG.info(req.getUrl());

        HttpResponse response = getHttpResponse(req, cred);

        Header[] headers = response.getAllHeaders();

        HttpProxyResponse ret = new HttpProxyResponse();
        ret.setStatusCode(response.getStatusLine().getStatusCode());
        ret.setStatusMessage(response.getStatusLine().getReasonPhrase());
        /**
         * add response headers
         */
        ret.setHeaders(headers);
        final HttpEntity entity = response.getEntity();

        if (entity != null && entity.getContentType() != null) {
            ret.setContentType(entity.getContentType().getValue());
        }

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

    public static HttpResponse getHttpResponse(HttpProxyRequest req, UsernamePasswordCredentials credentials) throws IOException {
        HttpGet request = new HttpGet(req.getUrl());

        for (HttpProxyHeader header : req.getHeaders()) {
            request.addHeader(header.getName(), header.getValue());
            //request.addHeader("accept", "image/*"); System.out.println(header.getName() + " ++++++ " + header.getValue());
        }
        HttpClient client;
        if (credentials != null) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            URI uri = request.getURI();
            AuthScope authScope = new AuthScope(uri.getHost(), uri.getPort());
            provider.setCredentials(authScope, credentials);
            client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).setDefaultCredentialsProvider(provider).build();
        } else {
            client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        }

        return client.execute(request);
    }
}
