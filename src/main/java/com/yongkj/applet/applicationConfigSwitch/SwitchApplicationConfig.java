package com.yongkj.applet.applicationConfigSwitch;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SwitchApplicationConfig {

    private final String branch;
    private final boolean isTest;
    private final boolean isFilter;
    private final String pullBranch;
    private final String configName;
    private final String configPath;
    private final String projectPath;
    private final String filterBranch;
    private final String privateKeyPath;
    private final List<String> projectNames;
    private final List<String> filterProjectNames;

    private SwitchApplicationConfig() {
        branch = GenUtil.getValue("branch");
        isTest = branch.contains("test");
        configPath = GenUtil.getValue("config-path");
        projectNames = GenUtil.getList("project-name");
        projectPath = GenUtil.getValue("project-path");
        privateKeyPath = GenUtil.getValue("private-key-path");
        configName = "application-" + (isTest ? "test" : "dev") + ".yml";

        Map<String, Object> mapFilter = GenUtil.getMap("filter");
        filterProjectNames = (List<String>) mapFilter.get("project-name");
        pullBranch = GenUtil.objToStr(mapFilter.get("pullBranch"));
        filterBranch = GenUtil.objToStr(mapFilter.get("branch"));
        isFilter = (Boolean) mapFilter.get("enable");
    }

    private void apply() {
        LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "apply", "projectNames", projectNames));
        System.out.println("---------------------------------------------------------------------------------------------");

        for (String projectName : projectNames) {
            branchCheckOutAndPull(projectName);

            String srcPath = getApplicationSrcPath(projectName);
            if (!FileUtil.exist(srcPath)) {
                continue;
            }
            String desPath = getApplicationDesPath(projectName);
            if (FileUtil.exist(desPath)) {
                FileUtil.delete(desPath);
            }
            FileUtil.copy(srcPath, desPath);

            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "apply", "srcPath", srcPath));
            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "apply", "desPath", desPath));
            System.out.println("---------------------------------------------------------------------------------------------");
        }
    }

    private void branchCheckOutAndPull(String projectName) {
        String gitPath = getProjectGitPath(projectName);
        try {
            Git git = Git.open(new File(gitPath));

            String branch = this.branch;
            if (isTest && isFilter && filterProjectNames.contains(projectName)) {
                branch = filterBranch;
            }

            String refName = "";
            String branchName = git.getRepository().getBranch();
            if (!Objects.equals(branchName, branch)) {
                Ref ref = git.checkout().setName(branch).call();
                refName = ref.getName();
            }

            boolean pullRemoteResultFlag = false;
            PullResult pullResult = git.pull().setTransportConfigCallback(this::setSshSessionFactory).call();
            if (isTest && isFilter && filterProjectNames.contains(projectName)) {
                PullResult pullRemoteResult = git.pull().setRemoteBranchName(pullBranch)
                        .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                        .setTransportConfigCallback(this::setSshSessionFactory).call();
                pullRemoteResultFlag = pullRemoteResult.isSuccessful();

                Iterable<PushResult> pushResults = git.push().setTransportConfigCallback(this::setSshSessionFactory).call();
                for (PushResult pushResult : pushResults) {
                    LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "branchCheckOut", "pushResult.getMessages()", pushResult.getMessages()));
                    System.out.println("---------------------------------------------------------------------------------------------");
                }
            }

            String url = git.getRepository().getConfig().getString("remote", "origin", "url");

            git.close();

            if (isTest && isFilter && filterProjectNames.contains(projectName)) {
                LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "branchCheckOut", "pullRemoteResult.isSuccessful()", pullRemoteResultFlag));
                System.out.println("---------------------------------------------------------------------------------------------");
            }

            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "branchCheckOut", "pullResult.isSuccessful()", pullResult.isSuccessful()));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "branchCheckOut", "branchName", branchName));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "branchCheckOut", "refName", refName));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("SwitchApplicationConfig", "branchCheckOut", "url", url));
            System.out.println("---------------------------------------------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private String getApplicationDesPath(String projectName) {
        return getListPath(getProjectResourcesPath(projectName), "application.yml");
    }

    private String getApplicationSrcPath(String projectName) {
        return getListPath(configPath, projectName, configName);
    }

    private String getProjectGitPath(String projectName) {
        return getListPath(getProjectPath(projectName), ".git");
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
        new SwitchApplicationConfig().apply();
    }

}
