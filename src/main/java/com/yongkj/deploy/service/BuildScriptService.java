package com.yongkj.deploy.service;

import com.yongkj.deploy.pojo.dto.BuildConfig;
import com.yongkj.deploy.pojo.po.Script;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.*;

import java.util.List;

public class BuildScriptService {

    private int configType = 1;
    private final List<Script> scripts;
    private final BuildConfig buildConfig;

    private BuildScriptService() {
        this.scripts = Script.get();
        this.buildConfig = BuildConfig.get();
    }

    private void apply() {
        GenUtil.println();
        GenUtil.println("0. update script dependencies");
        for (int i = 0; i < scripts.size(); i++) {
            GenUtil.println((i + 1) + ". " + scripts.get(i).getJavaName());
        }
        GenUtil.print("Please enter one or more numbers corresponding to the script: ");
        List<String> nums = GenUtil.readParams();
        if (nums.size() == 0) return;
        GenUtil.println();

        GenUtil.println("1. external libraries");
        GenUtil.println("2. external libraries update");
        GenUtil.println("3. internal libraries");
        GenUtil.print("Please enter the number corresponding to the config: ");
        List<String> types = GenUtil.readParams();
        if (nums.size() > 0) {
            configType = GenUtil.strToInt(types.get(0));
        }
        GenUtil.println();

        changePomXml(true);
        for (String num : nums) {
            int index = GenUtil.strToInt(num) - 1;
            if (0 <= index && index < scripts.size()) {
                build(scripts.get(index));
            }
            if (index == -1) {
                String buildCmd = CmdUtil.copyMavenDependencies();
                RemoteUtil.changeWorkFolder(FileUtil.appDir());
                RemoteUtil.execLocalCmd(buildCmd);
            }
        }
        changePomXml(false);
    }

    private void build(Script script) {
        changeBuildConfig(script, true);

        RemoteUtil.changeWorkFolder(FileUtil.appDir());
        String buildCmd = CmdUtil.buildJavaScript();
        RemoteUtil.execLocalCmd(buildCmd);
        if (FileUtil.exist(script.getYamlConfig())) {
            FileUtil.copy(script.getYamlConfig(), script.getScriptConfig());
        }

        changeBuildConfig(script, false);

        updateScript(script);
    }

    private void updateScript(Script script) {
        FileUtil.copy(configType == 3 ? buildConfig.getJarDepPath() : buildConfig.getJarPath(), script.getScriptPath());
    }

    private void changePomXml(boolean isBefore) {
        if (configType == 1) return;
        FileUtil.modFile(
                buildConfig.getPomPath(), buildConfig.getPomPluginsPattern(),
                !isBefore ? buildConfig.getPomPluginsOriginal() : (configType == 2 ?
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
