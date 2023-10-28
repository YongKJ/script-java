package com.yongkj.applet.apiForward;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Map;

public class ApiForward {


}

class ApiUtil {

    private ApiUtil() {
    }

    private static boolean proxyEnable = Global.isProxyEnable();
    private static final RestTemplate REST_TEMPLATE = getRestTemplate(false);
    private static final RestTemplate SOCKS_RESTTEMPLATE = getRestTemplate(true);

    public static String requestByGetWithParams(String api, Map<String, Object> params) {
        return (proxyEnable ? SOCKS_RESTTEMPLATE : REST_TEMPLATE).getForObject(getUrl(api, params), String.class);
    }

    public static String requestByGetWithHeaderParams(String api, Map<String, Object> params, Map<String, String> mapHeader) {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, String> map : mapHeader.entrySet()) {
            headers.set(map.getKey(), map.getValue());
        }
        return requestByGetWithHeaderAndData(getUrl(api, params), headers, null, String.class);
    }

    public static <T> T requestByGetWithParamsToEntity(String api, Map<String, Object> params, Class<T> clazz) {
        return (proxyEnable ? SOCKS_RESTTEMPLATE : REST_TEMPLATE).getForObject(getUrl(api, params), clazz);
    }

    public static String requestByPostWithParams(String api, Map<String, Object> params) {
        return (proxyEnable ? SOCKS_RESTTEMPLATE : REST_TEMPLATE).postForObject(getUrl(api, params), null, String.class);
    }

    public static <T> T requestByPostWithParamsToEntity(String api, Map<String, Object> params, Class<T> clazz) {
        return (proxyEnable ? SOCKS_RESTTEMPLATE : REST_TEMPLATE).postForObject(getUrl(api, params), null, clazz);
    }

    private static <T> T requestByGetWithHeaderAndData(String url, HttpHeaders headers, Object data, Class<T> responseType) {
        return (proxyEnable ? SOCKS_RESTTEMPLATE : REST_TEMPLATE).exchange(url, HttpMethod.GET, new HttpEntity<>(data, headers), responseType).getBody();
    }

    private static <T> T requestByPostWithHeaderAndData(String url, HttpHeaders headers, Object data, Class<T> responseType) {
        return (proxyEnable ? SOCKS_RESTTEMPLATE : REST_TEMPLATE).exchange(url, HttpMethod.POST, new HttpEntity<>(data, headers), responseType).getBody();
    }

    public static String getUrl(String api, Map<String, Object> params) {
        StringBuilder apiBuilder = new StringBuilder(api);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            apiBuilder.append(apiBuilder.toString().contains("?") ? "&" : "?")
                    .append(entry.getKey()).append("=")
                    .append(entry.getValue() == null ? "" : entry.getValue());
        }
        api = apiBuilder.toString();
        api = api.replaceAll("#", "%23");
        return api;
    }

    private static RestTemplate getRestTemplate(boolean proxyEnable) {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", proxyEnable ? new SSLConnectionSocketFactoryConfig() : SSLConnectionSocketFactory.getSocketFactory())
                .register("http", proxyEnable ? new PlainConnectionSocketFactoryConfig() : PlainConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(100);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(3000)
                .build();
        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    public static void setProxyEnable(boolean proxyEnable) {
        ApiUtil.proxyEnable = proxyEnable;
    }
}

class SSLConnectionSocketFactoryConfig extends SSLConnectionSocketFactory {

    public SSLConnectionSocketFactoryConfig() {
        super(SSLContexts.createSystemDefault());
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        InetSocketAddress socksAddr = new InetSocketAddress(Global.getProxyHost(), Global.getProxyPort());
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksAddr);
        return new Socket(proxy);
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
        InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
        return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
    }
}

class PlainConnectionSocketFactoryConfig extends PlainConnectionSocketFactory {

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        InetSocketAddress socksAddr = new InetSocketAddress(Global.getProxyHost(), Global.getProxyPort());
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksAddr);
        return new Socket(proxy);
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
        InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
        return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
    }
}

class Global {

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
