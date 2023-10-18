package com.yongkj.deploy.pojo.po;

import com.yongkj.util.GenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AppletDependency {

    private String name;
    private List<PomDependency> pomDependencies;

    public AppletDependency() {
        this.name = "";
        this.pomDependencies = new ArrayList<>();
    }

    public static Map<String, AppletDependency> get() {
        List<AppletDependency> lstAppletDependency = new ArrayList<>();
        List<Map<String, Object>> lstData = GenUtil.getListMap("applet-dependencies");
        for (Map<String, Object> data : lstData) {
            String name = GenUtil.objToStr(data.get("name"));
            List<PomDependency> lstPomDependency = new ArrayList<>();
            List<Map<String, Object>> lstDependency = (List<Map<String, Object>>) data.get("dependencies");
            for (Map<String, Object> dependency : lstDependency) {
                String groupId = GenUtil.objToStr(dependency.get("groupId"));
                String artifactId = GenUtil.objToStr(dependency.get("artifactId"));
                String version = GenUtil.objToStr(dependency.get("version"));
                PomDependency pomDependency = new PomDependency();
                pomDependency.setGroupId(groupId);
                pomDependency.setVersion(version);
                pomDependency.setArtifactId(artifactId);
                lstPomDependency.add(pomDependency);
            }
            AppletDependency appletDependency = new AppletDependency();
            appletDependency.setPomDependencies(lstPomDependency);
            appletDependency.setName(name);
            lstAppletDependency.add(appletDependency);
        }
        return lstAppletDependency.stream()
                .collect(Collectors.toMap(AppletDependency::getName, Function.identity()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PomDependency> getPomDependencies() {
        return pomDependencies;
    }

    public void setPomDependencies(List<PomDependency> pomDependencies) {
        this.pomDependencies = pomDependencies;
    }

    @Override
    public String toString() {
        return "AppletDependency{" +
                "name='" + name + '\'' +
                ", pomDependencies=" + pomDependencies +
                '}';
    }
}
