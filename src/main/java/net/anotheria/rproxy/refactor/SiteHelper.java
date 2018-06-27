package net.anotheria.rproxy.refactor;


public class SiteHelper {
    private URLHelper sourceUrlHelper;
    private URLHelper targetUrlHelper;

    public SiteHelper(URLHelper sourceUrlHelper, URLHelper targetUrlHelper) {
        this.sourceUrlHelper = sourceUrlHelper;
        this.targetUrlHelper = targetUrlHelper;
    }

    public SiteHelper() {
    }

    public URLHelper getSourceUrlHelper() {
        return sourceUrlHelper;
    }

    public void setSourceUrlHelper(URLHelper sourceUrlHelper) {
        this.sourceUrlHelper = sourceUrlHelper;
    }

    public URLHelper getTargetUrlHelper() {
        return targetUrlHelper;
    }

    public void setTargetUrlHelper(URLHelper targetUrlHelper) {
        this.targetUrlHelper = targetUrlHelper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SiteHelper that = (SiteHelper) o;

        if (sourceUrlHelper != null ? !sourceUrlHelper.equals(that.sourceUrlHelper) : that.sourceUrlHelper != null)
            return false;
        return targetUrlHelper != null ? targetUrlHelper.equals(that.targetUrlHelper) : that.targetUrlHelper == null;
    }

    @Override
    public int hashCode() {
        int result = sourceUrlHelper != null ? sourceUrlHelper.hashCode() : 0;
        result = 31 * result + (targetUrlHelper != null ? targetUrlHelper.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SiteHelper{" +
                "sourceUrlHelper=" + sourceUrlHelper +
                ", targetUrlHelper=" + targetUrlHelper +
                '}';
    }
}
