package com.yongkj.pojo.dto;

public class Log {

    private String className;
    private String methodName;
    private String paramName;
    private Object value;

    private Log(String className, String methodName, String paramName, Object value) {
        this.className = className;
        this.methodName = methodName;
        this.paramName = paramName;
        this.value = value;
    }

    public static Log of(String className, String methodName, String paramName, Object value) {
        return new Log(className, methodName, paramName, value);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}