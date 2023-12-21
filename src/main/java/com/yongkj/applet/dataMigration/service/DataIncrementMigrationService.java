package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataIncrementMigrationService extends BaseService {

    private final boolean enable;
    private final List<String> tableNames;

    public DataIncrementMigrationService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> mapDataMigration = GenUtil.getMap("data-migration");
        this.tableNames = (List<String>) mapDataMigration.get("table-names");
        this.enable = GenUtil.objToBoolean(mapDataMigration.get("enable"));
    }

    public void apply() {
        if (!enable) return;
        for (String tableName : tableNames) {
            compareAndMigrationData(tableName);
        }
        JDBCUtil.close(srcDatabase.getManager());
        JDBCUtil.close(desDatabase.getManager());
    }

    private void compareAndMigrationData(String tableName) {
        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        if (srcTable == null || desTable == null) {
            LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "compareAndMigrationData", "error", "srcTable or desTable not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        if (srcTable.getFieldNames().contains("id")) {
            compareAndMigrationDataById(desTable);
        } else {
            compareAndMigrationDataByMd5(srcTable, desTable);
        }
    }

    private void compareAndMigrationDataByMd5(Table srcTable, Table desTable) {
        Map<String, Map<String, Object>> srcTableData = getMapData(srcDataList(srcTable));
        Map<String, Map<String, Object>> desTableData = getMapData(desDataList(desTable));

        List<String> ids = getRetainIds(srcTableData, desTableData);
        for (String id : ids) {
            Map<String, Object> srcData = srcTableData.get(id);
            LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "compareAndMigrationDataByMd5", "srcData", srcData));
            insertDesData(desTable, srcData);
        }
        LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "compareAndMigrationDataByMd5", "ids.size()", ids.size()));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private void compareAndMigrationDataById(Table desTable) {
        String tableName = desTable.getName();
        Map<String, Map<String, Object>> desTableData = getMapData(desDataList(
                Wrappers.lambdaQuery(tableName).select("id")
        ));
        List<Long> lstId = desTableData.values().stream()
                .map(v -> GenUtil.objToLong(v.get("id"))).collect(Collectors.toList());
        Map<String, Map<String, Object>> srcTableData = lstId.isEmpty() ? new HashMap<>() : getMapData(srcDataList(
                Wrappers.lambdaQuery(tableName).notIn("id", lstId)
        ));
        List<String> ids = new ArrayList<>(srcTableData.keySet());
        for (String id : ids) {
            Map<String, Object> srcData = srcTableData.get(id);
            LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "compareAndMigrationDataById", "srcData", srcData));
            insertDesData(desTable, srcData);
        }
        LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "compareAndMigrationDataById", "ids.size()", ids.size()));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private void insertDesData(Table table, Map<String, Object> data) {
        if (table == null || data == null) {
            LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "insertDesData", "error", "table or data not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        String insertSql = getInsertSql(table, data);
        LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "insertDesData", "insertSql", insertSql));

        boolean sqlResult = desDataInsert(insertSql);
        LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "insertDesData", "sqlResult", sqlResult));
        LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "insertDesData", "success", "insert data success!"));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private String getInsertSql(Table table, Map<String, Object> data) {
        return SQL.getDataInsertSql(
                table.getName(),
                table.getFieldNames(),
                getDataValue(table.getFieldNames(), data)
        );
    }

    private List<String> getDataValue(List<String> lstFieldName, Map<String, Object> mapData) {
        List<String> lstData = new ArrayList<>();
        for (String fieldName : lstFieldName) {
            Object value = mapData.get(fieldName);
            if (value instanceof String) {
                lstData.add(String.format("'%s'", value));
            } else {
                lstData.add(value.toString());
            }
        }
        return lstData;
    }

    public List<Map<String, Object>> setDataSelectTestThree() {
        return desSetDataList(
                Wrappers.lambdaQuery(
                        "comment_detail d"
                )
                        .eq("d.utc_deleted", 0)
                        .eq("d.apply_id", 2)
                        .select(
                                "d.user_id",
                                "d.user_name",
                                "CAST(CONCAT('[', GROUP_CONCAT(d.id), ']') AS JSON) ids",
                                "CAST(CONCAT('[', GROUP_CONCAT(d.item_id), ']') AS JSON) item_ids",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(d.item_name SEPARATOR '\",\"'), '\"]') AS JSON) item_names",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(d.item_sub_name SEPARATOR '\",\"'), '\"]') AS JSON) item_sub_names",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(d.item_thumb SEPARATOR '\",\"'), '\"]') AS JSON) item_thumbs",
                                "CAST(CONCAT('[', GROUP_CONCAT(d.order_entity_id), ']') AS JSON) order_entity_ids",
                                "CAST(CONCAT('[', GROUP_CONCAT(d.shop_id), ']') AS JSON) shop_ids",
                                "CAST(CONCAT('[', GROUP_CONCAT(d.price), ']') AS JSON) prices",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(d.comment SEPARATOR '\",\"'), '\"]') AS JSON) comments",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(d.reply SEPARATOR '\",\"'), '\"]') AS JSON) replies",
                                "CAST(CONCAT('[', GROUP_CONCAT(d.type_id), ']') AS JSON) type_ids",
                                "CAST(CONCAT('[\"', REPLACE(GROUP_CONCAT(d.images SEPARATOR '\",\"'), '\"', '\\\\\"'), '\"]') AS JSON) images_list",
                                "CAST(CONCAT('[', GROUP_CONCAT(d.utc_created), ']') AS JSON) utc_createds",
                                "d.apply_id",
                                "d.order_serial_no",
                                "d.order_id",
                                "d.order_time"
                        )
        );
    }

    public List<Map<String, Object>> setDataSelectTestTwo() {
        return desSetDataList(
                Wrappers.lambdaQuery(
                        "`comment_detail` `d`"
                )
                        .eq("`d`.`utc_deleted`", 0)
                        .eq("`d`.`apply_id`", 2)
                        .select(
                                "`d`.`user_id`",
                                "`d`.`user_name`",
                                "CAST(CONCAT('[', GROUP_CONCAT(`d`.`id`), ']') AS JSON) `ids`",
                                "CAST(CONCAT('[', GROUP_CONCAT(`d`.`item_id`), ']') AS JSON) `item_ids`",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(`d`.`item_name` SEPARATOR '\",\"'), '\"]') AS JSON) `item_names`",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(`d`.`item_sub_name` SEPARATOR '\",\"'), '\"]') AS JSON) `item_sub_names`",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(`d`.`item_thumb` SEPARATOR '\",\"'), '\"]') AS JSON) `item_thumbs`",
                                "CAST(CONCAT('[', GROUP_CONCAT(`d`.`order_entity_id`), ']') AS JSON) `order_entity_ids`",
                                "CAST(CONCAT('[', GROUP_CONCAT(`d`.`shop_id`), ']') AS JSON) `shop_ids`",
                                "CAST(CONCAT('[', GROUP_CONCAT(`d`.`price`), ']') AS JSON) `prices`",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(`d`.`comment` SEPARATOR '\",\"'), '\"]') AS JSON) `comments`",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(`d`.`reply` SEPARATOR '\",\"'), '\"]') AS JSON) `replies`",
                                "CAST(CONCAT('[', GROUP_CONCAT(`d`.`type_id`), ']') AS JSON) `type_ids`",
                                "CAST(CONCAT('[\"', REPLACE(GROUP_CONCAT(`d`.`images` SEPARATOR '\",\"'), '\"', '\\\\\"'), '\"]') AS JSON) `images_list`",
                                "CAST(CONCAT('[', GROUP_CONCAT(`d`.`utc_created`), ']') AS JSON) `utc_createds`",
                                "`d`.`apply_id`",
                                "`d`.`order_serial_no`",
                                "`d`.`order_id`",
                                "`d`.`order_time`"
                        )
        );
    }

    public List<Map<String, Object>> setDataSelectTestOne() {
        return desSetDataList(
                Wrappers.lambdaQuery(
                        "organization_worker ow",
                        "shop s",
                        "worker w",
                        "rel_worker__worker_type rwt",
                        "worker_type wt"
                )
                        .eq("ow.worker_id", "w.id")
                        .eq("ow.organization_id", "s.organization_id")
                        .eq("ow.worker_id", "rwt.worker_id")
                        .eq("ow.organization_id", "rwt.organization_id")
                        .eq("rwt.type_id", "wt.id")
                        .groupBy("ow.worker_id", "ow.organization_id")
                        .orderByDesc("ow.utc_created", "ow.utc_modified")
                        .select(
                                "w.id",
                                "w.name",
                                "s.organization_id organization_id",
                                "s.name shop_name",
                                "CAST(CONCAT('[', GROUP_CONCAT(wt.id), ']') AS JSON) worker_types",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(wt.name SEPARATOR '\",\"'), '\"]') AS JSON) worker_names",
                                "w.mobile",
                                "ow.status",
                                "ow.utc_modified"
                        )
        );
    }

    public List<Map<String, Object>> setDataSelectTest() {
        return desSetDataList(
                Wrappers.lambdaQuery(
                        "`organization_worker` `ow`",
                        "`shop` `s`",
                        "`worker` `w`",
                        "`rel_worker__worker_type` `rwt`",
                        "`worker_type` `wt`"
                )
                        .eq("`ow`.`worker_id`", "`w`.`id`")
                        .eq("`ow`.`organization_id`", "`s`.`organization_id`")
                        .eq("`ow`.`worker_id`", "`rwt`.`worker_id`")
                        .eq("`ow`.`organization_id`", "`rwt`.`organization_id`")
                        .eq("`rwt`.`type_id`", "`wt`.`id`")
                        .groupBy("`ow`.`worker_id`", "`ow`.`organization_id`")
                        .orderByDesc("`ow`.`utc_created`", "`ow`.`utc_modified`")
                        .select(
                                "`w`.`id`",
                                "`w`.`name`",
                                "`s`.`organization_id` `organization_id`",
                                "`s`.`name` `shop_name`",
                                "CAST(CONCAT('[', GROUP_CONCAT(`wt`.`id`), ']') AS JSON) `worker_types`",
                                "CAST(CONCAT('[\"', GROUP_CONCAT(`wt`.`name` SEPARATOR '\",\"'), '\"]') AS JSON) `worker_names`",
                                "`w`.`mobile`",
                                "`ow`.`status`",
                                "`ow`.`utc_modified`"
                        )
        );
    }

    public List<Map<String, Object>> dataSelectTest(String keyword, String level) {
        return desDataList(Wrappers.lambdaQuery("amap_district")
                .eq("level", level)
                .and(w -> w
                        .eq("id", keyword)
                        .or()
                        .like("name", keyword)));
    }

}
