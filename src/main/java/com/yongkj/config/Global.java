package com.yongkj.config;

public class Global {

    private Global() {
    }

    private static final int PROXY_PORT = 51837;
    private static final boolean LOG_ENABLE = true;
    private static final boolean PROXY_ENABLE = false;
    private static final String PROXY_HOST = "127.0.0.1";

    public static boolean isProxyEnable() {
        return PROXY_ENABLE;
    }

    public static int getProxyPort() {
        return PROXY_PORT;
    }

    public static String getProxyHost() {
        return PROXY_HOST;
    }

    public static boolean isLogEnable() {
        return LOG_ENABLE;
    }
}
