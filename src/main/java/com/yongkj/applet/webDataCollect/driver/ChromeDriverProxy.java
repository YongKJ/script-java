package com.yongkj.applet.webDataCollect.driver;

import com.yongkj.util.GenUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChromeDriverProxy extends ChromeDriver {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int COMMAND_TIMEOUT = 5000;
    // 必须固定端口，因为ChromeDriver没有实时获取端口的接口；
    private static final int CHROME_DRIVER_PORT = 9999;
    private static final ChromeDriverService driverService = new ChromeDriverService.Builder()
            .usingPort(CHROME_DRIVER_PORT)
            .build();

    public ChromeDriverProxy(ChromeOptions options) {
        super(driverService, options);
    }

    // 根据请求ID获取返回内容
    public String getResponseBody(String requestId) {
        try {
            // CHROME_DRIVER_PORT chromeDriver提供的端口
            String url = String.format(
                    "http://localhost:%s/session/%s/goog/cdp/execute",
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

