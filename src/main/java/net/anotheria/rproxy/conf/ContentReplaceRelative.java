package net.anotheria.rproxy.conf;

/**
 * Implementation of ContentReplace interface for replacement relative links.
 */
public class ContentReplaceRelative implements ContentReplace{

    private String toReplace;
    private String replaceWith;

    /**
     *
     * @param toReplace relative link to be replaced
     * @param replaceWith relative link to replace 1st parameter
     */
    public ContentReplaceRelative(String toReplace, String replaceWith) {
        this.toReplace = toReplace;
        this.replaceWith = replaceWith;
    }

    public ContentReplaceRelative(){

    }

    @Override
    public String applyReplacement(String data) {
        return data.replaceAll(toReplace, replaceWith);
    }

    @Override
    public String toString() {
        return "ContentReplaceRelative{" +
                "toReplace='" + toReplace + '\'' +
                ", replaceWith='" + replaceWith + '\'' +
                '}';
    }
}
