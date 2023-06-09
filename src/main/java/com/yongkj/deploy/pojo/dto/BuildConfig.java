package com.yongkj.deploy.pojo.dto;

import com.yongkj.deploy.pojo.po.PomXml;
import com.yongkj.util.FileUtil;

public class BuildConfig {

    private String jarName;
    private String jarPath;
    private String appPath;
    private String pomPath;
    private String jarDepName;
    private String jarDepPath;
    private String scriptRunPattern;
    private String scriptRunOriginal;
    private String packageImportPattern;
    private String packageImportOriginal;
    private String pomPluginsPattern;
    private String pomPluginsOriginal;
    private String pomPluginsExternal;
    private String pomPluginsInternal;
    private String pomPluginsExternalUpdate;

    private BuildConfig(String jarName, String jarPath, String appPath, String pomPath, String jarDepName, String jarDepPath, String scriptRunPattern, String scriptRunOriginal, String packageImportPattern, String packageImportOriginal, String pomPluginsPattern, String pomPluginsOriginal, String pomPluginsExternal, String pomPluginsExternalUpdate, String pomPluginsInternal) {
        this.jarName = jarName;
        this.jarPath = jarPath;
        this.appPath = appPath;
        this.pomPath = pomPath;
        this.jarDepName = jarDepName;
        this.jarDepPath = jarDepPath;
        this.scriptRunPattern = scriptRunPattern;
        this.scriptRunOriginal = scriptRunOriginal;
        this.packageImportPattern = packageImportPattern;
        this.packageImportOriginal = packageImportOriginal;
        this.pomPluginsPattern = pomPluginsPattern;
        this.pomPluginsOriginal = pomPluginsOriginal;
        this.pomPluginsExternal = pomPluginsExternal;
        this.pomPluginsExternalUpdate = pomPluginsExternalUpdate;
        this.pomPluginsInternal = pomPluginsInternal;
    }

    public static BuildConfig of(String jarName, String jarPath, String appPath, String pomPath, String jarDepName, String jarDepPath, String scriptRunPattern, String scriptRunOriginal, String packageImportPattern, String packageImportOriginal, String pomPluginsPattern, String pomPluginsOriginal, String pomPluginsExternal, String pomPluginsExternalUpdate, String pomPluginsInternal) {
        return new BuildConfig(jarName, jarPath, appPath, pomPath, jarDepName, jarDepPath, scriptRunPattern, scriptRunOriginal, packageImportPattern, packageImportOriginal, pomPluginsPattern, pomPluginsOriginal, pomPluginsExternal, pomPluginsExternalUpdate, pomPluginsInternal);
    }

    public static BuildConfig get() {
        String appPath = FileUtil.getAbsPath(false, "src", "main", "java", "com", "yongkj", "App.java");
        String targetDir = FileUtil.getAbsPath(false, "target");
        String pomPath = FileUtil.getAbsPath(false, "pom.xml");
        String separator = targetDir.contains("/") ? "/" : "\\";
        String jarName = "script-java-1.0-SNAPSHOT.jar";
        String jarPath = targetDir + separator + jarName;
        String jarDepName = "script-java-1.0-SNAPSHOT-jar-with-dependencies.jar";
        String jarDepPath = targetDir + separator + jarDepName;
        return BuildConfig.of(
                jarName, jarPath, appPath, pomPath, jarDepName, jarDepPath, "\\s+(.*)\\.run\\(args\\);",
                "Demo", "import (.*);", "com.yongkj.applet.demo.Demo",
                "(<plugins>[\\s\\S]*?</plugins>)", PomXml.getExternalLibraries(),
                PomXml.getExternalLibraries(), PomXml.getExternalLibrariesUpdate(), PomXml.getInternalLibraries()
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

    public String getPomPath() {
        return pomPath;
    }

    public void setPomPath(String pomPath) {
        this.pomPath = pomPath;
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

    public String getPomPluginsPattern() {
        return pomPluginsPattern;
    }

    public void setPomPluginsPattern(String pomPluginsPattern) {
        this.pomPluginsPattern = pomPluginsPattern;
    }

    public String getPomPluginsOriginal() {
        return pomPluginsOriginal;
    }

    public String getJarDepName() {
        return jarDepName;
    }

    public void setJarDepName(String jarDepName) {
        this.jarDepName = jarDepName;
    }

    public String getJarDepPath() {
        return jarDepPath;
    }

    public void setJarDepPath(String jarDepPath) {
        this.jarDepPath = jarDepPath;
    }

    public void setPomPluginsOriginal(String pomPluginsOriginal) {
        this.pomPluginsOriginal = pomPluginsOriginal;
    }

    public String getPomPluginsExternal() {
        return pomPluginsExternal;
    }

    public void setPomPluginsExternal(String pomPluginsExternal) {
        this.pomPluginsExternal = pomPluginsExternal;
    }

    public String getPomPluginsInternal() {
        return pomPluginsInternal;
    }

    public void setPomPluginsInternal(String pomPluginsInternal) {
        this.pomPluginsInternal = pomPluginsInternal;
    }

    public String getPomPluginsExternalUpdate() {
        return pomPluginsExternalUpdate;
    }

    public void setPomPluginsExternalUpdate(String pomPluginsExternalUpdate) {
        this.pomPluginsExternalUpdate = pomPluginsExternalUpdate;
    }
}
