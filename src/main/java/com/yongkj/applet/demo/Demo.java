package com.yongkj.applet.demo;

import com.yongkj.AppTest;
import com.yongkj.deploy.pojo.po.PomXml;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.ApiUtil;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

public class Demo {

    private final String msg;

    private Demo() {
        Object value = GenUtil.getValue("demo.yaml", "msg");
        this.msg = GenUtil.objToStr(value);
    }

    private void test() {
        GenUtil.writeConfig("demo.yaml", Map.of("msg", "世界，你好！"));
        LogUtil.loggerLine(Log.of("Demo", "test", "msg", msg));
        final String dir = System.getProperty("user.dir");
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "dir", dir));
        String path = AppTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "path", path));
        path = FileUtil.dirname(path);
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "path", path));
        path = FileUtil.appDir();
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "appDir", path));
        path = FileUtil.getAbsPath(false, "src", "main", "resources");
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "absPath", path));
        path = FileUtil.getAbsPath(true, "src", "main", "resources");
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "absPath", path));
    }

    private void test1() {
        String humpName = GenUtil.toHump("build-script-service.java");
        LogUtil.loggerLine(Log.of("Demo", "test1", "humpName", humpName));
        System.out.println("----------------------------------------------------------------- ");
        String lineName = GenUtil.toLine("buildScriptService.java");
        LogUtil.loggerLine(Log.of("Demo", "test1", "lineName", lineName));
        System.out.println("----------------------------------------------------------------- ");
    }

    private void test2() {
        String url = "http://localhost:8899/path/demo";
        String data = ApiUtil.requestByGetWithParams(url, new HashMap<>());
        LogUtil.loggerLine(Log.of("Demo", "test2", "data", data));
        System.out.println("----------------------------------------------------------------- ");
        ApiUtil.setProxyEnable(true);
        url = "https://www.google.com";
        data = ApiUtil.requestByGetWithParams(url, new HashMap<>());
        LogUtil.loggerLine(Log.of("Demo", "test2", "data", data));
        System.out.println("----------------------------------------------------------------- ");
    }

    private void test3() {
        String path = "D:\\Document\\MyCodes\\Github\\script-java\\pom.xml";
        FileUtil.modFile(path, "(<plugins>[\\s\\S]*?</plugins>)", PomXml.getInternalLibraries());
    }

    public static void run(String[] args) {
        Demo demo = new Demo();
//        demo.test3();
        demo.test2();
//        demo.test1();
//        demo.test();
    }

}
