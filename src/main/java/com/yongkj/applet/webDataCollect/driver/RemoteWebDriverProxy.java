package com.yongkj.applet.webDataCollect.driver;

import com.yongkj.util.GenUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RemoteWebDriverProxy extends RemoteWebDriver {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int COMMAND_TIMEOUT = 5000;
    private static String CHROME_DRIVER_IP;
    private static int CHROME_DRIVER_PORT;

    public RemoteWebDriverProxy(URL remoteAddress, Capabilities capabilities) {
        super(remoteAddress, capabilities);
        CHROME_DRIVER_IP = remoteAddress.getHost();
        CHROME_DRIVER_PORT = remoteAddress.getPort();
    }

    // 根据请求ID获取返回内容
    public String getResponseBody(String requestId) {
        try {
            // CHROME_DRIVER_PORT chromeDriver提供的端口
            String url = String.format(
                    "http://" + CHROME_DRIVER_IP + ":%s/session/%s/goog/cdp/execute",
                    CHROME_DRIVER_PORT, getSessionId());

            Map<String, Object> mapData = new HashMap<>();
            Map<String, Object> mapParams = new HashMap<>();
            mapData.put("cmd", "Network.getResponseBody");
            mapParams.put("requestId", requestId);
            mapData.put("params", mapParams);

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(GenUtil.toJsonString(mapData)));

            RequestConfig requestConfig = RequestConfig
                    .custom()
                    .setSocketTimeout(COMMAND_TIMEOUT)
                    .setConnectTimeout(COMMAND_TIMEOUT).build();

            CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig).build();

            HttpResponse response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            logger.error("getResponseBody failed!", e);
        }

        return "";
    }

}
