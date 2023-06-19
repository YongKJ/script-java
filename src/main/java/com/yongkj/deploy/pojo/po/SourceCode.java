package com.yongkj.deploy.pojo.po;

import com.yongkj.util.FileUtil;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceCode {

    private String path;
    private String content;
    private boolean hasSpring;
    private String simpleName;
    private String packageName;
    private Set<String> internalSimpleNames;
    private Set<String> internalPackageNames;
    private Set<String> externalPackageNames;

    public SourceCode() {
        this.path = "";
        this.content = "";
        this.simpleName = "";
        this.packageName = "";
        this.hasSpring = false;
        this.internalSimpleNames = new HashSet<>();
        this.internalPackageNames = new HashSet<>();
        this.externalPackageNames = new HashSet<>();
    }

    public SourceCode(String path, String content, boolean hasSpring, String simpleName, String packageName, Set<String> externalPackageNames, Set<String> internalPackageNames, Set<String> internalSimpleNames) {
        this.path = path;
        this.content = content;
        this.hasSpring = hasSpring;
        this.simpleName = simpleName;
        this.packageName = packageName;
        this.internalSimpleNames = internalSimpleNames;
        this.internalPackageNames = internalPackageNames;
        this.externalPackageNames = externalPackageNames;
    }

    public static SourceCode of(String path, String content, boolean hasSpring, String simpleName, String packageName, Set<String> externalPackageNames, Set<String> internalPackageNames, Set<String> internalSimpleNames) {
        return new SourceCode(path, content, hasSpring, simpleName, packageName, externalPackageNames, internalPackageNames, internalSimpleNames);
    }

    public static Map<String, SourceCode> get() {
        String path = FileUtil.getAbsPath(false, "src", "main", "java", "com", "yongkj");
        String separator = path.contains("/") ? "/" : "\\";
        Map<String, SourceCode> mapCodes = new HashMap<>();
        List<File> lstFile = FileUtil.list(path);
        for (File file : lstFile) {
            if (file.isFile()) continue;
            String folder = path + separator + file.getName();
            mapCodes.putAll(analyzeFolder(folder));
        }
        return mapCodes;
    }

    private static Map<String, SourceCode> analyzeFolder(String folder) {
        String separator = folder.contains("/") ? "/" : "\\";
        Map<String, SourceCode> mapCodes = new HashMap<>();
        List<File> lstFile = FileUtil.list(folder);
        for (File file : lstFile) {
            String path = folder + separator + file.getName();
            if (file.isDirectory()) {
                mapCodes.putAll(analyzeFolder(path));
                continue;
            }
            String content = FileUtil.read(path);
            String simpleName = getSimpleName(path);
            String packageName = getPackageName(path);
            List<String> packageNames = getPackageNames(content);
            boolean hasSpring = content.contains("SpringBootApplication");
            Set<String> internalPackageNames = getInternalPackageNames(content, packageNames);
            Set<String> externalPackageNames = getExternalPackageNames(packageNames);
            Set<String> internalSimpleNames = getSimpleNames(internalPackageNames);
            checkInternalPackageNames(path, hasSpring, internalPackageNames);
            mapCodes.put(packageName, SourceCode.of(
                    path, content, hasSpring, simpleName, packageName,
                    externalPackageNames, internalPackageNames, internalSimpleNames
            ));
        }
        return mapCodes;
    }

    private static Set<String> getInternalPackageNames(String content, List<String> packageNames) {
        Set<String> lstPackageName = new HashSet<>();
        for (String packageName : packageNames) {
            if (!packageName.contains("yongkj")) continue;
            if (packageName.contains("App")) continue;
            if (!packageName.contains("*")) {
                lstPackageName.add(packageName);
                continue;
            }
            lstPackageName.addAll(getInternalPackageNames(content, packageName));
        }
        return lstPackageName;
    }

    private static Set<String> getInternalPackageNames(String content, String packageName) {
        String path = FileUtil.getAbsPath(false, "src", "main", "java");
        packageName = packageName.replace(".*", "");
        String separator = path.contains("/") ? "/" : "\\";
        path += separator + packageName.replace(".", separator);

        List<File> lstFile = FileUtil.list(path);
        Set<String> lstPath = new HashSet<>();
        for (File file : lstFile) {
            String name = file.getName().replace(".java", "");
            if (!content.contains(name)) continue;
            String sourceCodePath = path + separator + file.getName();
            lstPath.add(sourceCodePath);
        }
        return getPackageNames(lstPath);
    }

    private static void checkInternalPackageNames(String javaPath, boolean hasSpring, Set<String> internalPackageNames) {
        if (!hasSpring) return;
        String scriptDir = FileUtil.dirname(javaPath);
        Set<String> lstPath = getSourceCodePaths(scriptDir);
        internalPackageNames.addAll(getPackageNames(lstPath));
    }

    private static Set<String> getExternalPackageNames(List<String> packageNames) {
        Set<String> lstPackageName = new HashSet<>();
        for (String packageName : packageNames) {
            if (packageName.contains("yongkj")) continue;
            if (packageName.contains("w3c.dom")) continue;
            if (packageName.startsWith("java.")) continue;
            if (packageName.startsWith("javax.")) continue;
            lstPackageName.add(packageName);
        }
        return lstPackageName;
    }

    private static Set<String> getSimpleNames(Set<String> packageNames) {
        Set<String> simpleNames = new HashSet<>();
        for (String packageName : packageNames) {
            simpleNames.add(getSimpleName(packageName));
        }
        return simpleNames;
    }

    private static String getSimpleName(String javaPath) {
        String packageName = javaPath;
        if (!packageName.startsWith("com")) {
            packageName = getPackageName(javaPath);
        }
        int index = packageName.lastIndexOf(".");
        return packageName.substring(index + 1);
    }

    private static List<String> getPackageNames(String content) {
        Pattern pattern = Pattern.compile("\\simport (.*);");
        List<String> packageNames = new ArrayList<>();
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            packageNames.add(matcher.group(1));
        }
        pattern = Pattern.compile("package (.*);\\s+");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            if (!matcher.group(1).contains("yongkj")) continue;
            packageNames.add(matcher.group(1) + ".*");
        }
        return packageNames;
    }

    private static Set<String> getPackageNames(Set<String> javaPaths) {
        Set<String> lstPath = new HashSet<>();
        for (String path : javaPaths) {
            lstPath.add(getPackageName(path));
        }
        return lstPath;
    }

    private static String getPackageName(String javaPath) {
        int index = javaPath.lastIndexOf(".");
        String suffix = javaPath.substring(index);
        javaPath = javaPath.replace(suffix, "");
        javaPath = "com" + javaPath.split("com")[1];
        String separator = javaPath.contains("/") ? "/" : "\\\\";
        return javaPath.replaceAll(separator, ".");
    }

    private static Set<String> getSourceCodePaths(String scriptDir) {
        String separator = scriptDir.contains("/") ? "/" : "\\";
        List<File> lstFile = FileUtil.list(scriptDir);
        Set<String> paths = new HashSet<>();
        for (File file : lstFile) {
            String path = scriptDir + separator + file.getName();
            if (file.isDirectory()) {
                paths.addAll(getSourceCodePaths(path));
                continue;
            }
            paths.add(path);
        }
        return paths;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Set<String> getInternalSimpleNames() {
        return internalSimpleNames;
    }

    public void setInternalSimpleNames(Set<String> internalSimpleNames) {
        this.internalSimpleNames = internalSimpleNames;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isHasSpring() {
        return hasSpring;
    }

    public void setHasSpring(boolean hasSpring) {
        this.hasSpring = hasSpring;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public Set<String> getExternalPackageNames() {
        return externalPackageNames;
    }

    public void setExternalPackageNames(Set<String> externalPackageNames) {
        this.externalPackageNames = externalPackageNames;
    }

    public Set<String> getInternalPackageNames() {
        return internalPackageNames;
    }

    public void setInternalPackageNames(Set<String> internalPackageNames) {
        this.internalPackageNames = internalPackageNames;
    }
}