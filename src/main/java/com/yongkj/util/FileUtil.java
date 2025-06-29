package com.yongkj.util;

import com.yongkj.App;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String launchPath = getAppPath();
        if (launchPath.contains(".jar!")) {
            launchPath = launchPath.split("!")[0];
            launchPath = launchPath.replace("file:", "");
        }
        String appDir = FileUtil.dirname(launchPath);
        if (isProd) return appDir;
        return FileUtil.dirname(appDir);
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

    public static String getAbsPath(boolean isProd, String... names) {
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
        String lineBreak = System.getProperty("os.name").contains("dows") ? "\r\n" : "\n";
        try {
//            FileReader reader = new FileReader(path);
            InputStream inputStream = path.startsWith("/") || path.startsWith("\\") ?
                    new ClassPathResource(path).getInputStream() :
                    Files.newInputStream(Paths.get(path));
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bReader = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            String s = "";
            while ((s = bReader.readLine()) != null) {
                sb.append(s).append(lineBreak);
            }
            bReader.close();
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void write(String fileName, String content) {
        try {
//            FileWriter writeFile = new FileWriter(fileName, StandardCharsets.UTF_8);
            FileOutputStream outputStream = new FileOutputStream(fileName);
            OutputStreamWriter writeFile = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(writeFile);
            writer.write(content);
            writer.flush();
            writeFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<File> list(String folder) {
        File[] files = new File(folder).listFiles();
        if (files == null) return new ArrayList<>();
        Arrays.sort(files, (f1, f2) -> {
            long diff = f1.lastModified() - f2.lastModified();
            if (diff > 0) {
                return -1;
            } else if (diff == 0) {
                return 0;
            } else {
                return 1;
            }
        });
        return Arrays.asList(files);
    }

    public static boolean isEmpty(String fileName) {
        return Objects.requireNonNull((new File(fileName)).listFiles()).length == 0;
    }

    public static void create(String fileName) {
        File file = new File(fileName);
        if (file.exists()) return;
        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mkdir(String fileName) {
        File file = new File(fileName);
        if (file.exists()) return;
        file.mkdirs();
    }

    public static boolean isFolder(String fileName) {
        return new File(fileName).isDirectory();
    }

    public static void copy(String srcFileName, String desFileName) {
        if (isFolder(srcFileName)) {
            mkdir(desFileName);
            copyFolder(srcFileName, desFileName);
        } else {
            try {
                FileChannel sourceChannel = new FileInputStream(srcFileName).getChannel();
                FileChannel destChannel = new FileOutputStream(desFileName).getChannel();
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                sourceChannel.close();
                destChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyFolder(String srcFolderName, String desFolderName) {
        List<File> lstFile = list(srcFolderName);
        for (File file : lstFile) {
            String srcNewFileName = srcFolderName + File.separator + file.getName();
            String desNewFileName = desFolderName + File.separator + file.getName();
            if (isFolder(srcNewFileName)) {
                mkdir(desNewFileName);
                copyFolder(srcNewFileName, desNewFileName);
            } else {
                copy(srcNewFileName, desNewFileName);
            }
        }
    }

    public static void move(String srcFileName, String desFileName) {
        if (isFolder(srcFileName)) {
            mkdir(desFileName);
            moveFolder(srcFileName, desFileName);
        } else {
            new File(srcFileName).renameTo(new File(desFileName));
        }
    }

    private static void moveFolder(String srcFolderName, String desFolderName) {
        List<File> lstFile = list(srcFolderName);
        for (File file : lstFile) {
            String srcNewFileName = srcFolderName + File.separator + file.getName();
            String desNewFileName = desFolderName + File.separator + file.getName();
            if (isFolder(srcNewFileName)) {
                mkdir(desNewFileName);
                moveFolder(srcNewFileName, desNewFileName);
            } else {
                move(srcNewFileName, desNewFileName);
            }
        }
        if (isEmpty(srcFolderName)) delete(srcFolderName);
    }

    public static void delete(String fileName) {
        File file = new File(fileName);
        if (!file.isDirectory()) {
            if (file.exists()) file.delete();
        } else {
            deleteFolder(fileName);
        }
    }

    private static void deleteFolder(String fileName) {
        List<File> lstFile = list(fileName);
        for (File file : lstFile) {
            if (file.isFile()) {
                delete(file.getAbsolutePath());
            } else {
                deleteFolder(file.getAbsolutePath());
            }
        }
        new File(fileName).delete();
    }

    public static void modFile(String path, String regStr, String value) {
        modFile(path, regStr, value, false);
    }

    public static void modFile(String path, String regStr, String value, boolean isAll) {
        modFile(path, regStr, str -> value, isAll);
    }

    public static void modFile(String path, String regStr, Function<String, String> valueFunc, boolean isAll) {
        String content = read(path);
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(content);
        if (isAll) {
//            content = matcher.replaceAll(str -> Matcher.quoteReplacement(valueFunc.apply(str.group(1))));
            content = replaceStr(matcher, valueFunc, isAll);
        } else {
//            content = matcher.replaceFirst(str -> Matcher.quoteReplacement(valueFunc.apply(str.group(1))));
            content = replaceStr(matcher, valueFunc, isAll);
        }
        write(path, content);
    }

    public static String replaceStr(Matcher matcher, Function<String, String> valueFunc, boolean isAll) {
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(
                    matcher.group(1) == null ? matcher.group() : valueFunc.apply(matcher.group(1))
            ));
            if (!isAll) break;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static void modContent(String path, String regStr, String value) {
        modContent(path, regStr, value, false);
    }

    public static void modContent(String path, String regStr, String value, boolean isAll) {
        modify(path, regStr, str -> value, isAll);
    }

    public static void modify(String path, String regStr, Function<String, String> valueFunc) {
        modify(path, regStr, valueFunc, false);
    }

    public static void modify(String path, String regStr, Function<String, String> valueFunc, boolean isAll) {
        String content = read(path);
        String contentBreak = content.contains("\r\n") ? "\r\n" : "\n";
        String[] contentArray = content.split(contentBreak);
        Pattern pattern = Pattern.compile(regStr);
        for (int i = 0; i < contentArray.length; i++) {
            String line = contentArray[i];
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) continue;
            String value = valueFunc.apply(matcher.group(1));
            contentArray[i] = line.replace(matcher.group(1), value);
            if (!isAll) break;
        }
        write(path, String.join(contentBreak, contentArray));
    }

}
