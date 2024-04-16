package net.anotheria.rproxy.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.brotli.dec.BrotliInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public final class RewriteUtils {

    public static void permanentRedirect(HttpServletResponse httpServletResponse, String location){
        httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        httpServletResponse.addHeader("Location", location);
    }

    public static byte[] decompressBrotli(byte[] dataArr) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(dataArr);
            BrotliInputStream brotliInputStream = new BrotliInputStream(inputStream);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = brotliInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            brotliInputStream.close();
            outputStream.close();

            return outputStream.toByteArray();
        }catch (Exception e){
            return dataArr;
        }
    }
}
