package com.yongkj.pojo.dto;

public class ResponseData<T> {

    private T data;
    private String msg;
    private Integer code;
    private Boolean success;

    public ResponseData(T data, String msg, Integer code, Boolean success) {
        this.data = data;
        this.msg = msg;
        this.code = code;
        this.success = success;
    }

    public static <T> ResponseData<T> of(T data, String msg, Integer code, Boolean success) {
        return new ResponseData<T>(data, msg, code, success);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
