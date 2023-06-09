package com.yongkj.applet.tomcatService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class TomcatService {

    private final String tomcatBin = "D:\\Software\\scoop\\apps\\tomcat8\\8.5.77\\bin";

    private void start() {
        String startupBat = tomcatBin + "\\startup.bat";
        execLocalCmdByAsync(startupBat);
    }

    private void shutdown() {
        String shutdownBat = tomcatBin + "\\shutdown.bat";
        execLocalCmd(shutdownBat);
    }

    public static void main(String[] args) {
        if ((args.length == 0 || args[0].equals("start"))) {
            new TomcatService().start();
        } else {
            new TomcatService().shutdown();
        }
    }

    public void execLocalCmdByAsync(String command) {
        execLocalCmdByAsync(Collections.singletonList(command), tomcatBin);
    }

    public void execLocalCmdByAsync(List<String> lstCommand, String localPrefix) {
        String cmd = (System.getProperty("os.name").contains("dows") ? "cmd /c start " : "") +
                String.join(" & ", lstCommand);
        try {
            if (localPrefix.length() == 0) {
                Runtime.getRuntime().exec(cmd);
            } else {
                Runtime.getRuntime().exec(cmd, null, new File(localPrefix));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execLocalCmd(String command) {
        execLocalCmd(Collections.singletonList(command), tomcatBin);
    }

    public void execLocalCmd(List<String> lstCommand, String localPrefix) {
        String cmd = (System.getProperty("os.name").contains("dows") ? "cmd /c " : "") +
                String.join(" & ", lstCommand);
        try {
            Process process = localPrefix.length() == 0 ? Runtime.getRuntime().exec(cmd) :
                    Runtime.getRuntime().exec(cmd, null, new File(localPrefix));
            printInfo(process.getInputStream());
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printInfo(InputStream in) {
        InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);
        try {
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                System.out.println(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
