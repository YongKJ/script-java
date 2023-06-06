package com.yongkj.util;

public class CmdUtil {

    private CmdUtil() {
    }

    public static String copyMavenDependencies() {
        return "mvn -f pom.xml dependency:copy-dependencies -DoutputDirectory=" + FileUtil.getAbsPath(false, "libs");
    }

    public static String buildJavaScript() {
        return "mvn clean package -DskipTests";
    }

}
