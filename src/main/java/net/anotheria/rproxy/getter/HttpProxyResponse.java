package net.anotheria.rproxy.getter;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 04.06.18 10:41
 */
public class HttpProxyResponse {
	private byte[] data;
	private int statusCode;
	private String statusMessage;
	private String contentType;

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

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public boolean isHtml() {
		//this is temporarly solution.
		return contentType != null && contentType.contains("text/html");
	}
}
