package com.yongkj.applet.branchCheckout;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BranchCheckoutService {

    private final String branch;
    private final String configPath;
    private final String projectPath;
    private final String privateKeyPath;
    private final List<String> pullBranchs;
    private final List<String> projectNames;
    private final Map<String, Object> mapBranchConfig;

    private BranchCheckoutService() {
        branch = GenUtil.getValue("branch");
        configPath = GenUtil.getValue("config-path");
        pullBranchs = GenUtil.getList("pull-branchs");
        projectNames = GenUtil.getList("project-name");
        projectPath = GenUtil.getValue("project-path");
        privateKeyPath = GenUtil.getValue("private-key-path");
        mapBranchConfig = GenUtil.getMap("branch-config-relation");
    }

    private void apply() {
        for (String projectName : projectNames) {
            branchCheckOutAndPull(projectName);
        }
    }

    private void configCopy(String projectName) {
        String srcConfigPath = getConfigPath(projectName, true);
        if (FileUtil.exist(srcConfigPath)) {
            String desConfigPath = getConfigPath(projectName, false);
            if (FileUtil.exist(desConfigPath)) {
                FileUtil.delete(desConfigPath);
            }
            FileUtil.copy(srcConfigPath, desConfigPath);

            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "apply", "srcConfigPath", srcConfigPath));
            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "apply", "desConfigPath", desConfigPath));
            System.out.println("---------------------------------------------------------------------------------------------");
        }
    }

    private void bootConfigCopy(String projectName) {

    }

    private void branchCheckOutAndPull(String projectName) {

    }

    private void setSshSessionFactory(Transport transport) {
        ((SshTransport) transport).setSshSessionFactory(new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch sch = super.createDefaultJSch(fs);
                sch.addIdentity(privateKeyPath);
                return sch;
            }
        });
    }

    public boolean hasRemoteBranch(Git git, String branch) {
        try {
            List<Ref> lstBranch = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
            List<String> branchNames = lstBranch.stream().map(Ref::getName).collect(Collectors.toList());

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "hasRemoteBranch", "branchNames.size()", branchNames.size()));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "hasRemoteBranch", "branchNames", branchNames));
            System.out.println("---------------------------------------------------------------------------------------------");

            for (String branchName : branchNames) {
                if (branchName.endsWith(branch)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getConfigPath(String projectName, boolean isSrcPath) {
        return isSrcPath ? getListPath(configPath, projectName, getConfigName()) :
                getListPath(getProjectResourcesPath(projectName), "application.yml");
    }

    private String getConfigName() {
        String configName = "application-%s.yml";
        return String.format(configName, getConfigTag());
    }

    private String getConfigTag() {
        String branchKey = branch;
        if (branchKey.contains("-")) {
            branchKey = branchKey.split("-")[0];
        }
        if (branchKey.contains("/")) {
            branchKey = branchKey.split("/")[0];
        }
        if (mapBranchConfig.containsKey(branchKey)) {
            return (String) mapBranchConfig.get(branchKey);
        }
        return "dev";
    }

    private String getProjectResourcesPath(String projectName) {
        return getListPath(getProjectStartPath(projectName), "src", "main", "resources");
    }

    private String getProjectStartPath(String projectName) {
        String projectItemPath = getListPath(getProjectPath(projectName), "service-order-start");
        if (FileUtil.exist(projectItemPath)) {
            return projectItemPath;
        }
        projectItemPath = getListPath(getProjectPath(projectName), projectName + "-start");
        if (FileUtil.exist(projectItemPath)) {
            return projectItemPath;
        }
        return getProjectPath(projectName);
    }

    private String getProjectPath(String projectName) {
        return getListPath(projectPath, projectName);
    }

    private String getListPath(String... name) {
        return String.join(File.separator, name);
    }

    public static void run(String[] args) {
        new BranchCheckoutService().apply();
    }

}
