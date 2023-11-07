package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShopCancelLogoutService extends BaseService {

    private final boolean enable;
    private final List<Long> mobiles;

    public ShopCancelLogoutService(Database srcDatabase, Database desDatabase) {
        super(srcDatabase, desDatabase);
        Map<String, Object> shopCancelLogout = GenUtil.getMap("shop-cancel-logout");
        this.mobiles = (List<Long>) shopCancelLogout.get("mobiles");
        this.enable = (Boolean) shopCancelLogout.get("enable");
    }

    public void apply() {
        if (!enable) return;
        for (Long mobile : mobiles) {
            shopCancelLogout(mobile);
        }
        LogUtil.loggerLine(Log.of("ShopCancelLogoutService", "apply", "mobiles", mobiles));
    }

    private void shopCancelLogout(Long mobile) {
        Map<String, Object> adminUser = desDataList(
                Wrappers.lambdaQuery("admin_user")
                        .eq("mobile", mobile)).get(0);
        Integer status = (Integer) adminUser.get("status");
        Long adminUserId = (Long) adminUser.get("id");
        if (!Objects.equals(status, 0L)) {
            adminUser.put("status", 0L);
            desDataUpdate(SQL.getDataUpdateSql(
                    "admin_user", adminUser,
                    Wrappers.lambdaQuery()
                            .eq("id", adminUserId).getSqlSegment()));
        }

        List<Map<String, Object>> adminRoles = desDataList(
                Wrappers.lambdaQuery("admin_role_users")
                        .eq("user_id", adminUserId));
        for (Map<String, Object> adminRole : adminRoles) {
            Long utcDeleted = (Long) adminRole.get("utc_deleted");
            if (Objects.equals(utcDeleted, 0L)) continue;
            adminRole.put("utc_deleted", 0L);
            Long userId = (Long) adminRole.get("user_id");
            Long roleId = (Long) adminRole.get("role_id");
            Long utcCreated = (Long) adminRole.get("utc_created");
            desDataUpdate(SQL.getDataUpdateSql(
                    "admin_role_users", adminRole,
                    Wrappers.lambdaQuery()
                            .eq("user_id", userId)
                            .eq("role_id", roleId)
                            .eq("utc_created", utcCreated).getSqlSegment()));
        }

        Long organizationId = (Long) adminUser.get("organization_id");
        Map<String, Object> organization = desDataList(
                Wrappers.lambdaQuery("organization")
                        .eq("id", organizationId)).get(0);
        Long utcDeleted = (Long) organization.get("utc_deleted");
        if (!Objects.equals(utcDeleted, 0L)) {
            organization.put("utc_deleted", 0L);
            desDataUpdate(SQL.getDataUpdateSql(
                    "organization", organization,
                    Wrappers.lambdaQuery()
                            .eq("id", organizationId).getSqlSegment()));
        }

        Map<String, Object> shop = desDataList(
                Wrappers.lambdaQuery("shop")
                        .eq("organization_id", organizationId)).get(0);
        utcDeleted = (Long) shop.get("utc_deleted");
        if (!Objects.equals(utcDeleted, 0L)) {
            shop.put("utc_deleted", 0L);
            Long shopId = (Long) shop.get("id");
            desDataUpdate(SQL.getDataUpdateSql(
                    "shop", shop,
                    Wrappers.lambdaQuery()
                            .eq("id", shopId).getSqlSegment()));
        }

        LogUtil.loggerLine(Log.of("ShopCancelLogoutService", "shopCancelLogout", "adminUser", adminUser));
        LogUtil.loggerLine(Log.of("ShopCancelLogoutService", "shopCancelLogout", "adminRoles", adminRoles));
        LogUtil.loggerLine(Log.of("ShopCancelLogoutService", "shopCancelLogout", "organization", organization));
        LogUtil.loggerLine(Log.of("ShopCancelLogoutService", "shopCancelLogout", "shop", shop));
        System.out.println("------------------------------------------------------------------------------------------------------------------");
    }

}
