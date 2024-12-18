package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import com.yongkj.util.PoiExcelUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SmallAssignmentUpdateService extends BaseService {

    private final boolean enable;

    public SmallAssignmentUpdateService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> smallAssignmentUpdate = GenUtil.getMap("small-assignment-update");
        this.enable = GenUtil.objToBoolean(smallAssignmentUpdate.get("enable"));
    }

    public void apply() {
        if (!enable) return;
//        categoryModifierIdUpdate();
//        relWorkerTypeLogExport();
//        msgTemplateImport();
//        jsonFieldTest();
//        contactPersonUpdate();
//        syncShopApplyId();
//        syncShopOrganizationStatus();
//        syncRoleMenuData();
//        diffRoleMenuData();
//        organizationInfoUpdate();
//        devApplyMenuDataFix();
//        apiExport();
//        updatePlatformKind();
//        updateProdMenuPermissions();
//        exportWorkerIds();
//        exportWorkerShopInfo();
//        exportUtcCreatedData();
        exportConsumerUtcCreatedData();
//        exportJobRecallContent();
//        getMapDistrictTypeId();
    }

    private void getMapDistrictTypeId() {
        Table table = testDatabase.getMapTable().get("amap_district");
        List<Map<String, Object>> provinceData = srcDataList(
                Wrappers.lambdaQuery(table)
                        .eq("parent_id", 0));
        System.out.println("provinceData.size(): " + provinceData.size());

        List<Integer> provinceIds = provinceData.stream().map(po -> (Integer) po.get("id")).collect(Collectors.toList());
        List<Map<String, Object>> cityData = srcDataList(
                Wrappers.lambdaQuery(table)
                        .in("parent_id", provinceIds));
        System.out.println("cityData.size(): " + cityData.size());

        List<Integer> cityIds = cityData.stream().map(po -> (Integer) po.get("id")).collect(Collectors.toList());
        List<Map<String, Object>> districtData = srcDataList(
                Wrappers.lambdaQuery(table)
                        .in("parent_id", cityIds));
        System.out.println("districtData.size(): " + districtData.size());

        provinceData.addAll(cityData);
        provinceData.addAll(districtData);
        List<Map<String, Object>> lstData = new ArrayList<>();
        for (Map<String, Object> mapData : provinceData) {
            Integer id = (Integer) mapData.get("id");
            String name = (String) mapData.get("name");
            String level = (String) mapData.get("level");

            Map<String, Object> tempMapData = new HashMap<>();
            tempMapData.put("id", id);
            tempMapData.put("name", name);
            tempMapData.put("level", level);
            lstData.add(tempMapData);
        }

        FileUtil.write("C:\\Users\\Admin\\Desktop\\marketing-screen-district-type-id.json", GenUtil.toJsonString(lstData));
    }

    private void exportJobRecallContent() {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("回访内容分词");
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);

        List<List<String>> lstHeader = Arrays.asList(
                Collections.singletonList("序号"),
                Collections.singletonList("分词列表(逗号分隔)"),
                Collections.singletonList("工单回访内容")
        );
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);

        int rowIndex = 1;
        List<Map<String, Object>> jobRecallData = getJobRecallContent();
        for (Map<String, Object> mapData : jobRecallData) {
            String jobRecallContent = (String) mapData.get("recall_content");
            if (!StringUtils.hasText(jobRecallContent)) {
                continue;
            }
            if (!(jobRecallContent.contains("探") || jobRecallContent.contains("访"))) {
                continue;
            }

            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 0, rowIndex);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 1, "");
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex++, 2, jobRecallContent);
        }

        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\工单回访内容分词-" + System.currentTimeMillis() + ".xlsx");
    }

    private List<Map<String, Object>> getJobRecallContent() {
//        Table jobRecall = devDatabase.getMapTable().get("job_recall");
//        List<Map<String, Object>> jobRecallData = srcDataList(devDatabase, jobRecall);
//
//        jobRecall = testDatabase.getMapTable().get("job_recall");
//        jobRecallData.addAll(srcDataList(testDatabase, jobRecall));
//
//        jobRecall = preDatabase.getMapTable().get("job_recall");
//        jobRecallData.addAll(srcDataList(preDatabase, jobRecall));
//
//        jobRecall = prodDatabase.getMapTable().get("job_recall");
//        jobRecallData.addAll(srcDataList(prodDatabase, jobRecall));

        Table jobRecall = preDatabase.getMapTable().get("job_recall");
        List<Map<String, Object>> jobRecallData = srcDataList(preDatabase, jobRecall);

        jobRecall = prodDatabase.getMapTable().get("job_recall");
        jobRecallData.addAll(srcDataList(prodDatabase, jobRecall));

        return jobRecallData;
    }

    private void exportConsumerUtcCreatedData() {
        Table consumer = prodDatabase.getMapTable().get("consumer");

        List<Map<String, Object>> consumerData = srcDataList(preDatabase, consumer);

        Map<String, String> mapUtcCreatedData = new ConcurrentSkipListMap<>();
        for (Map<String, Object> mapData : consumerData) {
            Long utcCreated = (Long) mapData.get("utc_created");
            String utcCreatedStr = GenUtil.timestampToStr(utcCreated);
            mapUtcCreatedData.put(utcCreatedStr, utcCreatedStr);
        }

        FileUtil.write(
                "C:\\Users\\Admin\\Desktop\\utc-created-data.json",
                GenUtil.toJsonString(mapUtcCreatedData)
        );
    }

    private void exportUtcCreatedData() {
        Table adminUser = preDatabase.getMapTable().get("admin_user");
        Table organization = preDatabase.getMapTable().get("organization");

        List<Map<String, Object>> adminUserData = srcDataList(preDatabase, adminUser);
        List<Map<String, Object>> organizationData = srcDataList(preDatabase, organization);

        Map<String, String> mapUtcCreatedData = new ConcurrentSkipListMap<>();
        for (Map<String, Object> mapData : adminUserData) {
            Long utcCreated = (Long) mapData.get("utc_created");
            String utcCreatedStr = GenUtil.timestampToStr(utcCreated);
            mapUtcCreatedData.put(utcCreatedStr, utcCreatedStr);
        }

        for (Map<String, Object> mapData : organizationData) {
            Long utcCreated = (Long) mapData.get("utc_created");
            String utcCreatedStr = GenUtil.timestampToStr(utcCreated);
            mapUtcCreatedData.put(utcCreatedStr, utcCreatedStr);
        }

        FileUtil.write(
                "C:\\Users\\Admin\\Desktop\\utc-created-data.json",
                GenUtil.toJsonString(mapUtcCreatedData)
        );
    }

    private void exportWorkerShopInfo() {
        Table organizationWorker = testDatabase.getMapTable().get("organization_worker");
        Table shop = testDatabase.getMapTable().get("shop");

        List<Map<String, Object>> organizationWorkerData = srcDataList(testDatabase,
                Wrappers.lambdaQuery(organizationWorker)
                        .select("worker_id", "organization_id"));

        List<Map<String, Object>> shopData = srcDataList(testDatabase,
                Wrappers.lambdaQuery(shop)
                        .select("id", "organization_id", "name"));

        List<Map<String, Object>> lstData = new ArrayList<>();
        for (Map<String, Object> mapOrganizationWorker : organizationWorkerData) {
            Long workerId = (Long) mapOrganizationWorker.get("worker_id");
            Long organizationId = (Long) mapOrganizationWorker.get("organization_id");
            if (workerId == 0) {
                continue;
            }

            Map<String, Object> mapShop = shopData.stream().filter(po -> Objects.equals(po.get("organization_id"), organizationId)).findFirst().orElse(new HashMap<>());
            if (mapShop.isEmpty()) {
                continue;
            }

            Long shopId = (Long) mapShop.get("id");
            String shopName = (String) mapShop.get("name");
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("worker_id", workerId);
            mapData.put("shop_id", shopId);
            mapData.put("shop_name", shopName);
            lstData.add(mapData);
        }

        FileUtil.write(
                "C:\\Users\\Admin\\Desktop\\worker-shop-info.json",
                GenUtil.toJsonString(lstData)
        );
    }

    private void exportWorkerIds() {
        Table workerTable = preDatabase.getMapTable().get("worker");
        Map<String, Map<String, Object>> workerTableData = getMapData(srcDataList(preDatabase,
                Wrappers.lambdaQuery(workerTable)
                        .select("id", "name")));

        List<Map<String, Object>> lstData = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : workerTableData.entrySet()) {
            lstData.add(map.getValue());
        }

        FileUtil.write(
                "C:\\Users\\Admin\\Desktop\\worker-ids.json",
                GenUtil.toJsonString(lstData)
        );
    }

//    private void apiExport() {
//        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\白名单接口.txt");
//        String lineBreak = content.contains("\r\n") ? "\r\n" : "\n";
//        List<String> lstLine = Arrays.asList(content.split(lineBreak));
//        List<List<String>> lstHeader = Arrays.asList(
//                Collections.singletonList("序号"),
//                Collections.singletonList("白名单接口"),
//                Collections.singletonList("备注"),
//                Collections.singletonList("状态")
//        );
//        SXSSFWorkbook workbook = new SXSSFWorkbook();
//        SXSSFSheet sheet = workbook.createSheet();
//        PoiExcelUtil.writeHeader(sheet, lstHeader, 1);
//
//        Integer rowIndex = lstHeader.get(0).size();
//        for (int i = 0; i < lstLine.size(); i++) {
//            String line = lstLine.get(i);
//            List<String> lstLineData = Collections.singletonList(line);
//            if (line.contains("#")) {
//                lstLineData = Arrays.asList(line.split("#"));
//            }
//
//            int num = i + 1;
//            String api = lstLineData.get(0);
//            String mark
//        }
//    }

    private void devApplyMenuDataFix() {
        Database devUser = mapDatabase.get("dev_user");
        Table applyMenuTable = devUser.getMapTable().get("admin_apply_menu");
        List<Map<String, Object>> lstApplyMenuTableData = desDataList(devUser, applyMenuTable);
        Map<String, Map<String, Object>> applyMenuTableData = lstApplyMenuTableData.stream()
                .filter(po -> Objects.equals(po.get("apply_id"), 1L)).collect(Collectors.toMap(po -> po.get("menu_id") + "", Function.identity()));

        Table menuTable = devUser.getMapTable().get("admin_menu");
        Map<String, Map<String, Object>> menuTableData = getMapData(desDataList(devUser, menuTable));
        for (Map.Entry<String, Map<String, Object>> map : menuTableData.entrySet()) {
            if (applyMenuTableData.containsKey(map.getKey())) {
                continue;
            }

            Long menuId = (Long) map.getValue().get("id");
            Map<String, Object> tempMapData = new HashMap<>();
            tempMapData.put("utc_created", System.currentTimeMillis());
            tempMapData.put("menu_id", menuId);
            tempMapData.put("apply_id", 1);

            String insertSql = getInsertSQl(tempMapData, applyMenuTable);

            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "devApplyMenuDataFix", "insertSql", insertSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            desDataInsert(devUser, insertSql);
        }
    }

    private void organizationInfoUpdate() {
        Long organizationId = 1730477844633948162L;
        Map<String, Object> organizationDemo = getOrganizationDemo(organizationId);
        if (organizationDemo == null) {
            return;
        }
        Map<String, Object> idCardDemo = getIdCardDemo(organizationDemo);
        if (idCardDemo == null) {
            return;
        }

        Table idCardTable = desDatabase.getMapTable().get("id_card");
        Table organizationTable = desDatabase.getMapTable().get("organization");

        List<Map<String, Object>> lstOrganizationData = desDataList(
                Wrappers.lambdaQuery(organizationTable)
                        .eq("utc_deleted", 0)
                        .eq("apply_id", 2));

        List<Long> idCardIds = lstOrganizationData.stream()
                .map(po -> (Long) po.get("legal_person_id_card_id")).collect(Collectors.toList());
        Map<String, Map<String, Object>> mapIdCardData = getMapData(desDataList(
                Wrappers.lambdaQuery(idCardTable)
                        .in("id", idCardIds)));

        Map<String, Map<String, Object>> mapOrganizationData = getMapData(lstOrganizationData);
        for (Map.Entry<String, Map<String, Object>> map : mapOrganizationData.entrySet()) {
            Map<String, Object> organization = map.getValue();
            organization.put("code", organizationDemo.get("code"));
            organization.put("name", organizationDemo.get("name"));
            organization.put("address", organizationDemo.get("address"));
            organization.put("business_license", organizationDemo.get("business_license"));
            organization.put("period_of_validity_start", organizationDemo.get("period_of_validity_start"));
            organization.put("period_of_validity_end", organizationDemo.get("period_of_validity_end"));
            organization.put("period_type", organizationDemo.get("period_type"));

            Long id = (Long) organization.get("id");
            String updateSql = getUpdateSQl(organization,
                    Wrappers.lambdaQuery(organizationTable)
                            .eq("id", id));
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "organizationInfoUpdate", "updateSql", updateSql));
            desDataUpdate(updateSql);

            Long legalPersonIdCardId = (Long) organization.get("legal_person_id_card_id");
            Map<String, Object> idCard = mapIdCardData.get(legalPersonIdCardId + "");
            if (idCard == null) {
                continue;
            }

            idCard.put("positive_img", idCardDemo.get("positive_img"));
            idCard.put("negative_img", idCardDemo.get("negative_img"));
            idCard.put("name", idCardDemo.get("name"));
            idCard.put("number", idCardDemo.get("number"));
            idCard.put("start_time", idCardDemo.get("start_time"));
            idCard.put("end_time", idCardDemo.get("end_time"));
            idCard.put("validity", idCardDemo.get("validity"));

            id = (Long) idCard.get("id");
            String idCardUpdateSql = getUpdateSQl(idCard,
                    Wrappers.lambdaQuery(idCardTable)
                            .eq("id", id));
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "organizationInfoUpdate", "idCardUpdateSql", idCardUpdateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            desDataUpdate(idCardUpdateSql);
        }
    }

    private Map<String, Object> getIdCardDemo(Map<String, Object> organization) {
        Table idCardTable = srcDatabase.getMapTable().get("id_card");
        Long legalPersonIdCardId = (Long) organization.get("legal_person_id_card_id");
        List<Map<String, Object>> lstIdCardData = srcDataList(
                Wrappers.lambdaQuery(idCardTable)
                        .eq("id", legalPersonIdCardId));
        return lstIdCardData.isEmpty() ? null : lstIdCardData.get(0);
    }

    private Map<String, Object> getOrganizationDemo(Long organizationId) {
        Table organizationTable = srcDatabase.getMapTable().get("organization");
        List<Map<String, Object>> lstOrganizationData = srcDataList(
                Wrappers.lambdaQuery(organizationTable)
                        .eq("apply_id", 2)
                        .eq("id", organizationId));
        return lstOrganizationData.isEmpty() ? null : lstOrganizationData.get(0);
    }

    private void diffRoleMenuData() {
//        diffRoleMenuData("admin_roles", "admin_roles_4");
//        diffRoleMenuData("admin_menu", "admin_menu_4");
//        diffRoleMenuData("admin_role_menu", "admin_role_menu_4");
//        diffRoleMenuData("admin_apply_menu", "admin_apply_menu_4");

//        diffRoleMenuData("admin_roles", "admin_roles");
//        diffRoleMenuData("admin_apply", "admin_apply");
//        diffRoleMenuData("admin_role_menu", "admin_role_menu");

        Database devUser = mapDatabase.get("dev_user");
        Database testUser = mapDatabase.get("test_user");
        Database preUser = mapDatabase.get("pre_user");
        Database prodUser = mapDatabase.get("prod_user");

//        distinctRoleMenuData(devUser, "admin_apply_menu", "apply_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(testUser, "admin_apply_menu", "apply_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(preUser, "admin_apply_menu", "apply_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(prodUser, "admin_apply_menu", "apply_id", "menu_id");
//        System.out.println("==================================================================================================================\n");

//        distinctRoleMenuData(devUser, "admin_menu", "pid", "name", "alias_name", "type", "sort", "apply_kind", "platform_kind", "apply_id", "route", "route_param");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(testUser, "admin_menu", "pid", "name", "alias_name", "type", "sort", "apply_kind", "platform_kind", "apply_id", "route", "route_param");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(preUser, "admin_menu", "pid", "name", "alias_name", "type", "sort", "apply_kind", "platform_kind", "apply_id", "route", "route_param");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(prodUser, "admin_menu", "pid", "name", "alias_name", "type", "sort", "apply_kind", "platform_kind", "apply_id", "route", "route_param");
//        System.out.println("==================================================================================================================\n");

        diffRoleMenuData(devUser, testUser, "admin_menu", "id");
        diffRoleMenuData(devUser, testUser, "admin_apply_menu", "apply_id", "menu_id");
        System.out.println("==================================================================================================================\n");

//        diffRoleMenuData(testUser, preUser, "admin_menu", "id");
//        diffRoleMenuData(testUser, preUser, "admin_apply_menu", "apply_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//
//        diffRoleMenuData(preUser, prodUser, "admin_menu", "id");
//        diffRoleMenuData(preUser, prodUser, "admin_apply_menu", "apply_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
    }

    private void distinctRoleMenuData(Database database, String desTableName, String... fields) {
        Table desTable = database.getMapTable().get(desTableName);
        List<Map<String, Object>> lstData = desDataList(database, desTable);
        Map<String, Map<String, Object>> mapData = new HashMap<>();
        for (Map<String, Object> data : lstData) {
            String key = getMd5Key(data, Arrays.asList(fields));
            if (!mapData.containsKey(key)) {
                mapData.put(key, data);
                continue;
            }

            String deleteSql = getRemoveSQl(data, desTableName);
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "distinctRoleMenuData", "deleteSql", deleteSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            desDataRemove(database, deleteSql);
        }
    }

    private void diffRoleMenuData(Database srcDatabase, Database desDatabase, String tableName, String... fields) {
        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        List<Map<String, Object>> srcTableData = srcDataList(srcDatabase, srcTable);
        List<Map<String, Object>> desTableData = desDataList(desDatabase, desTable);
        Map<String, Map<String, Object>> srcMapTableData = getMapData(srcTableData, Arrays.asList(fields));
        Map<String, Map<String, Object>> desMapTableData = getMapData(desTableData, Arrays.asList(fields));

//        if (Objects.equals(tableName, "admin_apply_menu")) {
//            for (Map.Entry<String, Map<String, Object>> map : desMapTableData.entrySet()) {
//                if (srcMapTableData.containsKey(map.getKey())) {
//                    continue;
//                }
//
//                String removeSql = getRemoveSQl(
//                        Wrappers.lambdaQuery(desTable)
//                                .eq("menu_id", map.getValue().get("menu_id"))
//                                .eq("apply_id", map.getValue().get("apply_id")));
//
//                LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "diffRoleMenuData", "removeSql", removeSql));
//                System.out.println("------------------------------------------------------------------------------------------------------------------");
//
//                    desDataRemove(desDatabase, removeSql);
//            }
//        }

        List<String> lstKey = new ArrayList<>();
        for (Map.Entry<String, Object> map : srcTableData.get(0).entrySet()) {
//            if (map.getKey().contains("_kind") ||
//                    map.getKey().contains("_modified")) {
//                continue;
//            }
            lstKey.add(map.getKey());
        }

        for (Map.Entry<String, Map<String, Object>> map : srcMapTableData.entrySet()) {
            if (desMapTableData.containsKey(map.getKey())) {
                if (Objects.equals(tableName, "admin_menu")) {
                    String srcMd5Key = getMd5Key(map.getValue(), lstKey);
                    String desMd5Key = getMd5Key(desMapTableData.get(map.getKey()), lstKey);
                    if (Objects.equals(srcMd5Key, desMd5Key)) {
                        continue;
                    }

                    Map<String, Object> tempMapData = new HashMap<>();
                    for (Map.Entry<String, Object> tempMap : map.getValue().entrySet()) {
//                        if (tempMap.getKey().contains("_kind") ||
//                                tempMap.getKey().contains("_modified")) {
//                            continue;
//                        }
                        tempMapData.put(tempMap.getKey(), tempMap.getValue());
                    }

                    String updateSql = getUpdateSQl(tempMapData,
                            Wrappers.lambdaQuery(desTable)
                                    .eq("id", map.getValue().get("id")));

                    LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "diffRoleMenuData", "updateSql", updateSql));
                    System.out.println("------------------------------------------------------------------------------------------------------------------");

                    desDataUpdate(desDatabase, updateSql);
                }
                continue;
            }

            Map<String, Object> tempMapData = new HashMap<>();
            for (Map.Entry<String, Object> tempMap : map.getValue().entrySet()) {
//                if (tempMap.getKey().contains("_kind") ||
//                        tempMap.getKey().contains("_modified")) {
//                    continue;
//                }
                tempMapData.put(tempMap.getKey(), tempMap.getValue());
            }
            String insertSql = getInsertSQl(tempMapData, desTable);

            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "diffRoleMenuData", "insertSql", insertSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            desDataInsert(desDatabase, insertSql);
        }
    }

    private void updatePlatformKind() {
        Map<Integer, Integer> mapOldApply = new HashMap<>();
        mapOldApply.put(3, 3);
        mapOldApply.put(13, 13);
        mapOldApply.put(18, 3);
        mapOldApply.put(19, 13);

        Map<Integer, Integer> mapApply = new HashMap<>();
        mapApply.put(3, 18);
        mapApply.put(13, 19);
        mapApply.put(18, 18);
        mapApply.put(19, 19);

        Map<Integer, String> mapApplyCode = new HashMap<>();
        mapApplyCode.put(18, "ly.goods.shop");
        mapApplyCode.put(19, "ly.health.shop");

        List<Map<String, String>> lstData = PoiExcelUtil.toMap("C:\\Users\\admin\\Desktop\\店铺手机号.xlsx");
        List<Long> lstMobile = lstData.stream().map(po -> GenUtil.objToLong(po.get("mobile"))).collect(Collectors.toList());

        Database testUser = mapDatabase.get("test_user");
        Table shopTable = testUser.getMapTable().get("shop");
        Table adminUserTable = testUser.getMapTable().get("admin_user");
        Table organizationTable = testUser.getMapTable().get("organization");
        Table shopBusinessCardTable = testUser.getMapTable().get("shop_business_card");
        Table adminApplyOrganizationTable = testUser.getMapTable().get("admin_apply_organization");
        Map<String, Map<String, Object>> mapShop = getMapData(srcDataList(testUser, shopTable));
        Map<String, Map<String, Object>> mapAdminUser = getMapData(srcDataList(testUser, adminUserTable));
        Map<String, Map<String, Object>> mapOrganization = getMapData(srcDataList(testUser, organizationTable));
        Map<String, Map<String, Object>> mapShopBusinessCard = getMapData(srcDataList(testUser, shopBusinessCardTable));
        Map<String, Map<String, Object>> mapAdminApplyOrganization = getMapData(srcDataList(testUser, adminApplyOrganizationTable));

        List<Long> lstAdminUserId = mapAdminUser.values().stream()
                .filter(po -> lstMobile.contains((Long) po.get("mobile")))
                .map(po -> (Long) po.get("id")).collect(Collectors.toList());

        for (Long adminUserId : lstAdminUserId) {
            Map<String, Object> adminUser = mapAdminUser.get(adminUserId.toString());
            if (adminUser == null) {
                continue;
            }
            Integer oldApplyId = mapOldApply.get((Integer) adminUser.get("apply_id"));
            Integer applyId = mapApply.get(oldApplyId);
            String applyCode = mapApplyCode.get(applyId);

            adminUser.put("platform_kind", 2);
            adminUser.put("apply_id", applyId);
            adminUser.put("apply_code", applyCode);
            adminUser.put("apply_code_id", applyId);
            Long organizationId = (Long) adminUser.get("organization_id");
            String adminUserSql = getUpdateSQl(adminUser,
                    Wrappers.lambdaQuery(adminUserTable)
                            .eq("id", adminUser.get("id")));
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "updatePlatformKind", "adminUserSql", adminUserSql));
            desDataUpdate(testUser, adminUserSql);

            Map<String, Object> organization = mapOrganization.get(organizationId.toString());
            if (organization == null) {
                System.out.println("------------------------------------------------------------------------------------------------------------------");
                continue;
            }

            organization.put("platform_kind", 2);
            organization.put("apply_id", applyId);
            organization.put("apply_code", applyCode);
            organization.put("apply_code_id", applyId);
            String organizationSql = getUpdateSQl(organization,
                    Wrappers.lambdaQuery(organizationTable)
                            .eq("id", organization.get("id")));
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "updatePlatformKind", "organizationSql", organizationSql));
            desDataUpdate(testUser, organizationSql);

            Map<String, Object> shop = mapShop.values().stream().filter(po -> Objects.equals(po.get("organization_id"), organizationId)).findFirst().orElse(null);
            if (shop == null) {
                System.out.println("------------------------------------------------------------------------------------------------------------------");
                continue;
            }

            shop.put("platform_kind", 2);
            shop.put("apply_id", applyId);
            shop.put("apply_code", applyCode);
            shop.put("apply_code_id", applyId);
            Long shopId = (Long) shop.get("id");
            String shopSql = getUpdateSQl(shop,
                    Wrappers.lambdaQuery(shopTable)
                            .eq("id", shop.get("id")));
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "updatePlatformKind", "shopSql", shopSql));
            desDataUpdate(testUser, shopSql);

            Map<String, Object> adminApplyOrganization = mapAdminApplyOrganization.values().stream()
                    .filter(po -> Objects.equals(po.get("apply_id"), oldApplyId) &&
                            Objects.equals(po.get("organization_id"), organization.get("id"))).findFirst().orElse(null);
            if (adminApplyOrganization != null) {
                adminApplyOrganization.put("apply_id", applyId);
                String adminApplyOrganizationSql = getUpdateSQl(adminApplyOrganization,
                        Wrappers.lambdaQuery(adminApplyOrganizationTable)
                                .eq("id", adminApplyOrganization.get("id")));
                LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "updatePlatformKind", "adminApplyOrganizationSql", adminApplyOrganizationSql));
                desDataUpdate(testUser, adminApplyOrganizationSql);
            }


            Map<String, Object> shopBusinessCard = mapShopBusinessCard.values().stream().filter(po -> Objects.equals(po.get("shop_id"), shopId)).findFirst().orElse(null);
            if (shopBusinessCard == null) {
                System.out.println("------------------------------------------------------------------------------------------------------------------");
                continue;
            }

            shopBusinessCard.put("platform_kind", 2);
            shopBusinessCard.put("apply_id", applyId);
            shopBusinessCard.put("apply_code", applyCode);
            shopBusinessCard.put("apply_code_id", applyId);
            String shopBusinessCardSql = getUpdateSQl(shopBusinessCard,
                    Wrappers.lambdaQuery(shopBusinessCardTable)
                            .eq("id", shopBusinessCard.get("id")));
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "updatePlatformKind", "shopBusinessCardSql", shopBusinessCardSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            desDataUpdate(testUser, shopBusinessCardSql);
        }
    }

    private void updateProdMenuPermissions() {
        Long menuPid = 1687300237020479490L;

        Database prodUser = mapDatabase.get("prod_user");
        Table adminUserTable = prodUser.getMapTable().get("admin_user");
        Table adminMenuTable = prodUser.getMapTable().get("admin_menu");
        Table adminRoleMenuTable = prodUser.getMapTable().get("admin_role_menu");
        Table adminRoleUsersTable = prodUser.getMapTable().get("admin_role_users");

        List<Long> adminUserIds = srcDataList(
                prodUser,
                Wrappers.lambdaQuery(adminUserTable)
                        .eq("apply_id", 2)
        ).stream().map(po -> (Long) po.get("id")).distinct().collect(Collectors.toList());

        List<Long> adminRoleIds = srcDataList(
                prodUser,
                Wrappers.lambdaQuery(adminRoleUsersTable)
                        .in("user_id", adminUserIds)
        ).stream().map(po -> (Long) po.get("role_id")).distinct().collect(Collectors.toList());

        List<Long> sonMenuIds = srcDataList(
                prodUser,
                Wrappers.lambdaQuery(adminMenuTable)
                        .eq("pid", menuPid)
        ).stream().map(po -> (Long) po.get("id")).distinct().collect(Collectors.toList());

        for (Long sonMenuId : sonMenuIds) {
            for (Long adminRoleId : adminRoleIds) {
                List<Map<String, Object>> adminRoleMenus = srcDataList(
                        prodUser,
                        Wrappers.lambdaQuery(adminRoleMenuTable)
                                .eq("menu_id", sonMenuId)
                                .eq("role_id", adminRoleId));
                if (!adminRoleMenus.isEmpty()) {
                    continue;
                }

                Map<String, Object> mapData = new HashMap<>();
                mapData.put("utc_created", System.currentTimeMillis());
                mapData.put("role_id", adminRoleId);
                mapData.put("menu_id", sonMenuId);

                String insertSql = getInsertSQl(mapData, adminRoleMenuTable);
                LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "updateProdMenuPermissions", "insertSql", insertSql));
                System.out.println("------------------------------------------------------------------------------------------------------------------");
                desDataUpdate(prodUser, insertSql);
            }
        }
    }

    private void syncRoleMenuData() {
        syncRoleMenuData("admin_roles", "admin_roles_4");
        syncRoleMenuData("admin_menu", "admin_menu_4");
        syncRoleMenuData("admin_role_menu", "admin_role_menu_4");
        syncRoleMenuData("admin_apply_menu", "admin_apply_menu_4");
//        syncRoleMenuData("admin_role_users", "admin_role_users_4");
    }

    private void syncRoleMenuData(String srcTableName, String desTableName) {
        Table srcTable = srcDatabase.getMapTable().get(srcTableName);
        Table desTable = desDatabase.getMapTable().get(desTableName);
        List<Map<String, Object>> srcTableData = srcDataList(srcTable);
        for (Map<String, Object> mapData : srcTableData) {
            String insertSql = getInsertSQl(mapData, desTable);

            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "syncRoleMenuData", "insertSql", insertSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            desDataInsert(insertSql);
        }
    }

    private void syncShopOrganizationStatus() {
        Table organizationTable = srcDatabase.getMapTable().get("organization");
        Table shopTable = srcDatabase.getMapTable().get("shop");

        Map<String, Map<String, Object>> organizationData = getMapData(srcDataList(organizationTable));
        Map<String, Map<String, Object>> shopData = getMapData(srcDataList(shopTable));
        List<Map<String, Object>> lstShopData = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : shopData.entrySet()) {
            Long organizationId = (Long) map.getValue().get("organization_id");
            if (!organizationData.containsKey(organizationId + "")) {
                continue;
            }
            Map<String, Object> organization = organizationData.get(organizationId + "");
            Integer organizationStatus = (Integer) organization.get("organization_status");
            if (organizationStatus == 0) {
                continue;
            }
            Integer shopOrganizationStatus = (Integer) map.getValue().get("organization_status");
            if (Objects.equals(organizationStatus, shopOrganizationStatus)) {
                continue;
            }
            map.getValue().put("organization_status", organizationStatus);
            lstShopData.add(map.getValue());
        }

        for (Map<String, Object> mapData : lstShopData) {
            Long id = (Long) mapData.get("id");
            String updateSql = getUpdateSQl(
                    mapData, shopTable.getName(),
                    Wrappers.lambdaQuery()
                            .eq("id", id));

            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "syncShopOrganizationStatus", "updateSql", updateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            srcDataUpdate(updateSql);
        }
        System.out.println("\n");
    }

    private void syncShopApplyId() {
        Table organizationTable = srcDatabase.getMapTable().get("organization");
        Table shopTable = srcDatabase.getMapTable().get("shop");

        Map<String, Map<String, Object>> organizationData = getMapData(srcDataList(organizationTable));
        Map<String, Map<String, Object>> shopData = getMapData(srcDataList(shopTable));
        List<Map<String, Object>> lstShopData = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : shopData.entrySet()) {
            Long organizationId = (Long) map.getValue().get("organization_id");
            if (!organizationData.containsKey(organizationId + "")) {
                continue;
            }
            Map<String, Object> organization = organizationData.get(organizationId + "");
            Integer applyId = (Integer) organization.get("apply_id");
            if (applyId == 0) {
                continue;
            }
            Integer shopApplyId = (Integer) map.getValue().get("apply_id");
            if (Objects.equals(applyId, shopApplyId)) {
                continue;
            }
            map.getValue().put("apply_id", applyId);
            lstShopData.add(map.getValue());
        }

        for (Map<String, Object> mapData : lstShopData) {
            Long id = (Long) mapData.get("id");
            String updateSql = getUpdateSQl(
                    mapData, shopTable.getName(),
                    Wrappers.lambdaQuery()
                            .eq("id", id));

            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "categoryModifierIdUpdate", "updateSql", updateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            srcDataUpdate(updateSql);
        }
        System.out.println("\n");
    }

    private void contactPersonUpdate() {
        for (Database database : databases) {
            srcDatabase = database;
            contactPersonUpdate(srcDatabase);
        }
    }

    private void contactPersonUpdate(Database srcDatabase) {
        Table organizationTable = srcDatabase.getMapTable().get("organization");
        Table adminUserTable = srcDatabase.getMapTable().get("admin_user");
        Table shopTable = srcDatabase.getMapTable().get("shop");


        Map<String, Map<String, Object>> organizationData = getMapData(srcDataList(organizationTable));
        Map<String, Map<String, Object>> adminUserData = getMapData(srcDataList(adminUserTable));
        Map<String, Map<String, Object>> shopData = getMapData(srcDataList(shopTable));
        List<Map<String, Object>> lstShopData = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : shopData.entrySet()) {
            Long organizationId = (Long) map.getValue().get("organization_id");
            if (!organizationData.containsKey(organizationId + "")) {
                continue;
            }
            Map<String, Object> organization = organizationData.get(organizationId + "");
            Long administratorId = (Long) organization.get("administrator_id");
            if (!adminUserData.containsKey(administratorId + "")) {
                continue;
            }
            Map<String, Object> adminUser = adminUserData.get(administratorId + "");

            Long adminUserMobile = (Long) adminUser.get("mobile");
            Long shopMobile = (Long) map.getValue().get("mobile");
            Long shopContactPersonPhone = (Long) map.getValue().get("contact_person_phone");

            if (Objects.equals(adminUserMobile, shopMobile) &&
                    Objects.equals(adminUserMobile, shopContactPersonPhone) ||
                    (adminUserMobile + "").length() != 11) {
                continue;
            }

            map.getValue().put("contact_person_phone", adminUserMobile);
            map.getValue().put("mobile", adminUserMobile);
            lstShopData.add(map.getValue());
        }

        for (Map<String, Object> mapData : lstShopData) {
            Long id = (Long) mapData.get("id");
            String updateSql = getUpdateSQl(
                    mapData, shopTable.getName(),
                    Wrappers.lambdaQuery()
                            .eq("id", id));

            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "categoryModifierIdUpdate", "updateSql", updateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            srcDataUpdate(updateSql);
        }
        System.out.println("\n");
    }

    private void jsonFieldTest() {
        Table workflowLog = srcDatabase.getMapTable().get("workflow_log");
        Table organization = srcDatabase.getMapTable().get("organization");

        Map<String, Map<String, Object>> workflowLogData = getMapData(srcDataList(workflowLog));
        Map<String, Map<String, Object>> organizationData = getMapData(srcDataList(organization));

        LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "jsonFieldTest", "workflowLogData.size()", workflowLogData.size()));
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "jsonFieldTest", "workflowLogData", workflowLogData));
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "jsonFieldTest", "organizationData.size()", organizationData.size()));
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "jsonFieldTest", "organizationData", organizationData));
        System.out.println("------------------------------------------------------------------------------------------------------------------");
    }

    private void msgTemplateImport() {
        Table msgTemplateTable = desDatabase.getMapTable().get("_msg_template");
        String excelPath = "C:\\Users\\Admin\\Downloads\\msg-template-1702631689927(1).xlsx";
        List<Map<String, String>> lstData = PoiExcelUtil.toMap(excelPath);
        lstData = lstData.stream().filter(po -> po.containsKey("id")).collect(Collectors.toList());
        for (Map<String, String> mapData : lstData) {

            Map<String, Object> tempMapData = new HashMap<>();
            tempMapData.put("link_mark", "aftersale_detail");
            Long id = GenUtil.objToLong(mapData.get("id"));
            mapData.forEach(tempMapData::put);
            tempMapData.put("is_audio", 0);
            tempMapData.put("id", id);


            String insertSql = getInsertSQl(tempMapData, msgTemplateTable.getName(),
                    Wrappers.lambdaQuery()
                            .eq("id", id));

            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "categoryModifierIdUpdate", "insertSql", insertSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            desDataInsert(insertSql);
        }

        LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "msgTemplateImport", "lstData", lstData));
    }

    private void relWorkerTypeLogExport() {
        Table relWorkerTypeLogTable = desDatabase.getMapTable().get("rel_worker__worker_type_log");
        Table workerTypeTable = desDatabase.getMapTable().get("worker_type");

        List<Map<String, String>> lstData = new ArrayList<>();
        List<Map<String, Object>> relWorkerTypeLogDataList = desDataList(relWorkerTypeLogTable);
        Map<String, Map<String, Object>> mapWorkerType = getMapData(desDataList(workerTypeTable));
        for (Map<String, Object> relWorkerTypeLogData : relWorkerTypeLogDataList) {
            String relatedTypesStr = (String) relWorkerTypeLogData.get("related_types");
            Map<String, Object> relatedTypes = (Map<String, Object>) GenUtil.fromJsonString(relatedTypesStr, Map.class);
            List<Map<String, Object>> relWorkerTypes = (List<Map<String, Object>>) relatedTypes.get("relWorkerTypes");

            Long utcCreated = (Long) relWorkerTypeLogData.get("utc_created");
            LocalDateTime dateTime = GenUtil.toLocalDateTime(new Date(utcCreated * 1000));
            String dateTimeStr = GenUtil.localDateTimeToStr(dateTime);

            List<String> workerTypeNames = new ArrayList<>();
            for (Map<String, Object> relWorkerType : relWorkerTypes) {
                String typeId = relWorkerType.get("typeId") + "";
                if (!mapWorkerType.containsKey(typeId)) {
                    continue;
                }
                Map<String, Object> workerType = mapWorkerType.get(typeId);
                String name = GenUtil.objToStr(workerType.get("name"));
                workerTypeNames.add(name);
            }

            Map<String, String> mapData = new HashMap<>();
            mapData.put("dateTimeStr", dateTimeStr);
            mapData.put("workerTypeNameStr", String.join("、", workerTypeNames));
            lstData.add(mapData);
            System.out.println("-----------[SmallAssignmentUpdateService] relWorkerTypeLogExport -> relatedTypes: " + relatedTypes);
        }

        List<List<String>> lstHeader = new ArrayList<>();
        lstHeader.add(Collections.singletonList("序号"));
        lstHeader.add(Collections.singletonList("工种列表"));
        lstHeader.add(Collections.singletonList("变更时间"));

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet();
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
        int rowIndex = lstHeader.get(0).size();
        for (Map<String, String> mapData : lstData) {
            String dateTimeStr = mapData.get("dateTimeStr");
            String workerTypeNameStr = mapData.get("workerTypeNameStr");

            int colIndex = 0;
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, rowIndex);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, workerTypeNameStr);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex++, colIndex, dateTimeStr);
        }
        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\worker-types.xlsx");
    }

    private void categoryModifierIdUpdate() {
        Table organizationTable = desDatabase.getMapTable().get("organization");
        Table organizationCategoryTable = desDatabase.getMapTable().get("organization_category_ultra");

        List<Map<String, Object>> lstData = new ArrayList<>();
        Map<String, Map<String, Object>> mapOrganizationData = getMapData(desDataList(organizationTable));
        Map<String, Map<String, Object>> mapOrganizationCategoryData = getMapData(desDataList(organizationCategoryTable));
        for (Map.Entry<String, Map<String, Object>> map : mapOrganizationCategoryData.entrySet()) {
            Long modifierId = (Long) map.getValue().get("modifier_id");
            if (modifierId > 0) {
                continue;
            }
            String organizationId = map.getValue().get("organization_id") + "";
            if (!mapOrganizationData.containsKey(organizationId)) {
                continue;
            }
            Map<String, Object> organization = mapOrganizationData.get(organizationId);
            Long administratorId = (Long) organization.get("administrator_id");
            if (administratorId == 0) {
                continue;
            }
            map.getValue().put("modifier_id", administratorId);
            lstData.add(map.getValue());
        }
        if (lstData.isEmpty()) {
            return;
        }
        for (Map<String, Object> mapData : lstData) {
            Long id = (Long) mapData.get("id");
            String updateSql = getUpdateSQl(mapData, organizationCategoryTable.getName(),
                    Wrappers.lambdaQuery()
                            .eq("id", id));
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "categoryModifierIdUpdate", "updateSql", updateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            desDataUpdate(updateSql);
        }
    }

}