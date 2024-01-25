package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.api.TokenApiService;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.Map;

public class TokenManagementService extends BaseService {

    private final boolean enable;
    private final String TEST_TOKEN;
    private final String PLATFORM_CODE;
    private final String SERVICE_CODE;
    private final String GOODS_CODE;
    private final String AGENCY_CODE;
    private final String HEALTH_CODE;

    private final String DEV_USER_BASE_URL;
    private final String DEV_ORDER_BASE_URL;
    private final String TEST_USER_BASE_URL;
    private final String TEST_ORDER_BASE_URL;
    private final String PRE_USER_BASE_URL;
    private final String PRE_ORDER_BASE_URL;
    private final String PROD_USER_BASE_URL;
    private final String PROD_ORDER_BASE_URL;
    private final String ADMIN_USER_BASE_URL;
    private final String ADMIN_ORDER_BASE_URL;

    private final TokenApiService tokenApiService;

    public TokenManagementService(DataMigration dataMigration) {
        super(dataMigration);
        this.tokenApiService = new TokenApiService();
        Map<String, Object> tokenManagement = GenUtil.getMap("token-management");
        Map<String, Object> mapToken = (Map<String, Object>) tokenManagement.get("code");
        Map<String, Object> mapBaseUrl = (Map<String, Object>) tokenManagement.get("base-url");

        this.enable = GenUtil.objToBoolean(tokenManagement.get("enable"));
        this.TEST_TOKEN = GenUtil.objToStr(tokenManagement.get("test-token"));
        this.PLATFORM_CODE = GenUtil.objToStr(mapToken.get("platform-code"));
        this.SERVICE_CODE = GenUtil.objToStr(mapToken.get("service-code"));
        this.GOODS_CODE = GenUtil.objToStr(mapToken.get("goods-code"));
        this.AGENCY_CODE = GenUtil.objToStr(mapToken.get("agency-code"));
        this.HEALTH_CODE = GenUtil.objToStr(mapToken.get("health-code"));

        this.DEV_USER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("dev-user-base-url"));
        this.DEV_ORDER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("dev-order-base-url"));
        this.TEST_USER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("test-user-base-url"));
        this.TEST_ORDER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("test-order-base-url"));
        this.PRE_USER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("pre-user-base-url"));
        this.PRE_ORDER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("pre-order-base-url"));
        this.PROD_USER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("prod-user-base-url"));
        this.PROD_ORDER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("prod-order-base-url"));
        this.ADMIN_USER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("admin-user-base-url"));
        this.ADMIN_ORDER_BASE_URL = GenUtil.objToStr(mapBaseUrl.get("admin-order-base-url"));
    }

    public void apply() {
        if (!enable) return;
        testTokenToAdminToken();
    }

    private void testTokenToAdminToken() {
        Table organizationTable = desDatabase.getMapTable().get("organization");
        Long organizationId = tokenApiService.getOrganizationIdByToken(TEST_USER_BASE_URL, TEST_TOKEN);
        Map<String, Map<String, Object>> mapOrganizationData = getMapData(
                desDataList(
                        Wrappers.lambdaQuery(organizationTable.getName())
                                .eq("id", organizationId)));
        Map<String, Object> organizationData = mapOrganizationData.get(organizationId + "");
        Long administratorId = (Long) organizationData.get("administrator_id");

        System.out.println("------------------------------------------------------------------------------------------------------");
        LogUtil.loggerLine(Log.of("TokenManagementService", "testTokenToAdminToken", "administratorId", administratorId));
        System.out.println("------------------------------------------------------------------------------------------------------");

        Table adminUserTable = desDatabase.getMapTable().get("admin_user");
        Map<String, Map<String, Object>> mapAdminUserData = getMapData(
                desDataList(
                        Wrappers.lambdaQuery(adminUserTable.getName())
                                .eq("id", administratorId)));
        Map<String, Object> adminUserData = mapAdminUserData.get(administratorId + "");
        Long mobile = (Long) adminUserData.get("mobile");

        System.out.println("------------------------------------------------------------------------------------------------------");
        LogUtil.loggerLine(Log.of("TokenManagementService", "testTokenToAdminToken", "mobile", mobile));
        System.out.println("------------------------------------------------------------------------------------------------------");

        String adminToken = tokenApiService.getLoginToken(ADMIN_USER_BASE_URL, PLATFORM_CODE, mobile);

        System.out.println("------------------------------------------------------------------------------------------------------");
        LogUtil.loggerLine(Log.of("TokenManagementService", "testTokenToAdminToken", "adminToken", adminToken));
        System.out.println("------------------------------------------------------------------------------------------------------");
    }
}
