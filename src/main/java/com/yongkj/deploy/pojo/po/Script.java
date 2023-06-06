package com.yongkj.deploy.pojo.po;

import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Script {

    private String javaName;
    private String javaPath;
    private String packageName;
    private String yamlConfig;
    private String scriptName;
    private String scriptPath;
    private String scriptConfig;
    private String scriptRun;

    private Script(String javaName, String javaPath, String packageName, String yamlConfig, String scriptName, String scriptPath, String scriptConfig, String scriptRun) {
        this.javaName = javaName;
        this.javaPath = javaPath;
        this.packageName = packageName;
        this.yamlConfig = yamlConfig;
        this.scriptName = scriptName;
        this.scriptPath = scriptPath;
        this.scriptConfig = scriptConfig;
        this.scriptRun = scriptRun;
    }

    public static Script of(String javaName, String javaPath, String packageName, String yamlConfig, String scriptName, String scriptPath, String scriptConfig, String scriptRun) {
        return new Script(javaName, javaPath, packageName, yamlConfig, scriptName, scriptPath, scriptConfig, scriptRun);
    }

    public static List<Script> get() {
        String appletDir = FileUtil.getAbsPath(false, "src", "main", "java", "com", "yongkj", "applet");
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
            int index = javaPath.lastIndexOf(File.separator);
            String packageName = getPackageName(javaPath);
            String javaName = javaPath.substring(index + 1);
            String yamlName = javaName.replace(".java", ".yaml");
            String scriptConfig = scriptDir + separator + GenUtil.toLine(yamlName);
            String yamlConfig = resourcesDir + separator + GenUtil.toLine(yamlName);
            String scriptName = GenUtil.toLine(javaName.replace(".java", ".jar"));
            String scriptRun = javaName.replace(".java", "");
            String scriptPath = scriptDir + separator + scriptName;
            lstScript.add(Script.of(
                    javaName, javaPath, packageName, yamlConfig,
                    scriptName, scriptPath, scriptConfig, scriptRun
            ));
        }
        return lstScript;
    }

    private static String getPackageName(String javaPath) {
        javaPath = javaPath.replace(".java", "");
        String separator = javaPath.contains("/") ? "/" : "\\\\";
        javaPath = "com" + javaPath.split("com")[1];
        return javaPath.replaceAll(separator, ".");
    }

    private static String getScript(String folder) {
        List<File> lstFile = FileUtil.list(folder);
        File script = lstFile.stream().filter(file -> file.getName().contains(".java")).findFirst().orElse(null);
        return script != null ? script.getAbsolutePath() : "";
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
}
