package com.yongkj.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class RemoteUtil {

    private RemoteUtil() {
    }

    private static String workFolder = "";

    public static void execLocalCmd(String command) {
        execLocalCmd(Collections.singletonList(command), workFolder);
    }

    public static void execLocalCmd(List<String> lstCommand, String localPrefix) {
        try {
            String cmd = (System.getProperty("os.name").contains("dows") ? "cmd /c " : "") + String.join(" & ", lstCommand);
            Process  process = Runtime.getRuntime().exec(cmd, null, new File(localPrefix));
            printInfo(process.getInputStream());
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printInfo(InputStream in) {
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

    public static void changeWorkFolder(String home) {
        workFolder = home;
    }

}
