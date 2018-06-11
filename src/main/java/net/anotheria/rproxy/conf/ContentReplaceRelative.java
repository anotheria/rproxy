package net.anotheria.rproxy.conf;

public class ContentReplaceRelative implements ContentReplace{

    private String toReplace;
    private String replaceWith;

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
