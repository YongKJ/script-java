package com.yongkj.applet.tomcatService;

import com.yongkj.applet.librariesPull.LibrariesPull;
import com.yongkj.util.GenUtil;
import com.yongkj.util.RemoteUtil;

import java.util.concurrent.TimeUnit;

public class TomcatService {

    private final String tomcatBin;

    private TomcatService() {
        tomcatBin = GenUtil.getValue("tomcat-bin");
    }

    private void start() {
        String startupBat = tomcatBin + "\\startup.bat";
        RemoteUtil.changeWorkFolder(tomcatBin);
        RemoteUtil.execLocalCmdByAsync(startupBat);
    }

    private void shutdown() {
        String shutdownBat = tomcatBin + "\\shutdown.bat";
        RemoteUtil.changeWorkFolder(tomcatBin);
        RemoteUtil.execLocalCmd(shutdownBat);
    }

    public static void run(String[] args) {
        LibrariesPull.run(args);
        if (LibrariesPull.isPullFlag()) return;
        if (args.length > 0) {
            if (args[0].equals("start")) {
                new TomcatService().start();
            } else if (args[0].equals("stop")) {
                new TomcatService().shutdown();
            }
            return;
        }
        try {
            new TomcatService().start();
            TimeUnit.SECONDS.sleep(10);
            new TomcatService().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
