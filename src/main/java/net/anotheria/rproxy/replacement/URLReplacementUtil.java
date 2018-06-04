package net.anotheria.rproxy.replacement;

import net.anotheria.util.StringUtils;

import java.io.UnsupportedEncodingException;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 04.06.18 11:04
 */
public class URLReplacementUtil {
	public static byte[] replace(byte[] dataContent, String encoding, String what, String with){
		try {
			String dataAsString = new String(dataContent, encoding);
			dataAsString = StringUtils.replace(dataAsString, what, with);
			return dataAsString.getBytes();
		}catch(UnsupportedEncodingException e){
			//unclear for now what to do, add logging
			e.printStackTrace();
			throw new RuntimeException("Must be handled");
		}
	}
}
