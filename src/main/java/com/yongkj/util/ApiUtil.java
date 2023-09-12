package com.yongkj.util;

import com.yongkj.config.Global;
import com.yongkj.config.PlainConnectionSocketFactoryConfig;
import com.yongkj.config.SSLConnectionSocketFactoryConfig;
import com.yongkj.pojo.dto.Log;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class ApiUtil {

    private ApiUtil() {
    }

    private static boolean proxyEnable = Global.isProxyEnable();
    private static final RestTemplate REST_TEMPLATE = getRestTemplate(false);
    private static final RestTemplate SOCKS_RESTTEMPLATE = getRestTemplate(true);

    public static String requestByGetWithParams(String api, Map<String, Object> params) {
        return (proxyEnable ? SOCKS_RESTTEMPLATE : REST_TEMPLATE).getForObject(getUrl(api, params), String.class);
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

    private <T> T requestByGetWithHeaderAndData(String url, HttpHeaders headers, Object data, Class<T> responseType) {
        return (proxyEnable ? SOCKS_RESTTEMPLATE : REST_TEMPLATE).exchange(url, HttpMethod.GET, new HttpEntity<>(data, headers), responseType).getBody();
    }

    private <T> T requestByPostWithHeaderAndData(String url, HttpHeaders headers, Object data, Class<T> responseType) {
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
        LogUtil.loggerLine(Log.of("ApiUtil", "getUrl", "api", api));
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
