package com.yongkj.applet.demo;


import com.yongkj.util.GenUtil;
import okhttp3.*;
import okio.ByteString;

import java.io.IOException;
import java.util.*;

public class TuYaService {

    private static String TOKEN = null;
    private static final String ZONE_ID = "997";
    private static final String MOBILE = "17329928872";
    private static List<String> COMMANDS = new ArrayList<>();

    private static final String BASE_URL_USER = "https://test.baochuncare.com/api/user";
    private static final String BASE_URL_SMART = "https://test.baochuncare.com/api/smart";
    private static final String GET_TUYA_COMMANDS = BASE_URL_SMART + "/v1/bc/app/ai/getTuyaCommands";
    private static final String SEND_TUYA_COMMAND = BASE_URL_SMART + "/v1/bc/app/ai/sendTuyaCommand";
    private static final String MOBILE_CODE_LOGIN = BASE_URL_USER + "/v1/bc/public/app/user/mobileCodeLogin";


    public Boolean sendTuyaCommand(String cmd, String args) {
        Map<String, String> mapHeader = new HashMap<>();
        mapHeader.put("token", getToken());

        Map<String, Object> mapBody = new HashMap<>();
        mapBody.put("zoneId", ZONE_ID);
        mapBody.put("args", args);
        mapBody.put("cmd", cmd);

        Map<String, Object> mapResponse = GenUtil.fromJsonString(sendPost(SEND_TUYA_COMMAND, mapHeader, mapBody), Map.class);
        if (mapResponse != null && mapResponse.containsKey("data")) {
            return (Boolean) mapResponse.get("data");
        }
        return null;
    }

    public List<String> getCommands() {
        if (!COMMANDS.isEmpty()) {
            System.out.println("[TuYaService] getCommands -> COMMANDS: " + COMMANDS);
            return COMMANDS;
        }
        COMMANDS = getTuyaCommands();
        System.out.println("[TuYaService] getCommands -> COMMANDS: " + COMMANDS);
        return COMMANDS;
    }

    private List<String> getTuyaCommands() {
        Map<String, String> mapHeader = new HashMap<>();
        mapHeader.put("token", getToken());

        Map<String, Object> mapResponse = GenUtil.fromJsonString(sendGet(GET_TUYA_COMMANDS, mapHeader), Map.class);
        if (mapResponse != null && mapResponse.containsKey("data")) {
            return (List<String>) mapResponse.get("data");
        }
        return new ArrayList<>();
    }

    public static String sendGet(String url, Map<String, String> mapHeader) {
        String result = null;
        OkHttpClient okHttpClient = new OkHttpClient();
        String token = mapHeader.get("token");
        Request request = new Request.Builder()
                .url(url)
                .addHeader("token", Optional.ofNullable(token).orElse(""))
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getToken() {
        if (TOKEN != null) {
            System.out.println("[TuYaService] getToken -> TOKEN: " + TOKEN);
            return TOKEN;
        }

        Map<String, Object> mapData = mobileCodeLogin();
        String token = mapData.containsKey("token") ? Objects.requireNonNull(mapData.get("token")).toString() : "";
        if (token != null && token.length() > 0) {
            TOKEN = token;
        }
        System.out.println("[TuYaService] getToken -> TOKEN: " + TOKEN);
        return TOKEN;
    }

    private Map<String, Object> mobileCodeLogin() {
        Map<String, Object> mapBody = new HashMap<>();
        mapBody.put("mobile", MOBILE);
        mapBody.put("code", 8888);

        Map<String, Object> mapResponse = GenUtil.fromJsonString(sendPost(MOBILE_CODE_LOGIN, new HashMap<>(), mapBody), Map.class);
        if (mapResponse != null && mapResponse.containsKey("data")) {
            return (Map<String, Object>) mapResponse.get("data");
        }
        return new HashMap<>();
    }

    public static String sendPost(String url, Map<String, String> mapHeader, Map<String, Object> mapBody){
        String result = null;
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建一个请求对象
        ByteString byteString =
                ByteString.encodeUtf8(GenUtil.toJsonString(mapBody));
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), byteString);

        String token = mapHeader.get("token");
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .addHeader("token", Optional.ofNullable(token).orElse(""))
                .url(url)
                .post(requestBody)
                .build();
        //发送请求获取响应
        try {
            Response response = okHttpClient.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[TuYaService] sendPost -> result: " + result);
        return result;
    }

}
