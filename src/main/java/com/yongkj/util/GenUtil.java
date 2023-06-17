package com.yongkj.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.yongkj.App;
import com.yongkj.pojo.dto.Log;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenUtil {


    private GenUtil() {
    }

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static int strToInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String objToStr(Object value) {
        return value != null ? value.toString() : "";
    }

    public static String getValue(String key) {
        return objToStr(getConfig(getProfile() + ".yaml").get(key));
    }

    public static Map getConfig(String config) {
        try {
            String path = getConfigPath(config);
            return mapper.readValue(new File(path), Map.class);
        } catch (Exception e) {
            LogUtil.loggerLine(Log.of("GenUtil", "getConfig", "e", e));
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private static String getConfigPath(String config) {
        String tempConfig = "application-" + config;
        String path = FileUtil.getAbsPath(true, config);
        if (!FileUtil.exist(path)) {
            path = FileUtil.getAbsPath(true, tempConfig);
        }
        if (!FileUtil.exist(path)) {
            path = FileUtil.getAbsPath(false, "src", "main", "resources", config);
        }
        if (!FileUtil.exist(path)) {
            path = FileUtil.getAbsPath(false, "src", "main", "resources", tempConfig);
        }
        return path;
    }

    public static String toHump(String name) {
//        return name.substring(0, 1).toUpperCase() + Pattern.compile("\\-(\\w)").matcher(name.substring(1)).replaceAll(str -> str.group(1).toUpperCase());
        String tempName = name.substring(1);
        Pattern pattern = Pattern.compile("\\-(\\w)");
        Matcher matcher = pattern.matcher(tempName);
        return name.substring(0, 1).toUpperCase() + replaceStr(matcher, String::toUpperCase, true);
    }

    public static String replaceStr(Matcher matcher, Function<String, String> valueFunc, boolean isAll) {
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            if (matcher.group(1) == null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(valueFunc.apply(matcher.group(1))));
            }
            if (!isAll) break;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String toLine(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1).replaceAll("([A-Z])", "-$1").toLowerCase();
    }

    public static String getProfile() {
        try {
            return toLine(Class.forName(getPackageName(false)).getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getAppPath() {
        try {
            return Class.forName(getPackageName(true))
                    .getProtectionDomain().getCodeSource().getLocation().getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getPackageName(boolean isLaunchClass) {
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            int index = elements.length - 1;
            for (int i = elements.length - 1; i >= 0; i--) {
                if (!elements[i].getClassName().equals(App.class.getName())) continue;
                index = i;
                break;
            }
            return elements[isLaunchClass ? index : index - 1].getClassName();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void println() {
        System.out.println();
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public static void print(String str) {
        System.out.print(str);
    }

    public static List<String> readParams() {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        return strToList(line);
    }

    public static List<String> strToList(String str) {
        return strToList(str, " ");
    }

    public static List<String> strToList(String str, String separator) {
        if (str.length() == 0) return new ArrayList<>();
        return Arrays.asList(str.trim().split(separator));
    }

}
