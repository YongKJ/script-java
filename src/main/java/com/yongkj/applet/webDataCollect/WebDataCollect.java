package com.yongkj.applet.webDataCollect;

import com.yongkj.applet.webDataCollect.driver.ChromeDriverProxy;
import com.yongkj.applet.webDataCollect.pojo.dto.NetWorkRecord;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.*;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpResponse;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class WebDataCollect {

    private final String webUrl;
    private final String chromeDriver;
    private final String chromeBinary;
    private final String remoteChrome;

    private static final String LOGGING_PREFS = "goog:loggingPrefs";
    private static final Map<String, List<NetWorkRecord>> mapRecord = new HashMap<>();
    private static final String dataUrl = "https://ad.qq.com/api/v3.0/integrated_list_multiaccount/get";

    private WebDataCollect() {
        Map<String, Object> mapData = GenUtil.getMap("web-data-collect");
//        this.webUrl = "https://www.baidu.com";
        this.webUrl = mapData.get("web-url").toString();
        this.chromeDriver = mapData.get("chrome-driver").toString();
        this.chromeBinary = mapData.get("chrome-binary").toString();
        this.remoteChrome = mapData.get("remote-chrome").toString();
    }

    private void apply() {
        testWebUrl();
    }

    private void testWebUrl() {
        System.setProperty("webdriver.chrome.driver", chromeDriver);
        ChromeOptions options = getOption();
        ChromeDriverProxy driver = new ChromeDriverProxy(options);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().window().maximize();

//        DevTools devTools = driver.getDevTools();
//        devTools.createSessionIfThereIsNotOne();
//        devTools.getDomains().network().interceptTrafficWith(this::getFilter);

        try {
            driver.get(webUrl);

            TimeUnit.MINUTES.sleep(2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

//        for (Map.Entry<String, List<NetWorkRecord>> map : mapRecord.entrySet()) {
//            if (map.getKey().contains(dataUrl)) {
//                int index = map.getValue().size() - 1;
//                parseNetwork(map.getValue().get(index));
//            }
//            LogUtil.loggerLine(Log.of("WebDataCollect", "testWebUrl", "uri", map.getKey()));
//        }

        checkLogData(driver.manage().logs());
    }

    private void checkLogData(Logs logs) {
//        Set<String> availableLogTypes = logs.getAvailableLogTypes();
//        if (!availableLogTypes.contains(LogType.PERFORMANCE)) {
//            return;
//        }

        LogEntries logEntries = logs.get(LogType.PERFORMANCE);
        List<String> lstMessage = new ArrayList<>();
        for (LogEntry entry : logEntries) {
            lstMessage.add(entry.getMessage());
        }
        String content = GenUtil.toJsonString(lstMessage);
        FileUtil.write("C:\\Users\\Admin\\Desktop\\logs.json", content);
    }

    private HttpHandler getFilter(HttpHandler next) {
        return request -> {
            try {
                long startTime = System.currentTimeMillis();   //获取开始时间
                HttpResponse response = next.execute(request);
                long endTime = System.currentTimeMillis();   //获取结束时间

                NetWorkRecord record = new NetWorkRecord();
                record.setEndTime(endTime);
                record.setRequest(request);
                record.setResponse(response);
                record.setStartTime(startTime);

                String uri = request.getUri();
                if (!mapRecord.containsKey(uri)) {
                    mapRecord.put(uri, new ArrayList<>());
                }
                mapRecord.get(uri).add(record);

                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new HttpResponse();
        };
    }

    private ChromeOptions getOption() {
        LoggingPreferences logOption = new LoggingPreferences();
        logOption.enable(LogType.PERFORMANCE, Level.ALL);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("no-default-browser-check");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("no-sandbox");

        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.setCapability(LOGGING_PREFS, logOption);
        options.setBinary(chromeBinary);

        return options;
    }

    public static void parseNetwork(NetWorkRecord record) {
        long cost = record.getEndTime() - record.getStartTime();
        if (cost > 3000) {
            LogUtil.loggerLine(Log.of("WebDataCollect", "parseNetwork", "msg", "网络监控-慢响应：" + record.getRequest().toString()));
        }
        if (!record.getResponse().isSuccessful()) {
            LogUtil.loggerLine(Log.of("WebDataCollect", "parseNetwork", "msg", "网络监控-慢响应：" + "网络监控-失败响应：" + record));
            return;
        }
        if (Objects.isNull(record.getResponse().getHeader("Content-Type"))) {
            return;
        }

        if (record.getResponse().getHeader("Content-Type").contains("application/json") && record.getResponse().getStatus() == 200) {
            String bodyStr = Contents.string(record.getResponse());
            LogUtil.loggerLine(Log.of("WebDataCollect", "parseNetwork", "bodyStr", bodyStr));
        }
    }

    public static void run(String[] args) {
        new WebDataCollect().apply();
    }

}
