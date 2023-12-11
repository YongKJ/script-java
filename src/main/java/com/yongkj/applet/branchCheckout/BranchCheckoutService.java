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
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BranchCheckoutService {

    private final String configTag;
    private final String configPath;
    private final String projectPath;
    private final String privateKeyPath;
    private final List<String> pullBranchs;
    private final List<String> projectNames;
    private final Map<String, Object> mapTagBranch;
    private final List<Map<String, Object>> branchs;

    private BranchCheckoutService() {
        branchs = GenUtil.getListMap("branchs");
        configTag = GenUtil.getValue("config-tag");
        configPath = GenUtil.getValue("config-path");
        pullBranchs = GenUtil.getList("pull-branchs");
        projectPath = GenUtil.getValue("project-path");
        projectNames = GenUtil.getList("project-names");
        privateKeyPath = GenUtil.getValue("private-key-path");
        mapTagBranch = GenUtil.getMap("branch-config-relation");
    }

    private void apply() {
        for (String projectName : projectNames) {
            Map<String, Object> branchInfo = branchs.stream()
                    .filter(po -> Objects.equals(po.get("project-name"), projectName))
                    .findFirst().orElse(null);

            String branch = configTag;
            if (branchInfo != null) {
                branch = (String) branchInfo.get("branch");
            }
            branchCheckOutAndPull(projectName, branch);
            configCopy(projectName);
            bootCopy(projectName);
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

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "configCopy", "srcConfigPath", srcConfigPath));
            LogUtil.loggerLine(Log.of("BranchCheckoutService", "configCopy", "desConfigPath", desConfigPath));
            System.out.println("---------------------------------------------------------------------------------------------");
        }
    }

    private void bootCopy(String projectName) {
        String srcBootPath = getBootPath(projectName, true);
        if (FileUtil.exist(srcBootPath)) {
            String desBootPath = getBootPath(projectName, false);
            if (FileUtil.exist(desBootPath)) {
                FileUtil.delete(desBootPath);
            }
            FileUtil.copy(srcBootPath, desBootPath);

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "bootCopy", "srcBootPath", srcBootPath));
            LogUtil.loggerLine(Log.of("BranchCheckoutService", "bootCopy", "desBootPath", desBootPath));
            System.out.println("---------------------------------------------------------------------------------------------");
        }
    }

    private void branchCheckOutAndPull(String projectName, String branch) {
        String gitPath = getProjectGitPath(projectName);
        try {
            Git git = Git.open(new File(gitPath));

            if (!hasRemoteBranch(git, branch)) {
                branch = (String) mapTagBranch.get(configTag);
            }

            String refName = "";
            String branchName = git.getRepository().getBranch();
            if (!Objects.equals(branchName, branch)) {
                Ref ref = git.checkout().setName(branch).call();
                refName = ref.getName();
            }

            PullResult pullResult = git.pull().setTransportConfigCallback(this::setSshSessionFactory).call();
            if (pullResult.isSuccessful() && !pullBranchs.contains(branch)) {
                branchSyncPull(git);
            }

            String url = git.getRepository().getConfig().getString("remote", "origin", "url");

            git.close();

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOut", "pullResult.isSuccessful()", pullResult.isSuccessful()));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOut", "branchName", branchName));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOut", "refName", refName));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOut", "url", url));
            System.out.println("---------------------------------------------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void branchSyncPull(Git git) {
        try {
            for (String pullBranch : pullBranchs) {
                PullResult pullRemoteResult = git.pull()
                        .setRemoteBranchName(pullBranch)
                        .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                        .setTransportConfigCallback(this::setSshSessionFactory).call();

                if (pullRemoteResult.isSuccessful()) {
                    Iterable<PushResult> pushResults = git.push().setTransportConfigCallback(this::setSshSessionFactory).call();
                    for (PushResult pushResult : pushResults) {
                        LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchSyncPull", "pushResult.getMessages()", pushResult.getMessages()));
                        System.out.println("---------------------------------------------------------------------------------------------");
                    }
                }

                LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchSyncPull", "pullRemoteResult.isSuccessful()", pullRemoteResult.isSuccessful()));
                System.out.println("---------------------------------------------------------------------------------------------");
            }
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

    private String getBootPath(String projectName, boolean isSrcPath) {
        return isSrcPath ? getListPath(configPath, projectName, String.format("bootstrap-%s.yml", configTag)) :
                getListPath(getProjectResourcesPath(projectName), "bootstrap-dev.yml");
    }

    private String getConfigPath(String projectName, boolean isSrcPath) {
        return isSrcPath ? getListPath(configPath, projectName, String.format("application-%s.yml", configTag)) :
                getListPath(getProjectResourcesPath(projectName), "application.yml");
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
        new BranchCheckoutService().apply();
    }

}
