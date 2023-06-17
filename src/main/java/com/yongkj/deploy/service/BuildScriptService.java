package com.yongkj.deploy.service;

import com.yongkj.deploy.pojo.dto.BuildConfig;
import com.yongkj.deploy.pojo.po.Dependency;
import com.yongkj.deploy.pojo.po.Script;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.*;

import java.io.File;
import java.util.List;

public class BuildScriptService {

    private int configType = 1;
    private final List<Script> scripts;
    private final BuildConfig buildConfig;
    private final List<Dependency> dependencies;

    private BuildScriptService() {
        String repositoryPath = GenUtil.objToStr(GenUtil.getValue("repository-path"));
        this.dependencies = Dependency.get(repositoryPath);
        this.buildConfig = BuildConfig.get();
        this.scripts = Script.get();
    }

    private void apply() {
        GenUtil.println();
        for (int i = 0; i < scripts.size(); i++) {
            GenUtil.println((i + 1) + ". " + scripts.get(i).getJavaName());
        }
        GenUtil.println((scripts.size() + 1) + ". update script dependencies");
        GenUtil.print("Please enter one or more numbers corresponding to the script: ");
        List<String> nums = GenUtil.readParams();
        if (nums.size() == 0) return;
        GenUtil.println();

        GenUtil.println("1. external libraries");
        GenUtil.println("2. external libraries slim");
        GenUtil.println("3. external libraries slim mini");
        GenUtil.println("4. external libraries update");
        GenUtil.println("5. internal libraries");
        GenUtil.println("6. internal libraries slim");
        GenUtil.println("7. internal libraries slim loader");
        GenUtil.print("Please enter the number corresponding to the config: ");
        List<String> types = GenUtil.readParams();
        if (nums.size() > 0) {
            configType = GenUtil.strToInt(types.get(0));
        }
        GenUtil.println();

        changePomPlugins(true);
        for (String num : nums) {
            int index = GenUtil.strToInt(num) - 1;
            if (0 <= index && index < scripts.size()) {
                build(scripts.get(index));
            }
            if (index == scripts.size()) {
                String buildCmd = CmdUtil.copyMavenDependencies();
                RemoteUtil.changeWorkFolder(FileUtil.appDir());
                RemoteUtil.execLocalCmd(buildCmd);
            }
        }
        changePomPlugins(false);
        RemoteUtil.execLocalCmd(CmdUtil.compileJavaScript());
    }

    private void build(Script script) {
        changeBuildConfig(script, true);

        RemoteUtil.changeWorkFolder(FileUtil.appDir());
        RemoteUtil.execLocalCmd(CmdUtil.compileJavaScript());
        changeResourcesYaml(script, true);
        changeTargetClasses(script);
        switch (configType) {
            case 1:
            case 4:
            case 5:
                RemoteUtil.execLocalCmd(CmdUtil.packageJavaScript());
                break;
            case 2:
                changePomDependencies(script, true);
                FileUtil.delete(buildConfig.getLibsPath());
                RemoteUtil.execLocalCmd(CmdUtil.copyMavenDependencies());
                RemoteUtil.execLocalCmd(CmdUtil.packageJavaScript());
                changePomDependencies(script, false);
                break;
            case 3:
                changePomDependencies(script, true);
                changePomClassPath(true);
                FileUtil.delete(buildConfig.getLibsPath());
                RemoteUtil.execLocalCmd(CmdUtil.copyMavenDependencies());
                RemoteUtil.execLocalCmd(CmdUtil.packageJavaScript());
                changePomClassPath(false);
                changePomDependencies(script, false);
                break;
            case 6:
            case 7:
                changePomDependencies(script, true);
                RemoteUtil.execLocalCmd(CmdUtil.packageJavaScript());
                changePomDependencies(script, false);
                break;
        }
        if (FileUtil.exist(script.getYamlConfig())) {
            FileUtil.copy(script.getYamlConfig(), script.getScriptConfig());
        }

        updateScript(script);
        changeBuildConfig(script, false);
        changeResourcesYaml(script, false);
    }

    private void updateScript(Script script) {
        FileUtil.copy(configType == 5 || configType == 6 ? buildConfig.getJarDepPath() : buildConfig.getJarPath(), script.getScriptPath());
    }

    private void changeResourcesYaml(Script script, boolean isBefore) {
        String resourcesPath = FileUtil.getAbsPath(false, "src", "main", "resources");
        String mainPath = FileUtil.getAbsPath(false, "src", "main");
        List<File> lstFile = FileUtil.list(isBefore ? resourcesPath : mainPath);
        String separator = resourcesPath.contains("/") ? "/" : "\\";
        for (File file : lstFile) {
            if (file.isDirectory()) continue;
            String packageName = Script.getPackageName(file.getAbsolutePath());
            if (isBefore && script.getInternalPackageNames().contains(packageName)) continue;
            String srcPath = (isBefore ? resourcesPath : mainPath)  + separator + file.getName();
            String desPath = (isBefore ? mainPath : resourcesPath)  + separator + file.getName();
            FileUtil.move(srcPath, desPath);
        }
    }

    private void changeTargetClasses(Script script) {
        String path = FileUtil.getAbsPath(false, "target", "classes");
        List<File> lstFile = FileUtil.list(path);
        for (File file : lstFile) {
            if (file.isDirectory()) {
                BuildConfig.changeClassFolder(script, file.getAbsolutePath());
                continue;
            }
            String packageName = Script.getPackageName(file.getAbsolutePath());
            if (file.isFile() && script.getInternalPackageNames().contains(packageName)) continue;
            FileUtil.delete(file.getAbsolutePath());
        }
    }

    private void changePomDependencies(Script script, boolean isBefore) {
        if (!(configType == 2 || configType == 3 || configType == 6 || configType == 7)) return;
        FileUtil.modFile(
                buildConfig.getPomPath(), buildConfig.getPomDependenciesPattern(),
                !isBefore ? buildConfig.getPomDependenciesOriginal() : BuildConfig.getPomDependenciesLatest(dependencies, script)
        );
    }

    private void changePomClassPath(boolean isBefore) {
        if (configType != 3) return;
        FileUtil.modContent(
                buildConfig.getPomPath(), buildConfig.getPomClassPathPattern(),
                !isBefore ? buildConfig.getPomClassPathOriginal() : buildConfig.getPomClassPathLatest()
        );
    }

    private void changePomPlugins(boolean isBefore) {
        if (configType == 1 || configType == 2 || configType == 3) return;
        FileUtil.modFile(
                buildConfig.getPomPath(), buildConfig.getPomPluginsPattern(),
                !isBefore ? buildConfig.getPomPluginsOriginal() : (configType == 4 ?
                        buildConfig.getPomPluginsExternalUpdate() : (configType == 7 ?
                        buildConfig.getPomPluginsInternalLoader() : buildConfig.getPomPluginsInternal()))
        );
    }

    private void changeBuildConfig(Script script, boolean isBefore) {
        FileUtil.modContent(
                buildConfig.getAppPath(), buildConfig.getPackageImportPattern(),
                isBefore ? script.getPackageName() : buildConfig.getPackageImportOriginal()
        );
        FileUtil.modContent(
                buildConfig.getAppPath(), buildConfig.getScriptRunPattern(),
                isBefore ? script.getScriptRun() : buildConfig.getScriptRunOriginal()
        );
    }

    private void test() {
        LogUtil.loggerLine(Log.of("BuildScriptService", "test", "scripts", scripts));
        LogUtil.loggerLine(Log.of("BuildScriptService", "test", "buildConfig", buildConfig));
    }

    private void test1() {
        FileUtil.modContent(
                buildConfig.getAppPath(), buildConfig.getPackageImportPattern(),
                "com.yongkj.applet.demo.Test"
        );
    }

    public static void run(String[] args) {
        new BuildScriptService().apply();
    }

}
