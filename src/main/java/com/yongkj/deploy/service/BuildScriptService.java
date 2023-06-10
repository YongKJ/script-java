package com.yongkj.deploy.service;

import com.yongkj.deploy.pojo.dto.BuildConfig;
import com.yongkj.deploy.pojo.po.Dependency;
import com.yongkj.deploy.pojo.po.Script;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.*;

import java.util.List;

public class BuildScriptService {

    private int configType = 1;
    private final List<Script> scripts;
    private final BuildConfig buildConfig;
    private final List<Dependency> dependencies;

    private BuildScriptService() {
        String repositoryPath = GenUtil.objToStr(GenUtil.getValue(
                "build-script-service.yaml", "repository-path"
        ));
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
        GenUtil.println("3. external libraries update");
        GenUtil.println("4. internal libraries");
        GenUtil.println("5. internal libraries slim");
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
    }

    private void build(Script script) {
        changeBuildConfig(script, true);

        RemoteUtil.changeWorkFolder(FileUtil.appDir());
        switch (configType) {
            case 1:
            case 3:
            case 4:
                RemoteUtil.execLocalCmd(CmdUtil.buildJavaScript());
                break;
            case 2:
                RemoteUtil.execLocalCmd(CmdUtil.compileJavaScript());
                changePomDependencies(script, true);
                RemoteUtil.execLocalCmd(CmdUtil.packageJavaScript());
                FileUtil.delete(buildConfig.getLibsPath());
                RemoteUtil.execLocalCmd(CmdUtil.copyMavenDependencies());
                changePomDependencies(script, false);
                break;
            case 5:
                RemoteUtil.execLocalCmd(CmdUtil.compileJavaScript());
                changePomDependencies(script, true);
                RemoteUtil.execLocalCmd(CmdUtil.packageJavaScript());
                changePomDependencies(script, false);
                break;
        }
        if (FileUtil.exist(script.getYamlConfig())) {
            FileUtil.copy(script.getYamlConfig(), script.getScriptConfig());
        }

        changeBuildConfig(script, false);

        updateScript(script);
    }

    private void updateScript(Script script) {
        FileUtil.copy(configType == 4 || configType == 5 ? buildConfig.getJarDepPath() : buildConfig.getJarPath(), script.getScriptPath());
    }

    private void changePomDependencies(Script script, boolean isBefore) {
        if (!(configType == 2 || configType == 5)) return;
        FileUtil.modFile(
                buildConfig.getPomPath(), buildConfig.getPomDependenciesPattern(),
                !isBefore ? buildConfig.getPomDependenciesOriginal() : BuildConfig.getPomDependenciesLatest(dependencies, script)
        );
    }

    private void changePomPlugins(boolean isBefore) {
        if (configType == 1 || configType == 2) return;
        FileUtil.modFile(
                buildConfig.getPomPath(), buildConfig.getPomPluginsPattern(),
                !isBefore ? buildConfig.getPomPluginsOriginal() : (configType == 3 ?
                        buildConfig.getPomPluginsExternalUpdate() : buildConfig.getPomPluginsInternal())
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
