package com.yongkj.deploy.pojo.dto;

import com.yongkj.deploy.pojo.po.Dependency;
import com.yongkj.deploy.pojo.po.PomXml;
import com.yongkj.deploy.pojo.po.Script;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.LogUtil;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildConfig {

    private String jarName;
    private String jarPath;
    private String appPath;
    private String pomPath;
    private String libsPath;
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
    private String pomPluginsInternalLoader;
    private String pomPluginsExternalUpdate;
    private String pomDependenciesPattern;
    private String pomDependenciesOriginal;
    private String pomClassPathPattern;
    private String pomClassPathOriginal;
    private String pomClassPathLatest;

    private BuildConfig(String jarName, String jarPath, String appPath, String pomPath, String libsPath, String jarDepName, String jarDepPath, String scriptRunPattern, String scriptRunOriginal, String packageImportPattern, String packageImportOriginal, String pomPluginsPattern, String pomPluginsOriginal, String pomPluginsExternal, String pomPluginsExternalUpdate, String pomPluginsInternal, String pomPluginsInternalLoader, String pomDependenciesPattern, String pomDependenciesOriginal, String pomClassPathPattern, String pomClassPathOriginal, String pomClassPathLatest) {
        this.jarName = jarName;
        this.jarPath = jarPath;
        this.appPath = appPath;
        this.pomPath = pomPath;
        this.libsPath = libsPath;
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
        this.pomPluginsInternalLoader = pomPluginsInternalLoader;
        this.pomDependenciesPattern = pomDependenciesPattern;
        this.pomDependenciesOriginal = pomDependenciesOriginal;
        this.pomClassPathPattern = pomClassPathPattern;
        this.pomClassPathOriginal = pomClassPathOriginal;
        this.pomClassPathLatest = pomClassPathLatest;
    }

    public static BuildConfig of(String jarName, String jarPath, String appPath, String pomPath, String libsPath, String jarDepName, String jarDepPath, String scriptRunPattern, String scriptRunOriginal, String packageImportPattern, String packageImportOriginal, String pomPluginsPattern, String pomPluginsOriginal, String pomPluginsExternal, String pomPluginsExternalUpdate, String pomPluginsInternal, String pomPluginsInternalLoader, String pomDependenciesPattern, String pomDependenciesOriginal, String pomClassPathPattern, String pomClassPathOriginal, String pomClassPathLatest) {
        return new BuildConfig(jarName, jarPath, appPath, pomPath, libsPath, jarDepName, jarDepPath, scriptRunPattern, scriptRunOriginal, packageImportPattern, packageImportOriginal, pomPluginsPattern, pomPluginsOriginal, pomPluginsExternal, pomPluginsExternalUpdate, pomPluginsInternal, pomPluginsInternalLoader, pomDependenciesPattern, pomDependenciesOriginal, pomClassPathPattern, pomClassPathOriginal, pomClassPathLatest);
    }

    public static BuildConfig get() {
        String appPath = FileUtil.getAbsPath(false, "src", "main", "java", "com", "yongkj", "App.java");
        String targetDir = FileUtil.getAbsPath(false, "target");
        String pomPath = FileUtil.getAbsPath(false, "pom.xml");
        String libsPath = FileUtil.getAbsPath(false, "libs");
        String separator = targetDir.contains("/") ? "/" : "\\";
        String jarName = "script-java-1.0-SNAPSHOT.jar";
        String jarPath = targetDir + separator + jarName;
        String jarDepName = "script-java-1.0-SNAPSHOT-jar-with-dependencies.jar";
        String jarDepPath = targetDir + separator + jarDepName;
        BuildConfig config = BuildConfig.of(
                jarName, jarPath, appPath, pomPath, libsPath, jarDepName, jarDepPath, "\\s+(.*)\\.run\\(args\\);",
                "Demo", "import (.*);", "com.yongkj.applet.demo.Demo",
                "(<plugins>[\\s\\S]*?</plugins>)", PomXml.getExternalLibraries(),
                PomXml.getExternalLibraries(), PomXml.getExternalLibrariesUpdate(), PomXml.getInternalLibraries(),
                PomXml.getInternalLibrariesLoader(), "(<dependencies>[\\s\\S]*?</dependencies>)", "",
                "<classpathPrefix>([\\s\\S]*?)</classpathPrefix>", "../libs/", "./libs/"
        );
        setPomDependenciesOriginal(config);
        return config;
    }

    public static void changeClassFolder(Script script, String fileName) {
        List<File> lstFile = FileUtil.list(fileName);
        for (File file : lstFile) {
            if (file.isDirectory()) {
                changeClassFolder(script, file.getAbsolutePath());
                continue;
            }
            String packageName = Script.getPackageName(file.getAbsolutePath());
            if (file.isFile() && script.getInternalPackageNames().contains(packageName)) continue;
            FileUtil.delete(file.getAbsolutePath());
        }
        if (!FileUtil.isEmpty(fileName)) return;
        FileUtil.delete(fileName);
    }

    public static String getPomDependenciesLatest(List<Dependency> dependencies, Script script) {
        StringBuilder dependenciesStr = new StringBuilder("<dependencies>");
        for (Dependency dependency : dependencies) {
            if (dependency.getGroupId().equals("junit")) continue;
            for (String packageName : dependency.getPackageNames()) {
                boolean flag = false;
                for (String tempPackageName : script.getExternalPackageNames()) {
                    if (!tempPackageName.contains(packageName)) continue;
                    if (dependency.getArtifactId().contains("spring-boot-starter") && !script.isHasSpring()) continue;
                    LogUtil.loggerLine(Log.of("BuildConfig", "getPomDependenciesLatest", "packageName", packageName));
                    LogUtil.loggerLine(Log.of("BuildConfig", "getPomDependenciesLatest", "dependency", dependency.getXmlText()));
                    dependenciesStr.append(dependency.getXmlText());
                    flag = true;
                    break;
                }
                if (flag) break;
            }
        }
        return dependenciesStr + "\n    </dependencies>";
    }

    private static void setPomDependenciesOriginal(BuildConfig config) {
        Pattern pattern = Pattern.compile(config.pomDependenciesPattern);
        String content = FileUtil.read(config.pomPath);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            config.setPomDependenciesOriginal(matcher.group(1));
        }
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

    public String getLibsPath() {
        return libsPath;
    }

    public void setLibsPath(String libsPath) {
        this.libsPath = libsPath;
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

    public String getPomPluginsInternalLoader() {
        return pomPluginsInternalLoader;
    }

    public void setPomPluginsInternalLoader(String pomPluginsInternalLoader) {
        this.pomPluginsInternalLoader = pomPluginsInternalLoader;
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

    public String getPomDependenciesPattern() {
        return pomDependenciesPattern;
    }

    public void setPomDependenciesPattern(String pomDependenciesPattern) {
        this.pomDependenciesPattern = pomDependenciesPattern;
    }

    public String getPomDependenciesOriginal() {
        return pomDependenciesOriginal;
    }

    public void setPomDependenciesOriginal(String pomDependenciesOriginal) {
        this.pomDependenciesOriginal = pomDependenciesOriginal;
    }

    public String getPomClassPathPattern() {
        return pomClassPathPattern;
    }

    public void setPomClassPathPattern(String pomClassPathPattern) {
        this.pomClassPathPattern = pomClassPathPattern;
    }

    public String getPomClassPathOriginal() {
        return pomClassPathOriginal;
    }

    public void setPomClassPathOriginal(String pomClassPathOriginal) {
        this.pomClassPathOriginal = pomClassPathOriginal;
    }

    public String getPomClassPathLatest() {
        return pomClassPathLatest;
    }

    public void setPomClassPathLatest(String pomClassPathLatest) {
        this.pomClassPathLatest = pomClassPathLatest;
    }
}
