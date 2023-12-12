package com.yongkj.applet.branchCheckout;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BranchCheckout {

    private final String configTag;
    private final String configPath;
    private final String projectPath;
    private final boolean branchClean;
    private final String privateKeyPath;
    private final List<String> pullBranchs;
    private final List<String> projectNames;
    private final Map<String, Object> mapTagBranch;
    private final List<Map<String, Object>> branchs;

    private BranchCheckout() {
        branchs = GenUtil.getListMap("branchs");
        configTag = GenUtil.getValue("config-tag");
        configPath = GenUtil.getValue("config-path");
        pullBranchs = GenUtil.getList("pull-branchs");
        projectPath = GenUtil.getValue("project-path");
        projectNames = GenUtil.getList("project-names");
        branchClean = GenUtil.getBoolean("branch-clean");
        privateKeyPath = GenUtil.getValue("private-key-path");
        mapTagBranch = GenUtil.getMap("branch-config-relation");
    }

    private void apply() {
        for (String projectName : projectNames) {
            Map<String, Object> branchInfo = branchs.stream()
                    .filter(po -> Objects.equals(po.get("project-name"), projectName))
                    .findFirst().orElse(null);

            String branch = branchInfo == null ? getBranchByConfigTag() : (String) branchInfo.get("branch");
            if (!StringUtils.hasText(branch)) {
                continue;
            }

            branchCheckOutAndPull(projectName, branch);
            configCopy(projectName);
            bootCopy(projectName);
        }
    }

    private void configCopy(String projectName) {
        String srcConfigPath = getConfigPath(projectName, true);
        if (!FileUtil.exist(srcConfigPath)) {
            return;
        }

        String desConfigPath = getConfigPath(projectName, false);
        if (FileUtil.exist(desConfigPath)) {
            FileUtil.delete(desConfigPath);
        }
        FileUtil.copy(srcConfigPath, desConfigPath);

        LogUtil.loggerLine(Log.of("BranchCheckoutService", "configCopy", "srcConfigPath", srcConfigPath));
        LogUtil.loggerLine(Log.of("BranchCheckoutService", "configCopy", "desConfigPath", desConfigPath));
        System.out.println("---------------------------------------------------------------------------------------------");
    }

    private void bootCopy(String projectName) {
        String srcBootPath = getBootPath(projectName, true);
        if (!FileUtil.exist(srcBootPath)) {
            return;
        }

        String desBootPath = getBootPath(projectName, false);
        if (FileUtil.exist(desBootPath)) {
            FileUtil.delete(desBootPath);
        }
        FileUtil.copy(srcBootPath, desBootPath);

        LogUtil.loggerLine(Log.of("BranchCheckoutService", "bootCopy", "srcBootPath", srcBootPath));
        LogUtil.loggerLine(Log.of("BranchCheckoutService", "bootCopy", "desBootPath", desBootPath));
        System.out.println("---------------------------------------------------------------------------------------------");
    }

    private void branchCheckOutAndPull(String projectName, String branch) {
        String gitPath = getProjectGitPath(projectName);
        try {
            Git git = Git.open(new File(gitPath));

            String refName = "";
            branch = getReasonableCheckBranch(git, branch);
            String branchName = git.getRepository().getBranch();
            if (!Objects.equals(branchName, branch) && !hasBranch(git, branch, false)) {
                refName = branchCheckOutAndPull(git, branch);
            } else {
                if (!Objects.equals(branchName, branch)) {
                    Ref ref = git.checkout().setName(branch).call();
                    refName = ref.getName();
                }

                PullResult pullResult = git.pull().setTransportConfigCallback(this::setSshSessionFactory).call();
                if (pullResult.isSuccessful() && !pullBranchs.contains(branch)) {
                    boolean pullFlag = branchSyncPull(git);

                    if (pullFlag) {
                        Iterable<PushResult> pushResults = git.push().setTransportConfigCallback(this::setSshSessionFactory).call();
                        for (PushResult pushResult : pushResults) {
                            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPull", "pushResult.getMessages()", pushResult.getMessages()));
                            System.out.println("---------------------------------------------------------------------------------------------");
                        }
                    }

                    LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPull", "pullFlag", pullFlag));
                    System.out.println("---------------------------------------------------------------------------------------------");
                }

                LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPull", "pullResult.isSuccessful()", pullResult.isSuccessful()));
                System.out.println("---------------------------------------------------------------------------------------------");
            }

            if (branchClean) {
                branchClean(git, branch);
            }

            String url = git.getRepository().getConfig().getString("remote", "origin", "url");

            git.close();

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPull", "branchName", branchName));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPull", "refName", refName));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPull", "url", url));
            System.out.println("---------------------------------------------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getReasonableCheckBranch(Git git, String branch) {
        if (branch.contains("feat") || branch.contains("fix")) {
            return branch;
        }
        try {
            List<Ref> lstBranch = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            List<String> branchNames = lstBranch.stream().map(Ref::getName).collect(Collectors.toList());
            String branchName = branchNames.stream().filter(po -> po.endsWith(branch)).findFirst().orElse(null);
            if (StringUtils.hasText(branchName)) {
                return branch;
            }

            for (Map.Entry<String, Object> map : mapTagBranch.entrySet()) {
                branchName = branchNames.stream().filter(po -> po.endsWith(map.getKey())).findFirst().orElse(null);
                if (StringUtils.hasText(branchName)) {
                    return map.getKey();
                }

                branchName = branchNames.stream().filter(po -> po.endsWith((String) map.getValue())).findFirst().orElse(null);
                if (StringUtils.hasText(branchName)) {
                    return pullBranchs.stream().filter(po -> po.contains((String) map.getValue())).findFirst().orElse("master");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "master";
    }

    private void branchClean(Git git, String branch) {
        if (!(branch.contains("feat") || branch.contains("fix"))) {
            return;
        }

        try {
            List<Ref> lstBranch = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            List<String> lstBranchName = new ArrayList<>();
            for (Ref branchRef : lstBranch) {
                if (!branchRef.getName().endsWith(branch) &&
                        branchRef.getName().contains("dxj") &&
                        (branchRef.getName().contains("feat") ||
                                branchRef.getName().contains("fix"))) {
                    lstBranchName.add(branchRef.getName());
                }
            }

            String[] delBranchNames = new String[lstBranchName.size()];
            for (int i = 0; i < lstBranchName.size(); i++) {
                delBranchNames[i] = lstBranchName.get(i);
            }

            List<String> branchNames = git.branchDelete().setBranchNames(delBranchNames).call();

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchClean", "branchNames", branchNames));
            System.out.println("---------------------------------------------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String branchCheckOutAndPull(Git git, String branch) {
        try {
            if (!(branch.contains("feat") || branch.contains("fix"))) {
                if (hasBranch(git, branch, true)) {
                    Ref ref = git.checkout()
                            .setName(branch)
                            .setCreateBranch(true)
                            .setStartPoint(String.format("origin/%s", branch))
                            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).call();
                    return ref.getName();
                }
                return "";
            }
            if (branch.contains("feat")) {
                String tempBranch = pullBranchs.stream()
                        .filter(po -> po.contains("develop")).findFirst().orElse("develop");

                return branchCheckOutAndPullByLocal(git, branch, tempBranch);
            }
            if (branch.contains("fix")) {
                String tempBranch = pullBranchs.stream()
                        .filter(po -> po.contains((String) mapTagBranch.get(configTag)))
                        .findFirst().orElse((String) mapTagBranch.get(configTag));

                return branchCheckOutAndPullByLocal(git, branch, tempBranch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String branchCheckOutAndPullByLocal(Git git, String branch, String tempBranch) {
        try {
            branchCheckOutAndPullByTemp(git, tempBranch);

            Ref ref = git.checkout()
                    .setName(branch)
                    .setCreateBranch(true)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).call();

            boolean pullFlag = branchSyncPull(git, Collections.singletonList(tempBranch));
            if (pullFlag) {
                Iterable<PushResult> pushResults = git.push().setTransportConfigCallback(this::setSshSessionFactory).call();
                for (PushResult pushResult : pushResults) {
                    LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPullByLocal", "pushResult.getMessages()", pushResult.getMessages()));
                    System.out.println("---------------------------------------------------------------------------------------------");
                }
            }

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPullByLocal", "refName", ref.getName()));
            System.out.println("---------------------------------------------------------------------------------------------");

            return ref.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void branchCheckOutAndPullByTemp(Git git, String branch) {
        try {
            String branchName = git.getRepository().getBranch();
            if (!Objects.equals(branchName, branch) && !hasBranch(git, branch, false)) {
                if (hasBranch(git, branch, true)) {
                    Ref ref = git.checkout()
                            .setName(branch)
                            .setCreateBranch(true)
                            .setStartPoint(String.format("origin/%s", branch))
                            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).call();
                    String refName = ref.getName();

                    LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPullByTemp", "branchName", branchName));
                    System.out.println("---------------------------------------------------------------------------------------------");

                    LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPullByTemp", "refName", refName));
                    System.out.println("---------------------------------------------------------------------------------------------");
                }
                return;
            }

            String refName = "";
            if (!Objects.equals(branchName, branch)) {
                Ref ref = git.checkout().setName(branch).call();
                refName = ref.getName();
            }

            PullResult pullResult = git.pull().setTransportConfigCallback(this::setSshSessionFactory).call();

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPullByTemp", "pullResult.isSuccessful()", pullResult.isSuccessful()));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPullByTemp", "branchName", branchName));
            System.out.println("---------------------------------------------------------------------------------------------");

            LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchCheckOutAndPullByTemp", "refName", refName));
            System.out.println("---------------------------------------------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean branchSyncPull(Git git) {
        return branchSyncPull(git, new ArrayList<>());
    }

    private boolean branchSyncPull(Git git, List<String> branchFilters) {
        try {
            boolean pullFlag = true;
            for (String pullBranch : pullBranchs) {
                if (branchFilters != null && !branchFilters.isEmpty() && branchFilters.contains(pullBranch)) {
                    continue;
                }

                PullResult pullRemoteResult = git.pull()
                        .setRemoteBranchName(pullBranch)
                        .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                        .setTransportConfigCallback(this::setSshSessionFactory).call();

                pullFlag = pullFlag && pullRemoteResult.isSuccessful();

                LogUtil.loggerLine(Log.of("BranchCheckoutService", "branchSyncPull", "pullRemoteResult.isSuccessful()", pullRemoteResult.isSuccessful()));
                System.out.println("---------------------------------------------------------------------------------------------");
            }

            return pullFlag;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    private boolean hasBranch(Git git, String branch, boolean isRemote) {
        try {
            List<Ref> lstBranch = !isRemote ? git.branchList().call() :
                    git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
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

    private String getBranchByConfigTag() {
        String branch = (String) mapTagBranch.get(configTag);
        return pullBranchs.stream().filter(po -> po.contains(branch)).findFirst().orElse("local");
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
        new BranchCheckout().apply();
    }

}
