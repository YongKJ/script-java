package com.yongkj.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenUtil {


    private GenUtil() {
    }

    private static final ObjectMapper jsonObjectMapper = new ObjectMapper();
    private static final ObjectMapper xmlObjectMapper = new ObjectMapper(new XmlFactory());
    private static final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    public static <T> String jsonToXml(String jsonStr, Class<T> c) {
        try {
            T jsonValue = fromJsonString(jsonStr, c);
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.writeValueAsString(jsonValue);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String xmlToJson(String xmlStr) {
        try {
            JsonNode jsonNode = xmlObjectMapper.readTree(xmlStr);
            return jsonObjectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String timestampToStr(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        return localDateToStr(instant.atZone(ZoneId.systemDefault()).toLocalDate(), "yyyyMMdd");
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static <T> T fromJsonString(String strJson, Class<T> c) {
        T value = null;
        if (strJson != null) {
            try {
                value = jsonObjectMapper.readValue(strJson, c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static String toJsonString(Object object) {
        String json;
        try {
            json = jsonObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json generate error", e);
        }

        return json;
    }

    public static double round(double v, int scale) {
        return round(v, scale, 6);
    }

    public static double round(double v, int scale, int round_mode) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        } else {
            BigDecimal b = new BigDecimal(Double.toString(v));
            return b.setScale(scale, round_mode).doubleValue();
        }
    }

    public static String douToStr(Double value) {
        return value == null ? "" : BigDecimal.valueOf(value).toPlainString();
    }

    public static long ldtToTimestamp(LocalDateTime ldt) {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = ldt.atZone(zoneId).toInstant();
        return instant.toEpochMilli();
    }

    public static LocalDateTime timestampToLdt(long milliseconds) {
        return LocalDateTime.ofEpochSecond(milliseconds / 1000, 0, ZoneOffset.ofHours(8));
    }

    public static String getMd5Str(String dataStr) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(dataStr.getBytes());
            return bytesToHex(digest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static <T> List<T> getRetainData(Set<T> srcData, Set<T> desData) {
        return getRetainData(new ArrayList<>(srcData), new ArrayList<>(desData));
    }

    public static <T> List<T> getRetainData(List<T> srcData, List<T> desData) {
        List<T> tempData = new ArrayList<>(srcData);
        tempData.retainAll(desData);
        return tempData;
    }

    public static List<String> getStrLines(String str) {
        if (str == null) {
            return new ArrayList<>();
        }
        String lineBreak = str.contains("\r\n") ? "\r\n" : "\n";
        return Arrays.asList(str.split(lineBreak));
    }

    public static Double strToDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long strToLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer strToInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static int strToInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public static Boolean objToBoolean(Object value) {
        return value != null ? Boolean.parseBoolean(value.toString()) : null;
    }

    public static Long objToLong(Object value) {
        return value != null ? Long.parseLong(objToStr(value)) : null;
    }

    public static String objToStr(Object value) {
        return value != null ? value.toString() : "";
    }

    public static Integer objToInteger(Object value) {
        Integer result = 0;
        if (value != null) {
            String str = value.toString();

            try {
                result = Integer.parseInt(str);
            } catch (Exception var4) {
            }
        }

        return result;
    }

    public static boolean getBoolean(String key) {
        return Objects.equals(getValue(key), "true");
    }

    public static Object getObject(String key) {
        return getConfig(getProfile() + ".yaml").get(key);
    }

    public static String getValue(String key) {
        return objToStr(getConfig(getProfile() + ".yaml").get(key));
    }

    public static List<Map<String, Object>> getListMap(String key) {
        Object value = getConfig(getProfile() + ".yaml").get(key);
        if (value instanceof List) return (List<Map<String, Object>>) value;
        return new ArrayList<>();
    }

    public static LocalDate strToLocalDate(String dateStr) {
        return strToLocalDate(dateStr, "yyyy-MM-dd");
    }

    public static String localDateToStr(LocalDate date) {
        return localDateToStr(date, "yyyy-MM-dd");
    }

    public static Long strToTimestamp(String dateStr) {
        LocalDateTime dateTime = strToLocalDateTime(dateStr);
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.toInstant().getEpochSecond();
    }

    private static LocalDateTime strToLocalDateTime(String dateStr) {
        return strToLocalDateTime(dateStr, "yyyy-MM-dd HH:mm:ss");
    }

    private static LocalDateTime strToLocalDateTime(String dateStr, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(dateStr, formatter);
    }

    public static String localDateToStr(LocalDate date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return date.format(formatter);
    }

    public static LocalDate strToLocalDate(String dateStr, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(dateStr, formatter);
    }

    public static String localDateTimeToStr(LocalDateTime value) {
        return localDateTimeToStr(value, "yyyy-MM-dd HH:mm:ss");
    }

    public static String localDateTimeToStr(LocalDateTime value, String format) {
        String dateString;
        if (value == null) {
            dateString = "";
        } else {
            DateTimeFormatter formatDate = DateTimeFormatter.ofPattern(format);
            dateString = value.format(formatDate);
        }

        return dateString;
    }

    public static Map<String, Object> getMap(String key) {
        Object value = getConfig(getProfile() + ".yaml").get(key);
        if (value instanceof Map) return (Map<String, Object>) value;
        return new HashMap<>();
    }

    public static List<String> getList(String key) {
        Object value = getConfig(getProfile() + ".yaml").get(key);
        if (value instanceof List) return (List<String>) value;
        return new ArrayList<>();
    }

    public static Map getConfig(String config) {
        try {
            String path = getConfigPath(config);
            return yamlObjectMapper.readValue(new File(path), Map.class);
        } catch (Exception e) {
            e.printStackTrace();

            try {
                return yamlObjectMapper.readValue(new ClassPathResource(config).getInputStream(), Map.class);
            } catch (Exception exception) {
                exception.printStackTrace();

                return new HashMap<String, Object>();
            }
        }
    }

    public static void writeConfig(String config, Map<String, Object> mapData) {
        try {
            String path = getConfigPath(config);
            String content = yamlObjectMapper.writeValueAsString(mapData);
            content = content.substring(4);
            FileUtil.write(path, content);
        } catch (Exception e) {
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
        return name.substring(0, 1).toUpperCase() + FileUtil.replaceStr(matcher, String::toUpperCase, true);
    }

    public static String toLine(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1).replaceAll("([A-Z])", "-$1").toLowerCase();
    }

    public static String getProfile() {
        try {
            return toLine(Class.forName(FileUtil.getPackageName(false)).getSimpleName());
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

    public static boolean isNumeric(String str) {
        String pattern = "^[-+]?\\d+(\\.\\d+)?$";
        return str.matches(pattern);
    }

    public static Integer random(int min, int max) {
        try {
            TimeUnit.MILLISECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Random(System.currentTimeMillis()).nextInt(max - min + 1) + min;
    }

    public static double randomDouble(double min, double max) {
        try {
            TimeUnit.MILLISECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return min + new Random(System.currentTimeMillis()).nextDouble() * (max - min);
    }
}
