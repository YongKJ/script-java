package com.yongkj.util;

import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import java.util.List;

public class ModelUtil {

    private static final String API_KEY = "sk-08df77ce331045b0863b844e22ac1b5f";
    private static final String APPLICATION_ID = "a92439964e5341f0b5e38ce112640bd7";


    public static String mdContent(String prompt) {
        return getAi(prompt, null);
    }

    public static String aiItemComment(String prompt, List<String> images) {
        return getAi(prompt, images);
    }

    public static String aiTraining(String prompt) {
        return getAi(prompt, null);
    }

    private static String getAi(String prompt, List<String> images) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "";
        }
        ApplicationParam param;
        if (images != null && !images.isEmpty()) {
            param = getApplicationParam(prompt, images);
        } else {
            param = getApplicationParam(prompt);
        }
        Application application = new Application();
        ApplicationResult result;
        try {
            result = application.call(param);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
        if (result != null && result.getOutput() != null) {
            return result.getOutput().getText();
        }
        return "";
    }

    private static ApplicationParam getApplicationParam(String prompt) {
        return ApplicationParam.builder()
                .appId(APPLICATION_ID)
                .apiKey(API_KEY)
                .prompt(prompt)
                .build();
    }

    private static ApplicationParam getApplicationParam(String prompt, List<String> images) {
        return ApplicationParam.builder()
                .appId(APPLICATION_ID)
                .apiKey(API_KEY)
                .prompt(prompt)
                .images(images)
                .build();
    }

}
