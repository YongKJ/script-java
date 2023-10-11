package com.yongkj.applet.applicationConfigSwitch;

import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SwitchApplicationConfig {

    private final String configName;
    private final String configPath;
    private final String projectPath;
    private final List<String> projectNames;

    private SwitchApplicationConfig() {
        configPath = GenUtil.getValue("config-path");
        projectNames = GenUtil.getList("project-name");
        projectPath = GenUtil.getValue("project-path");
        String environment = GenUtil.getValue("environment");
        configName = "application-" + (Objects.equals(environment, "test") ? "test" : "dev") + ".yml";
    }

    private void apply() {
        LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "apply", "projectNames", projectNames));
        System.out.println("---------------------------------------------------------------------------------------------");
        for (String projectName : projectNames) {
            String srcPath = getApplicationSrcPath(projectName);
            String desPath = getApplicationDesPath(projectName);
            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "apply", "srcPath", srcPath));
            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "apply", "desPath", desPath));
            System.out.println("---------------------------------------------------------------------------------------------");
            if (FileUtil.exist(desPath)) {
                FileUtil.delete(desPath);
            }
            FileUtil.copy(srcPath, desPath);
        }
    }

    private String getApplicationDesPath(String projectName) {
        return getProjectResourcesPath(projectName) + File.separator + "application.yml";
    }

    private String getApplicationSrcPath(String projectName) {
        return getListPath(Arrays.asList(
                configPath, projectName, configName
        ));
    }

    private String getProjectResourcesPath(String projectName) {
        String resourcesPath = getListPath(Arrays.asList(
                projectPath, projectName, "src", "main", "resources"
        ));
        if (FileUtil.exist(resourcesPath)) {
            return resourcesPath;
        }
        resourcesPath = getListPath(Arrays.asList(
                projectPath, projectName, projectName + "-start", "src", "main", "resources"
        ));
        if (FileUtil.exist(resourcesPath)) {
            return resourcesPath;
        }
        return getListPath(Arrays.asList(
                projectPath, projectName, "service-order-start", "src", "main", "resources"
        ));
    }

    private String getListPath(List<String> lstName) {
        return String.join(File.separator, lstName);
    }

    public static void run(String[] args) {
        new SwitchApplicationConfig().apply();
    }

}
