package net.anotheria.rproxy;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.apache.http.HttpHeaders.USER_AGENT;

public final class PageContent {

    public static String getPageContent(String url) {
        HttpResponse r = getPageResponse(url);
        String page = responseToString(r);


        return page;
    }

    private static HttpResponse getPageResponse(String url) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private static String responseToString(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                entity.writeTo(out);
                return new String(out.toByteArray(), Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    EntityUtils.consume(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String getImage(String url) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            BufferedInputStream bis = new BufferedInputStream(entity.getContent());

            //showContentType(entity);
            byte[] a = new byte[entity.getContent().available()];
            //byte[] bytes = IOUtils.toByteArray(is);
            return new String(Base64.encodeBase64(a), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void proxy(String url){
        System.out.println(url);
    }

}
