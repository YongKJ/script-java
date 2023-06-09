package com.yongkj.applet.tomcatService;

import com.yongkj.util.GenUtil;
import com.yongkj.util.RemoteUtil;

public class TomcatService {

    private final String tomcatBin;

    private TomcatService() {
        Object value = GenUtil.getValue("tomcat-service.yaml", "tomcat-bin");
        tomcatBin = GenUtil.objToStr(value);
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
        if ((args.length == 0 || args[0].equals("start"))) {
            new TomcatService().start();
        } else if (args[0].equals("stop")) {
            new TomcatService().shutdown();
        }
    }

}
