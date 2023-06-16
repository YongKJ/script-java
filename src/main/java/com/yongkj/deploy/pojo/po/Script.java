package com.yongkj.deploy.pojo.po;

import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Script {

    private String javaName;
    private String javaPath;
    private String packageName;
    private String yamlConfig;
    private String scriptName;
    private String scriptPath;
    private String scriptConfig;
    private String scriptRun;
    private boolean hasSpring;
    private Set<String> externalPackageNames;
    private Set<String> internalPackageNames;

    private Script(String javaName, String javaPath, String packageName, String yamlConfig, String scriptName, String scriptPath, String scriptConfig, String scriptRun, boolean hasSpring, Set<String> externalPackageNames, Set<String> internalPackageNames) {
        this.javaName = javaName;
        this.javaPath = javaPath;
        this.packageName = packageName;
        this.yamlConfig = yamlConfig;
        this.scriptName = scriptName;
        this.scriptPath = scriptPath;
        this.scriptConfig = scriptConfig;
        this.scriptRun = scriptRun;
        this.hasSpring = hasSpring;
        this.externalPackageNames = externalPackageNames;
        this.internalPackageNames = internalPackageNames;
    }

    public static Script of(String javaName, String javaPath, String packageName, String yamlConfig, String scriptName, String scriptPath, String scriptConfig, String scriptRun, boolean hasSpring, Set<String> externalPackageNames, Set<String> internalPackageNames) {
        return new Script(javaName, javaPath, packageName, yamlConfig, scriptName, scriptPath, scriptConfig, scriptRun, hasSpring, externalPackageNames, internalPackageNames);
    }

    public static List<Script> get() {
        String path = FileUtil.getAbsPath(false, "src", "main", "java", "com", "yongkj", "deploy", "service");
        List<Script> lstScript = get("");
        lstScript.addAll(get(path));
        return lstScript;
    }

    private static List<Script> get(String appletDir) {
        if (appletDir.length() == 0) appletDir = FileUtil.getAbsPath(false, "src", "main", "java", "com", "yongkj", "applet");
        String resourcesDir = FileUtil.getAbsPath(false, "src", "main", "resources");
        String scriptDir = FileUtil.getAbsPath(false, "script");
        String separator = appletDir.contains("/") ? "/" : "\\";
        List<File> lstFile = FileUtil.list(appletDir);
        List<Script> lstScript = new ArrayList<>();
        for (File file : lstFile) {
            String javaPath = file.getAbsolutePath();
            if (file.isDirectory()) {
                javaPath = getScript(javaPath);
            }
            Set<String> paths = new HashSet<>();
            Set<String> internalPackageNames = new HashSet<>();
            int index = javaPath.lastIndexOf(File.separator);
            String packageName = getPackageName(javaPath);
            String javaName = javaPath.substring(index + 1);
            String content = FileUtil.read(getSourceCodePath(packageName));
            boolean hasSpring = content.contains("SpringBootApplication");
            String yamlName = javaName.replace(".java", ".yaml");
            yamlName = !hasSpring ? yamlName : "application" +
                    yamlName.substring(0, 1).toUpperCase() + yamlName.substring(1);
            String scriptConfig = scriptDir + separator + GenUtil.toLine(yamlName);
            String yamlConfig = resourcesDir + separator + GenUtil.toLine(yamlName);
            String scriptName = GenUtil.toLine(javaName.replace(".java", ".jar"));
            String scriptRun = javaName.replace(".java", "");
            internalPackageNames.add(GenUtil.toLine(yamlName));
            String scriptPath = scriptDir + separator + scriptName;
            Script script = Script.of(
                    javaName, javaPath, packageName, yamlConfig,
                    scriptName, scriptPath, scriptConfig, scriptRun,
                    hasSpring, new HashSet<>(), internalPackageNames
            );
            script.setExternalPackageNames(analyzeExternalPackageName(content, packageName, paths, script.isHasSpring()));
            lstScript.add(analyzeInternalPackageName(script, paths));
        }
        return lstScript;
    }

    public static Script analyzeInternalPackageName(Script script, Set<String> paths) {
        script.getInternalPackageNames().add("com.yongkj.App");
        for (String path : paths) {
            script.getInternalPackageNames().add(getPackageName(path));
        }
        return script;
    }

    public static Set<String> analyzeExternalPackageName(String content, String packageName, Set<String> paths, boolean hasSpring) {
        List<String> lstPath = getSourceCodePaths(content, packageName, hasSpring);
        Set<String> packageNames = new HashSet<>();
        for (String path : lstPath) {
            if (paths.contains(path)) continue;
            paths.add(path);
            String tempContent = FileUtil.read(path);
            List<String> lstPackageNames = getPackageNames(tempContent);
            for (String tempPackageName : lstPackageNames) {
                if (packageName.contains(tempPackageName)) continue;
                if (tempPackageName.contains("java.")) continue;
                if (tempPackageName.contains("javax.")) continue;
                if (tempPackageName.contains("w3c.dom")) continue;
                if (tempPackageName.contains("com.yongkj.App")) continue;
                if (tempPackageName.contains("yongkj")) {
                    packageNames.addAll(analyzeExternalPackageName(content, tempPackageName, paths, hasSpring));
                    packageNames.addAll(analyzeExternalPackageName(tempContent, tempPackageName, paths, hasSpring));
                    continue;
                }
                packageNames.add(tempPackageName);
            }
        }
        return packageNames;
    }

    private static List<String> getPackageNames(String content) {
        Pattern pattern = Pattern.compile("\\simport (.*);");
        List<String> packageNames = new ArrayList<>();
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            packageNames.add(matcher.group(1));
        }
        pattern = Pattern.compile("package (.*);");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            packageNames.add(matcher.group(1) + ".*");
        }
        return packageNames;
    }

    private static List<String> getSourceCodePaths(String content, String packageName, boolean hasSpring) {
        List<String> lstPath = new ArrayList<>();
        if (!packageName.contains("*")) {
            lstPath.add(getSourceCodePath(packageName));
            if (!hasSpring) return lstPath;
            String scriptDir = FileUtil.dirname(lstPath.get(0));
            lstPath.addAll(getSourceCodePaths(scriptDir));
            return lstPath;
        }
        String path = FileUtil.getAbsPath(false, "src", "main", "java");
        packageName = packageName.replace(".*", "");
        String separator = path.contains("/") ? "/" : "\\";
        path += separator + packageName.replace(".", separator);

        List<File> lstFile = FileUtil.list(path);
        for (File file : lstFile) {
            String name = file.getName().replace(".java", "");
            if (content.length() > 0 && !content.contains(name)) continue;
            String sourceCodePath = path + separator + file.getName();
            lstPath.add(sourceCodePath);
        }
        return lstPath;
    }

    private static List<String> getSourceCodePaths(String scriptDir) {
        String separator = scriptDir.contains("/") ? "/" : "\\";
        List<File> lstFile = FileUtil.list(scriptDir);
        List<String> paths = new ArrayList<>();
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

    public static String getSourceCodePath(String packageName) {
        String path = FileUtil.getAbsPath(false, "src", "main", "java");
        String separator = path.contains("/") ? "/" : "\\";
        path += separator + packageName.replace(".", separator);
        return path + ".java";
    }

    public static String getPackageName(String javaPath) {
        int index = javaPath.lastIndexOf(".");
        String suffix = javaPath.substring(index);
        if (suffix.equals(".yaml")) {
            String separator = javaPath.contains("/") ? "/" : "\\";
            return javaPath.substring(javaPath.lastIndexOf(separator) + 1);
        }
        javaPath = javaPath.replace(suffix, "");
        String separator = javaPath.contains("/") ? "/" : "\\\\";
        javaPath = "com" + javaPath.split("com")[1];
        return javaPath.replaceAll(separator, ".");
    }

    private static String getScript(String folder) {
        List<File> lstFile = FileUtil.list(folder);
        File script = lstFile.stream().filter(file -> file.getName().contains(".java")).findFirst().orElse(null);
        return script != null ? script.getAbsolutePath() : "";
    }

    public boolean isHasSpring() {
        return hasSpring;
    }

    public void setHasSpring(boolean hasSpring) {
        this.hasSpring = hasSpring;
    }

    public String getYamlConfig() {
        return yamlConfig;
    }

    public String getJavaName() {
        return javaName;
    }

    public void setJavaName(String javaName) {
        this.javaName = javaName;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setYamlConfig(String yamlConfig) {
        this.yamlConfig = yamlConfig;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getScriptConfig() {
        return scriptConfig;
    }

    public void setScriptConfig(String scriptConfig) {
        this.scriptConfig = scriptConfig;
    }

    public String getScriptRun() {
        return scriptRun;
    }

    public void setScriptRun(String scriptRun) {
        this.scriptRun = scriptRun;
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
