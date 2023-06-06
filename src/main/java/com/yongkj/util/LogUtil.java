package com.yongkj.util;

import com.yongkj.config.Global;
import com.yongkj.pojo.dto.Log;

public class LogUtil {

    private LogUtil() {
    }

    public static void loggerLine(Log log) {
        if (!Global.isLogEnable()) return;
        System.out.print("[" + log.getClassName() + "] " + log.getMethodName() + " -> " + log.getParamName() + ": ");
        System.out.println(log.getValue());
    }

    public static void logger(Log log) {
        if (!Global.isLogEnable()) return;
        System.out.print("[" + log.getClassName() + "] " + log.getMethodName() + " -> " + log.getParamName() + ": ");
        System.out.print(log.getValue());
    }

}
