package com.yongkj.applet.dataMigration.api;

import com.yongkj.util.ApiUtil;
import com.yongkj.util.GenUtil;

import java.util.HashMap;
import java.util.Map;

public class TokenApiService {

    private static final String LOGIN = "%s/v1/bc/company/admin/login";
    private static final String SHOP_DETAIL = "%s/v1/bc/company/shop/detail";
    private static final String ACCOUNT_LOGIN = "%s/v1/bc/company/admin/accountLogin";
    private static final String DETAIL_BY_USER = "%s/v1/bc/company/organization/detailByUser";

    public Long getShopIdByShopDetail(String baseUrl, String token) {
        Map<String, String> mapHeader = new HashMap<>();
        mapHeader.put("token", token);

        String responseDataStr =
                ApiUtil.requestByGetWithHeaderToEntity(String.format(SHOP_DETAIL, baseUrl), mapHeader, String.class);

        Map<String, Object> mapData = (Map<String, Object>) GenUtil.fromJsonString(responseDataStr, Map.class).get("data");

        return GenUtil.objToLong(mapData.get("id"));
    }

    public Long getShopIdByToken(String baseUrl, String token) {
        Map<String, String> mapHeader = new HashMap<>();
        mapHeader.put("token", token);

        String responseDataStr =
                ApiUtil.requestByGetWithHeaderToEntity(String.format(DETAIL_BY_USER, baseUrl), mapHeader, String.class);

        Map<String, Object> mapData = (Map<String, Object>) GenUtil.fromJsonString(responseDataStr, Map.class).get("data");

        Map<String, Object> shopInfo = (Map<String, Object>) mapData.get("shopInfo");

        return GenUtil.objToLong(shopInfo.get("id"));
    }

    public Long getOrganizationIdByToken(String baseUrl, String token) {
        Map<String, String> mapHeader = new HashMap<>();
        mapHeader.put("token", token);

        String responseDataStr =
                ApiUtil.requestByGetWithHeaderToEntity(String.format(DETAIL_BY_USER, baseUrl), mapHeader, String.class);

        Map<String, Object> mapData = (Map<String, Object>) GenUtil.fromJsonString(responseDataStr, Map.class).get("data");

        Map<String, Object> organizationInfo = (Map<String, Object>) mapData.get("organizationInfo");

        return GenUtil.objToLong(organizationInfo.get("id"));
    }

    public String getLoginToken(String baseUrl, String applyCode, Long mobile) {
        return getLoginToken(baseUrl, applyCode, mobile, "8888");
    }

    public String getLoginToken(String baseUrl, String applyCode, Long mobile, String code) {
        Map<String, Object> mapBody = new HashMap<>();
        mapBody.put("applyCode", applyCode);
        mapBody.put("mobile", mobile);
        mapBody.put("code", code);

        String responseDataStr =
                ApiUtil.requestWithBodyDataByPostToEntity(String.format(LOGIN, baseUrl), mapBody, String.class);

        Map<String, Object> mapData = (Map<String, Object>) GenUtil.fromJsonString(responseDataStr, Map.class).get("data");

        return GenUtil.objToStr(mapData.get("token"));
    }

    public String getAccountLoginToken(String baseUrl, String applyCode, Long mobile) {
        return getLoginToken(baseUrl, applyCode, mobile, "123123");
    }

    public String getAccountLoginToken(String baseUrl, String applyCode, Long mobile, String password) {
        Map<String, Object> mapBody = new HashMap<>();
        mapBody.put("applyCode", applyCode);
        mapBody.put("password", password);
        mapBody.put("mobile", mobile);

        String responseDataStr =
                ApiUtil.requestWithBodyDataByPostToEntity(String.format(ACCOUNT_LOGIN, baseUrl), mapBody, String.class);

        Map<String, Object> mapData = (Map<String, Object>) GenUtil.fromJsonString(responseDataStr, Map.class).get("data");

        return GenUtil.objToStr(mapData.get("token"));
    }

}
