package com.yongkj.deploy.pojo.dto;

import com.yongkj.util.FileUtil;

public class BuildConfig {

    private String jarName;
    private String jarPath;
    private String appPath;
    private String scriptRunPattern;
    private String scriptRunOriginal;
    private String packageImportPattern;
    private String packageImportOriginal;

    private BuildConfig(String jarName, String jarPath, String appPath, String scriptRunPattern, String scriptRunOriginal, String packageImportPattern, String packageImportOriginal) {
        this.jarName = jarName;
        this.jarPath = jarPath;
        this.appPath = appPath;
        this.scriptRunPattern = scriptRunPattern;
        this.scriptRunOriginal = scriptRunOriginal;
        this.packageImportPattern = packageImportPattern;
        this.packageImportOriginal = packageImportOriginal;
    }

    public static BuildConfig of(String jarName, String jarPath, String appPath, String scriptRunPattern, String scriptRunOriginal, String packageImportPattern, String packageImportOriginal) {
        return new BuildConfig(jarName, jarPath, appPath, scriptRunPattern, scriptRunOriginal, packageImportPattern, packageImportOriginal);
    }

    public static BuildConfig get() {
        String appPath = FileUtil.getAbsPath(false, "src", "main", "java", "com", "yongkj", "App.java");
        String targetDir = FileUtil.getAbsPath(false, "target");
        String separator = targetDir.contains("/") ? "/" : "\\";
        String jarName = "script-java-1.0-SNAPSHOT.jar";
        String jarPath = targetDir + separator + jarName;
        return BuildConfig.of(
                jarName, jarPath, appPath, "\\s+(.*).run\\(\\);", "Demo",
                "import (.*);", "com.yongkj.applet.demo.Demo"
        );
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public String getScriptRunPattern() {
        return scriptRunPattern;
    }

    public void setScriptRunPattern(String scriptRunPattern) {
        this.scriptRunPattern = scriptRunPattern;
    }

    public String getScriptRunOriginal() {
        return scriptRunOriginal;
    }

    public void setScriptRunOriginal(String scriptRunOriginal) {
        this.scriptRunOriginal = scriptRunOriginal;
    }

    public String getPackageImportPattern() {
        return packageImportPattern;
    }

    public void setPackageImportPattern(String packageImportPattern) {
        this.packageImportPattern = packageImportPattern;
    }

    public String getPackageImportOriginal() {
        return packageImportOriginal;
    }

    public void setPackageImportOriginal(String packageImportOriginal) {
        this.packageImportOriginal = packageImportOriginal;
    }

}
