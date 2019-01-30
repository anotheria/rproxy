package net.anotheria.rproxy.refactor;

public class LocaleSpecialTarget {
    private String locale;
    private String customTarget;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getCustomTarget() {
        return customTarget;
    }

    public void setCustomTarget(String customTarget) {
        this.customTarget = customTarget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocaleSpecialTarget that = (LocaleSpecialTarget) o;

        if (locale != null ? !locale.equals(that.locale) : that.locale != null) return false;
        return customTarget != null ? customTarget.equals(that.customTarget) : that.customTarget == null;
    }

    @Override
    public int hashCode() {
        int result = locale != null ? locale.hashCode() : 0;
        result = 31 * result + (customTarget != null ? customTarget.hashCode() : 0);
        return result;
    }
}
