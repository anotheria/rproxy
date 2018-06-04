package net.anotheria.rproxy;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
        //page = page.replaceAll("/images/nav_logo229.png", "https://www.google.com/images/nav_logo229.png");
//        String s = getImage("https://www.google.com/logos/doodles/2018/childrens-day-2018-5663898442137600-l.png");
//        page = page.replaceAll("/logos/doodles/2018/childrens-day-2018-5663898442137600-l.png", s);
        // page = page.replaceAll("/client_204?&atyp=i&biw=845&bih=724&ei=YkgRW63xNubJ6AT3zYugBA", "https://www.google.com/client_204?&atyp=i&biw=845&bih=724&ei=YkgRW63xNubJ6AT3zYugBA");
        //page = page.replaceAll("/xjs/_/js/k=xjs.hp.en.kF3IU-QQl14.O/m=sb_he,d/am=VGFs/rt=j/d=1/rs=ACT90oGkpOkQq2_GhrzZuQ1VKtDmY7Z9Xw", "https://www.google.com/xjs/_/js/k=xjs.hp.en.kF3IU-QQl14.O/m=sb_he,d/am=VGFs/rt=j/d=1/rs=ACT90oGkpOkQq2_GhrzZuQ1VKtDmY7Z9Xw");

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
