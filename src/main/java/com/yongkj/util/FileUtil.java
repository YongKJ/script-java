package com.yongkj.util;

import com.yongkj.App;
import com.yongkj.pojo.dto.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtil {

    private FileUtil() {
    }

    private static String appDir = "";

    public static String appDir() {
        if (FileUtil.appDir.length() > 0) return FileUtil.appDir;
        FileUtil.appDir = FileUtil.appDir(false);
        return FileUtil.appDir;
    }

    public static String appDir(boolean isProd) {
        String launchName = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String appDir = FileUtil.dirname(launchName);
        if (isProd) return appDir;
        return FileUtil.dirname(appDir);
    }

    public static String getAbsPath(boolean isProd, String ...names) {
        StringBuilder path = new StringBuilder(FileUtil.appDir(isProd));
        String separator = path.toString().contains("/") ? "/" : "\\";
        for (String name : names) {
            path.append(separator).append(name);
        }
        return path.toString();
    }

    public static String dirname(String path) {
        if (path.length() == 0) return path;
        char lastChar = path.charAt(path.length() - 1);
        if (lastChar == '/' || lastChar == '\\') {
            path = path.substring(0, path.length() - 1);
        }
        String separator = path.contains("/") ? "/" : "\\";
        int index = path.lastIndexOf(separator);
        return path.substring(0, index);
    }

    public static boolean exist(String path) {
        return new File(path).exists();
    }

    public static String read(String path) {
        try {
            FileReader reader = new FileReader(path);
            BufferedReader bReader = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            String s = "";
            while ((s = bReader.readLine()) != null) {
                sb.append(s).append("\n");
            }
            bReader.close();
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            LogUtil.loggerLine(Log.of("FileUtil", "read", "e", e));
            e.printStackTrace();
            return "";
        }
    }

    public static void write(String fileName, String content) {
        try {
            FileWriter writeFile = new FileWriter(fileName, StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(writeFile);
            writer.write(content);
            writer.flush();
            writeFile.close();
        } catch (Exception e) {
            LogUtil.loggerLine(Log.of("FileUtil", "write", "e", e));
            e.printStackTrace();
        }
    }

}
