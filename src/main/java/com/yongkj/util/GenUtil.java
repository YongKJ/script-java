package com.yongkj.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.yongkj.pojo.dto.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GenUtil {


    private GenUtil() {
    }

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static String objToStr(Object value) {
        return value != null ? value.toString() : "";
    }

    public static Object getValue(String config, String key) {
        return getConfig(config).get(key);
    }

    public static Map getConfig(String config) {
        try {
            String path = getConfigPath(config);
            return mapper.readValue(new File(path), Map.class);
        } catch (Exception e) {
            LogUtil.loggerLine(Log.of("GenUtil", "getConfig", "e", e));
            return new HashMap<String, Object>();
        }
    }

    public static void writeConfig(String config, Map<String, Object> mapData) {
        try {
            String path = getConfigPath(config);
            String content = mapper.writeValueAsString(mapData);
            content = content.substring(4);
            FileUtil.write(path, content);
        } catch (Exception e) {
            LogUtil.loggerLine(Log.of("GenUtil", "writeConfig", "e", e));
        }
    }

    private static String getConfigPath(String config) {
        String path = FileUtil.getAbsPath(true, config);
        if (!FileUtil.exist(path)) {
            path = FileUtil.getAbsPath(false, "src", "main", "resources", config);
        }
        return path;
    }

}
