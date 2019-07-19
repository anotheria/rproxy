package net.anotheria.rproxy.cache.resources;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import net.anotheria.rproxy.cache.CacheLoader;
import net.anotheria.rproxy.cache.resources.bean.CacheableResource;
import net.anotheria.rproxy.cache.resources.bean.ResourceCacheKey;
import net.anotheria.rproxy.getter.HttpGetter;
import net.anotheria.rproxy.getter.HttpProxyRequest;
import net.anotheria.rproxy.getter.HttpProxyResponse;

/**
 * @author
 */
public class ResourceCacheLoader implements CacheLoader<ResourceCacheKey, CacheableResource> {

    private ResourceCacheConfig config = new ResourceCacheConfig();

    @Override
    public CacheableResource load(ResourceCacheKey key) throws Exception {
        String sourceUrl = getSourceUrl(key.getUrl());

        HttpProxyRequest proxyRequest = new HttpProxyRequest(sourceUrl);
        HttpProxyResponse response = HttpGetter.getUrlContent(proxyRequest);

        if (response.getStatusCode() != 200) {
            return null;
        }

        CacheableResource ret = new CacheableResource();
        ret.setContentType(response.getContentType());
        ret.setUrl(key.getUrl());

        String uri = saveToFS(ret.getUrl(), response.getData());
        ret.setStoragePath(uri);

        return ret;
    }

    private String getSourceUrl(String url) throws URISyntaxException {
        URI uri = new URI(url);
        uri = new URI(uri.getScheme().toLowerCase(), config.getSourceDomain(), uri.getPath(), uri.getQuery(), uri.getFragment());

        return uri.toString();
    }

    private String saveToFS(String uri, byte[] body) throws IOException {
        File file = new File(config.getFsStoragePath() + File.separator + new URL(uri).getFile());
        FileUtils.writeByteArrayToFile(file, body);

        return file.getPath();
    }
}
