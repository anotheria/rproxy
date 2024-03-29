package net.anotheria.rproxy.getter;

import org.apache.http.Header;

import java.io.Serializable;
import java.util.Arrays;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 04.06.18 10:41
 */
public class HttpProxyResponse implements Serializable {
    private byte[] data;
    private int statusCode;
    private String statusMessage;
    private String contentType;
    private String contentEncoding;
    private Header[] headers;
    private boolean gzip;

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        //System.out.println("Content type -----> " + contentType);
        boolean set = false;
        if (contentType != null && !contentType.equals("")) {
            String[] cType = contentType.split(";");
            for (String p : cType) {
                if (p.startsWith(" charset=") || p.startsWith("charset=")) {
                    p = p.trim();
                    p = p.replace("charset=", "");
                    this.contentEncoding = p;
                    set = true;
                }
            }
        }
        if (!set) {
            //set default encoding utf-8 or ISO8859_1
            this.contentEncoding = "UTF-8";
        }
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isHtml() {
        //this is temporarly solution.
        return contentType != null && contentType.contains("text/html");
    }

    public boolean isCss() {
        //this is temporarly solution.
        return contentType != null && contentType.contains("css");
    }

    public boolean isScript() {
        //this is temporarly solution.
        return contentType != null && contentType.contains("text/javascript");
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    @Override
    public String toString() {
        return "HttpProxyResponse{" +
                "data=" + Arrays.toString(data) +
                ", statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentEncoding='" + contentEncoding + '\'' +
                '}';
    }
}
