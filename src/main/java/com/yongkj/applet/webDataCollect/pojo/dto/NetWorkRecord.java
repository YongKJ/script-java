package com.yongkj.applet.webDataCollect.pojo.dto;

import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

public class NetWorkRecord {

    private Long endTime;
    private Long startTime;
    private HttpRequest request;
    private HttpResponse response;

    public NetWorkRecord() {
        this.endTime = 0L;
        this.startTime = 0L;
        this.request = null;
        this.response = null;
    }

    private NetWorkRecord(Long startTime, Long endTime, HttpRequest request, HttpResponse response) {
        this.endTime = endTime;
        this.startTime = startTime;
        this.request = request;
        this.response = response;
    }

    public static NetWorkRecord of(Long startTime, Long endTime, HttpRequest request, HttpResponse response) {
        return new NetWorkRecord(startTime, endTime, request, response);
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }
}
