package net.anotheria.rproxy.conf;

/**
 * Content replacement for html.
 */
public interface ContentReplace {

    /**
     * Applies replacement
     * @param data html document
     * @return html document with applied replacement
     */
    String applyReplacement(String data);
}
