package net.anotheria.rproxy.refactor;

import net.anotheria.rproxy.getter.HttpProxyResponse;

public final class RProxyFactory {

    private static RProxy<String, HttpProxyResponse> DEFAULT_INSTANCE = new RProxy<>();

    public static RProxy<String, HttpProxyResponse> getInstance() {
        return DEFAULT_INSTANCE;
    }
}
