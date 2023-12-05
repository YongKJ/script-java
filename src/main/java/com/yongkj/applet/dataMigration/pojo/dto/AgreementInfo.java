package com.yongkj.applet.dataMigration.pojo.dto;

import com.yongkj.util.GenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgreementInfo {

    private String srcUrl;
    private String desUrl;
    private String srcName;
    private String desName;

    private AgreementInfo() {
    }

    private AgreementInfo(String srcUrl, String desUrl, String srcName, String desName) {
        this.srcUrl = srcUrl;
        this.desUrl = desUrl;
        this.srcName = srcName;
        this.desName = desName;
    }

    public static AgreementInfo of(String srcUrl, String desUrl, String srcName, String desName) {
        return new AgreementInfo(srcUrl, desUrl, srcName, desName);
    }

    public static List<AgreementInfo> getAgreements(List<Map<String, Object>> lstData) {
        List<AgreementInfo> lstAgreement = new ArrayList<>();
        for (Map<String, Object> data : lstData) {
            String srcUrl = GenUtil.objToStr(data.get("srcUrl"));
            String desUrl = GenUtil.objToStr(data.get("desUrl"));
            String srcName = GenUtil.objToStr(data.get("srcName"));
            String desName = GenUtil.objToStr(data.get("desName"));
            lstAgreement.add(AgreementInfo.of(srcUrl, desUrl, srcName, desName));
        }
        return lstAgreement;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }

    public String getDesUrl() {
        return desUrl;
    }

    public void setDesUrl(String desUrl) {
        this.desUrl = desUrl;
    }

    public String getSrcName() {
        return srcName;
    }

    public void setSrcName(String srcName) {
        this.srcName = srcName;
    }

    public String getDesName() {
        return desName;
    }

    public void setDesName(String desName) {
        this.desName = desName;
    }
}
