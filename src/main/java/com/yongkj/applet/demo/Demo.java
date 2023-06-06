package com.yongkj.applet.demo;

import com.yongkj.AppTest;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

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

    public static void run() {
        new Demo().test();
    }

}
