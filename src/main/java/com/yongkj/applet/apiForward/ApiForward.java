package com.yongkj.applet.apiForward;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiForward {

    private static String TOKEN = null;

    private ApiForward() {
    }

    public static Object getAdminTreeOneData(String applyCode) {
        if (TOKEN == null) {
            TOKEN = getPlatformToken();
        }
        return ApiController.adminTree1(TOKEN, applyCode);
    }

    private static String getPlatformToken() {
        Map<String, Object> mapData = ApiController.accountLogin(
                Global.getAPPLY(), Global.getMOBILE(), Global.getPASSWORD()
        );
        return GenUtil.objToStr(mapData.get("token"));
    }

    private void apply() {
        List<Map<String, Object>> lstData = (List<Map<String, Object>>) getAdminTreeOneData("platform");
        System.out.println("---------------------[ApiForward] apply -> lstData.size: " + lstData.size());
        System.out.println("---------------------[ApiForward] apply -> lstData: " + lstData);
        System.out.println("-------------------------------------------------------------------------------------------");
    }

    public static void run(String[] args) {
        new ApiForward().apply();
    }

}

class ApiController {

    private ApiController() {
    }

    public static List<Map<String, Object>> adminTree1(String token, String applyCode) {
        String responseData = ApiService.adminTree1(token, applyCode);
        Map<String, Object> mapData = GenUtil.toMap(responseData);
        return (List<Map<String, Object>>) mapData.get("data");
    }

    public static Map<String, Object> accountLogin(Integer apply, String mobile, String password) {
        String responseData = ApiService.accountLogin(apply, mobile, password);
        Map<String, Object> mapData = GenUtil.toMap(responseData);
        return (Map<String, Object>) mapData.get("data");
    }

}

class ApiService {

    private final static String BASE_URL = Global.getBaseHost();
    private final static String ADMIN_ONE = BASE_URL + "/api/user/v1/bc/admin/menu/tree/admin1";
    private final static String ACCOUNT_LOGIN = BASE_URL + "/api/user/v1/bc/company/admin/accountLogin";

    private ApiService() {
    }

    public static String adminTree1(String token, String applyCode) {
        Map<String, String> mapHeader = new HashMap<>();
        mapHeader.put("token", token);

        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("applyCode", applyCode);

        return ApiUtil.requestByGetWithParamsAndHeader(ADMIN_ONE, mapParams, mapHeader);
    }

    public static String accountLogin(Integer apply, String mobile, String password) {
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("apply", apply);
        mapData.put("mobile", mobile);
        mapData.put("password", password);
        return ApiUtil.requestByPostWithParamsAndMapData(ACCOUNT_LOGIN, new HashMap<>(), mapData);
    }

}

class GenUtil {

    private GenUtil() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T fromJsonString(String strJson, Class<T> c) {
        T value = null;
        if (strJson != null) {
            try {
                value = objectMapper.readValue(strJson, c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static String toJsonString(Object object) {
        String json;
        try {
            json = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json generate error", e);
        }

        return json;
    }

    private String toJsonStringPretty(Object object) {
        String json;
        try {
            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json generate error", e);
        }

        return json;
    }

    public static Map<String, Object> toMap(@Nullable String json) {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };
        Map<String, Object> map = new HashMap<>();
        if (json != null) {
            try {
                map = objectMapper.readValue(json, typeRef);
            } catch (IOException e) {
                throw new RuntimeException("json parse error:" + json, e);
            }
        }
        return map;
    }

    public static String objToStr(Object value) {
        return value != null ? value.toString() : "";
    }

}

class ApiUtil {

    private ApiUtil() {
    }

    private static boolean proxyEnable = Global.isProxyEnable();
    private static final RestTemplate REST_TEMPLATE = getRestTemplate(false);
    private static final RestTemplate SOCKS_REST_TEMPLATE = getRestTemplate(true);

    public static String requestByGetWithParams(String api, Map<String, Object> params) {
        return (proxyEnable ? SOCKS_REST_TEMPLATE : REST_TEMPLATE).getForObject(getUrl(api, params), String.class);
    }

    public static String requestByGetWithParamsAndHeader(String api, Map<String, Object> params, Map<String, String> mapHeader) {
        HttpHeaders headers = new HttpHeaders();
        mapHeader.forEach(headers::set);
        return requestByGetWithHeaderAndData(getUrl(api, params), headers, null, String.class);
    }

    public static String requestByPostWithParams(String api, Map<String, Object> params) {
        return (proxyEnable ? SOCKS_REST_TEMPLATE : REST_TEMPLATE).postForObject(getUrl(api, params), null, String.class);
    }

    public static String requestByPostWithHeaderAndMapData(String api, Map<String, String> mapHeader, Map<String, Object> data) {
        HttpHeaders headers = new HttpHeaders();
        mapHeader.forEach(headers::set);
        return requestByPostWithHeaderAndData(api, headers, GenUtil.toJsonString(data), String.class);
    }

    public static String requestByPostWithParamsAndMapData(String api, Map<String, Object> params, Map<String, Object> data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return requestByPostWithHeaderAndData(getUrl(api, params), headers, GenUtil.toJsonString(data), String.class);
    }

    public static <T> T requestByGetWithParamsToEntity(String api, Map<String, Object> params, Class<T> clazz) {
        return (proxyEnable ? SOCKS_REST_TEMPLATE : REST_TEMPLATE).getForObject(getUrl(api, params), clazz);
    }

    public static <T> T requestByPostWithParamsToEntity(String api, Map<String, Object> params, Class<T> clazz) {
        return (proxyEnable ? SOCKS_REST_TEMPLATE : REST_TEMPLATE).postForObject(getUrl(api, params), null, clazz);
    }

    private static <T> T requestByGetWithHeaderAndData(String url, HttpHeaders headers, Object data, Class<T> responseType) {
        return (proxyEnable ? SOCKS_REST_TEMPLATE : REST_TEMPLATE).exchange(url, HttpMethod.GET, new HttpEntity<>(data, headers), responseType).getBody();
    }

    private static <T> T requestByPostWithHeaderAndData(String url, HttpHeaders headers, Object data, Class<T> responseType) {
        return (proxyEnable ? SOCKS_REST_TEMPLATE : REST_TEMPLATE).exchange(url, HttpMethod.POST, new HttpEntity<>(data, headers), responseType).getBody();
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

    private static final int APPLY = 1;
    private static final int PROXY_PORT = 51837;
    private static final String MOBILE = "18888888888";
    private static final String PASSWORD = "123123";
    private static final boolean LOG_ENABLE = true;
    private static final boolean PROXY_ENABLE = false;
    private static final String PROXY_HOST = "127.0.0.1";
    private static final String BASE_HOST = "https://test.ydchun.com";

    public static String getBaseHost() {
        return BASE_HOST;
    }

    public static int getAPPLY() {
        return APPLY;
    }

    public static String getMOBILE() {
        return MOBILE;
    }

    public static String getPASSWORD() {
        return PASSWORD;
    }

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
