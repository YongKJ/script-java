package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.util.GenUtil;

import java.util.Map;

public class TokenManagementService extends BaseService {

    private final boolean enable;

    public TokenManagementService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> tokenManagement = GenUtil.getMap("token-management");
        this.enable = GenUtil.objToBoolean(tokenManagement.get("enable"));
    }

    public void apply() {
        if (!enable) return;
    }

}
