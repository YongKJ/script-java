package com.yongkj.deploy.pojo.po;

import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;

import java.io.File;
import java.util.*;

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
    private static final Map<String, SourceCode> mapCodes = SourceCode.get();

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
        if (appletDir.length() == 0)
            appletDir = FileUtil.getAbsPath(false, "src", "main", "java", "com", "yongkj", "applet");
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
            Set<String> internalPackageNames = new HashSet<>();
            Set<String> externalPackageNames = new HashSet<>();
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
            String scriptPath = scriptDir + separator + scriptName;
            internalPackageNames.add(GenUtil.toLine(yamlName));
            internalPackageNames.add("com.yongkj.App");
            lstScript.add(Script.of(
                    javaName, javaPath, packageName, yamlConfig,
                    scriptName, scriptPath, scriptConfig, scriptRun,
                    hasSpring, externalPackageNames, internalPackageNames
            ));
            analyzePackageNames(mapCodes.get(packageName), new HashSet<>(), internalPackageNames, externalPackageNames);
        }
        return lstScript;
    }

    private static void analyzePackageNames(SourceCode code, Set<String> codePackageNames, Set<String> internalPackageNames, Set<String> externalPackageNames) {
        if (code == null) return;
        for (String packageName : code.getInternalPackageNames()) {
            if (codePackageNames.contains(packageName)) continue;
            codePackageNames.add(code.getPackageName());
            internalPackageNames.addAll(code.getInternalPackageNames());
            externalPackageNames.addAll(code.getExternalPackageNames());
            analyzePackageNames(mapCodes.get(packageName), codePackageNames, internalPackageNames, externalPackageNames);
        }
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
