package com.yongkj.applet.librariesPull;

import com.yongkj.util.CmdUtil;
import com.yongkj.util.FileUtil;
import com.yongkj.util.RemoteUtil;

import java.io.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class LibrariesPull {

    private static boolean PULL_FLAG = false;

    private void apply() {
        List<File> lstFile = FileUtil.list(FileUtil.appDir(true));
        for (File file : lstFile) {
            if (!file.getName().contains(".jar")) continue;
            if (file.getName().contains("libraries-pull")) continue;
            exportPom(file.getAbsolutePath());
        }
    }

    private void exportPom(String jarPath) {
        try {
            JarEntry entry;
            String separator = jarPath.contains("/") ? "/" : "\\";
            InputStream inputStream = new FileInputStream(jarPath);
            JarInputStream jarInputStream = new JarInputStream(inputStream);
            String libsPath = FileUtil.dirname(jarPath) + separator + "libs";
            String pomPath = FileUtil.dirname(jarPath) + separator + "pom.xml";
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) continue;
                if (!(entry.getName().contains("pom.xml") ||
                        entry.getName().contains(".yaml"))) continue;
                if (entry.getName().contains("pom.xml") &&
                        FileUtil.exist(libsPath)) continue;
                int index = entry.getName().lastIndexOf("/");
                String fileName = entry.getName().substring(index + 1);
                fileName = FileUtil.dirname(jarPath) + separator + fileName;
                if (entry.getName().contains(".yaml") && FileUtil.exist(fileName)) continue;
                if (entry.getName().contains(".yaml")) PULL_FLAG = true;
                writeFile(new JarFile(jarPath).getInputStream(entry), fileName);
            }
            inputStream.close();
            jarInputStream.close();
            if (!FileUtil.exist(libsPath)) {
                PULL_FLAG = true;
                pullLibraries(pomPath, libsPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeFile(InputStream stream, String filePath) {
        try {
            OutputStream outputStream = new FileOutputStream(filePath);
            int len;
            byte[] b = new byte[1024];
            while ((len = stream.read(b)) > 0) {
                outputStream.write(b, 0, len);
            }
            outputStream.flush();
            outputStream.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pullLibraries(String pomPath, String libsPath) {
        String cmd = CmdUtil.pullMavenDependencies(pomPath, libsPath);
        RemoteUtil.execLocalCmd(cmd);
        FileUtil.delete(pomPath);
    }

    public static void run(String[] args) {
        new LibrariesPull().apply();
    }

    public static boolean isPullFlag() {
        return PULL_FLAG;
    }
}
