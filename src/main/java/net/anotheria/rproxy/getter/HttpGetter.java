package net.anotheria.rproxy.getter;

import net.anotheria.rproxy.conf.Credentials;
import net.anotheria.rproxy.refactor.SiteCredentials;
import net.anotheria.rproxy.utils.IdleConnectionMonitorThread;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

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
    private static CloseableHttpClient httpClient = null;
    //private static HttpClientContext httpClientContext = null;

    static{
        try {
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(3, TimeUnit.SECONDS);
            ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Charset.forName("UTF-8")).build();

            connectionManager.setDefaultConnectionConfig(connectionConfig);
            connectionManager.setMaxTotal(200);
            connectionManager.setDefaultMaxPerRoute(20);

            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(2000).setConnectionRequestTimeout(2000).build();
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setRedirectStrategy(LaxRedirectStrategy.INSTANCE)
                    .setConnectionManager(connectionManager)
                    .build();

            IdleConnectionMonitorThread connectionMonitor = new IdleConnectionMonitorThread(connectionManager);
            connectionMonitor.start();
        }catch (Exception any){
            any.printStackTrace();
        }
    }

    private static HttpClientContext getContextInstance(){
        HttpClientContext httpClientContext = HttpClientContext.create();
        httpClientContext.setCredentialsProvider(new BasicCredentialsProvider());
        return httpClientContext;
    }

    public static HttpProxyResponse getUrlContent(HttpProxyRequest req) throws IOException {
        try{
            return getURL(req, null);
        }catch (Exception any){
            LOG.error("Could not get URL content", any);
            return null;
        }
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

        CloseableHttpResponse response = getHttpResponse(req, cred);

        Header[] headers = response.getAllHeaders();

        HttpProxyResponse ret = new HttpProxyResponse();
        ret.setStatusCode(response.getStatusLine().getStatusCode());
        ret.setStatusMessage(response.getStatusLine().getReasonPhrase());
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
                try {
                    //ensure entity is closed.
                    EntityUtils.consume(entity);
                } finally {
                    response.close();
                }
            }
        }

        return ret;
    }

    public static CloseableHttpResponse getHttpResponse(HttpProxyRequest req, UsernamePasswordCredentials credentials) throws IOException {
        HttpGet request = new HttpGet(req.getUrl());

        for (HttpProxyHeader header : req.getHeaders()) {
            request.addHeader(header.getName(), header.getValue());
            //request.addHeader("accept", "image/*"); System.out.println(header.getName() + " ++++++ " + header.getValue());
        }
        HttpClientContext context = getContextInstance();
        if (credentials != null) {
            URI uri = request.getURI();
            AuthScope authScope = new AuthScope(uri.getHost(), uri.getPort());

            org.apache.http.auth.Credentials cached = context.getCredentialsProvider().getCredentials(authScope);
            if (!areSame(cached, credentials)) {
                context.getCredentialsProvider().setCredentials(authScope, credentials);
            }
        }

        return httpClient.execute(request, context);
    }

    /**
     * Compare two instances of Credentials.
     * @param c1 instance of Credentials
     * @param c2 another instance of Credentials
     * @return comparison result. {@code true} if both are null or contain same user/password pairs, false otherwise.
     */
    private static boolean areSame(org.apache.http.auth.Credentials c1, org.apache.http.auth.Credentials c2) {
        if (c1 == null) {
            return c2 == null;
        } else {
            return StringUtils.equals(c1.getUserPrincipal().getName(), c1.getUserPrincipal().getName()) &&
                    StringUtils.equals(c1.getPassword(), c1.getPassword());
        }
    }
}
