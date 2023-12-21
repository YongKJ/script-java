package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import com.yongkj.util.PoiExcelUtil;
import com.yongkj.util.ThreadUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ShopWorkerDataExportService extends BaseService {

    private final String path;
    private final boolean enable;
    private final String environment;
    private final int ROW_ACCESS_WINDOW_SIZE;
    private final Map<String, List<String>> mapTables;
    private final Map<String, Map<String, Map<String, Object>>> mapTableData;

    public ShopWorkerDataExportService(DataMigration dataMigration) {
        super(dataMigration);
        this.ROW_ACCESS_WINDOW_SIZE = 100;
        this.mapTableData = new HashMap<>();
        Map<String, Object> shopWorkerDataExport = GenUtil.getMap("shop-worker-data-export");
        this.path = GenUtil.objToStr(shopWorkerDataExport.get("path"));
        this.enable = GenUtil.objToBoolean(shopWorkerDataExport.get("enable"));
        this.environment = GenUtil.objToStr(shopWorkerDataExport.get("environment"));
        List<Map<String, Object>> tables = (List<Map<String, Object>>) shopWorkerDataExport.get("tables");
        this.mapTables = tables.stream().collect(Collectors.toMap(po -> GenUtil.objToStr(po.get("name")), po -> (List<String>) po.get("fields")));
    }

    public void apply() {
        if (!enable) return;
        initTableData();
        exportData();
    }

    private void initTableData() {
        for (Map.Entry<String, List<String>> map : mapTables.entrySet()) {
            Table table = Objects.equals(environment, "test") ?
                    desDatabase.getMapTable().get(map.getKey()) :
                    srcDatabase.getMapTable().get(map.getKey());
            List<Map<String, Object>> lstData = Objects.equals(environment, "test") ?
                    desDataList(table) : srcDataList(table);
            Map<String, Map<String, Object>> mapData = new HashMap<>();
            switch (map.getKey()) {
                case "admin_role_users":
                    mapData.putAll(getMapData(lstData, Arrays.asList("user_id", "role_id", "utc_created", "utc_deleted")));
                    break;
                case "organization_worker":
                    mapData.putAll(getMapData(lstData, Arrays.asList("organization_id", "worker_id")));
                    break;
                case "rel_worker__worker_type":
                    mapData.putAll(getMapData(lstData, Arrays.asList("organization_id", "worker_id", "type_id", "utc_created", "utc_deleted")));
                    break;
                default:
                    mapData.putAll(getMapData(lstData));
            }
            mapTableData.put(map.getKey(), mapData);
        }
    }

    private void exportData() {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        exportShopData(workbook.createSheet("商家店铺信息"), lstCellStyle);
//        exportWorkerData(workbook.createSheet("店铺服务人员信息"), lstCellStyle);
        PoiExcelUtil.write(workbook, String.format("%s-%s.xlsx", path, System.currentTimeMillis()));
    }

    private void exportWorkerData(SXSSFSheet sheet, List<CellStyle> lstCellStyle) {
        List<List<String>> lstHeader = getWorkerDataHeader();
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
        List<Map<String, Object>> lstData = getWorkerData();
        writeSheetData(lstData, sheet, lstCellStyle, lstHeader.get(0).size());
    }

    private List<Map<String, Object>> getWorkerData() {
        return new ArrayList<>();
    }

    private List<List<String>> getWorkerDataHeader() {
        List<String> lstName = Arrays.asList(
                "序号", "商家组织标识", "商家主体名称", "商家状态",
                "店铺标识", "店铺名称",
                "服务人员标识", "服务人员名称", "服务人员手机号", "服务人员状态"
        );
        List<List<String>> lstHeader = new ArrayList<>();
        for (String name : lstName) {
            lstHeader.add(Collections.singletonList(name));
        }
        return lstHeader;
    }

    private void exportShopData(SXSSFSheet sheet, List<CellStyle> lstCellStyle) {
        List<List<String>> lstHeader = getShopDataHeader();
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
        List<Map<String, Object>> lstData = getShopData();
        writeSheetData(lstData, sheet, lstCellStyle, lstHeader.get(0).size());
    }

    private List<List<String>> getShopDataHeader() {
        List<String> lstName = Arrays.asList(
                "序号", "用户标识", "应用名称", "用户名称", "用户密码", "注册手机号", "数据角色", "用户状态", "用户创建时间",
                "商家组织标识", "商家主体名称", "商家组织类型", "法人名称", "法人手机号", "商家状态",
                "店铺标识", "店铺名称", "店铺地址", "服务城市", "服务地区"
        );
        List<List<String>> lstHeader = new ArrayList<>();
        for (String name : lstName) {
            lstHeader.add(Collections.singletonList(name));
        }
        return lstHeader;
    }

    private List<Map<String, Object>> getShopData() {
        List<Map<String, Object>> lstRowData = new ArrayList<>();
        Map<String, Map<String, Object>> mapAdminUser = mapTableData.get("admin_user");
        for (Map.Entry<String, Map<String, Object>> map : mapAdminUser.entrySet()) {

            Map<String, Object> adminUser = map.getValue();
            Long adminUserId = (Long) adminUser.get("id");
            Integer applyId = (Integer) adminUser.get("applyId");
            String applyName = getApplyName(applyId);
            String username = (String) adminUser.get("username");
            String password = (String) adminUser.get("password");
            String realPassword = getRealPassword(password);
            Long mobile = (Long) adminUser.get("mobile");
            String roleName = getRoleName(adminUserId);
            Integer adminUserStatus = (Integer) adminUser.get("status");
            String adminUserStatusName = getAdminUserStatusName(adminUserStatus);
            Long utcCreated = (Long) adminUser.get("utc_created");
            String utcCreatedStr = getUtcCreatedStr(utcCreated);

            Map<String, Object> organization = getDataByNameAndField("organization", "administrator_id", adminUserId);
            Long organizationId = (Long) organization.get("id");
            String organizationName = (String) organization.get("name");
            Integer organizationType = (Integer) organization.get("type");
            String organizationTypeName = getOrganizationTypeName(organizationType);
            Long legalPersonId = (Long) organization.get("legal_person_id_card_id");
            Long legalPersonMobile = (Long) organization.get("legal_person_mobile");
            Integer organizationStatus = (Integer) organization.get("organization_status");
            String organizationStatusName = getOrganizationStatusName(organizationStatus);

            Map<String, Object> idCard = getDataByNameAndField("id_card", "id", legalPersonId);
            String legalPersonName = (String) idCard.get("name");

            Map<String, Object> shop = getDataByNameAndField("shop", "organization_id", organizationId);
            Long shopId = (Long) shop.get("id");
            String shopName = (String) shop.get("name");
            String provinceName = (String) shop.get("province_name");
            String cityName = (String) shop.get("city_name");
            String districtName = (String) shop.get("district_name");
            String streetName = (String) shop.get("street_name");
            String address = (String) shop.get("address");
            String addressDetail = String.join("", Arrays.asList(provinceName, cityName, districtName, streetName, address));
            Long serviceCityId = (Long) shop.get("service_city_id");
            String serviceCityName = getServiceCityName(serviceCityId);
            List<Long> serviceDistrictId = new ArrayList<>();
            if (shop.get("service_district_id") instanceof List) {
                serviceDistrictId = (List<Long>) shop.get("service_district_id");
            }
            String serviceDistrictName = getServiceDistrictName(serviceDistrictId);

            Map<String, Object> mapRowData = new LinkedHashMap<>();
            mapRowData.put("adminUserId", adminUserId);
            mapRowData.put("applyName", applyName);
            mapRowData.put("username", username);
            mapRowData.put("realPassword", realPassword);
            mapRowData.put("mobile", mobile);
            mapRowData.put("roleName", roleName);
            mapRowData.put("adminUserStatusName", adminUserStatusName);
            mapRowData.put("utcCreatedStr", utcCreatedStr);

            mapRowData.put("organizationId", organizationId);
            mapRowData.put("organizationName", organizationName);
            mapRowData.put("organizationTypeName", organizationTypeName);
            mapRowData.put("legalPersonName", legalPersonName);
            mapRowData.put("legalPersonMobile", legalPersonMobile);
            mapRowData.put("organizationStatusName", organizationStatusName);

            mapRowData.put("shopId", shopId);
            mapRowData.put("shopName", shopName);
            mapRowData.put("addressDetail", addressDetail);
            mapRowData.put("serviceCityName", serviceCityName);
            mapRowData.put("serviceDistrictName", serviceDistrictName);

            lstRowData.add(mapRowData);
        }
        return lstRowData;
    }

    private String getServiceDistrictName(List<Long> serviceDistrictId) {
        if (serviceDistrictId == null || serviceDistrictId.isEmpty()) return "";
        List<Map<String, Object>> lstData = desDataList(
                Wrappers.lambdaQuery("amap_district")
                        .in("id", serviceDistrictId)
                        .select("name"));
        List<String> lstName = lstData.stream().map(po -> (String) po.get("name")).collect(Collectors.toList());
        return String.join(", ", lstName);
    }

    private String getServiceCityName(Long serviceCityId) {
        if (serviceCityId == null) return "";
        List<Map<String, Object>> lstData = desDataList(
                Wrappers.lambdaQuery("amap_district")
                        .eq("id", serviceCityId)
                        .select("name"));
        List<String> lstName = lstData.stream().map(po -> (String) po.get("name")).collect(Collectors.toList());
        return String.join(", ", lstName);
    }

    private String getOrganizationStatusName(Integer organizationStatus) {
        if (organizationStatus == null) return "";
        switch (organizationStatus) {
            case 1:
                return "待审核";
            case 2:
                return "黑名单";
            case 3:
                return "已通过";
            case 4:
                return "已冻结";
            case 5:
                return "已拒绝";
            default:
                return "";
        }
    }

    private String getOrganizationTypeName(Integer organizationType) {
        if (organizationType == null) return "";
        switch (organizationType) {
            case 1:
                return "平台";
            case 2:
                return "个人商家";
            case 3:
                return "企业/个体工商户";
            case 4:
                return "个体工商户";
            case 5:
                return "企业/公司";
            default:
                return "";
        }
    }

    private String getUtcCreatedStr(Long utcCreated) {
        LocalDateTime dateTime = GenUtil.timestampToLdt(utcCreated * 1000);
        return GenUtil.localDateTimeToStr(dateTime);
    }

    private String getAdminUserStatusName(Integer adminUserStatus) {
        if (adminUserStatus == null) return "";
        switch (adminUserStatus) {
            case 0:
                return "正常";
            case 1:
                return "冻结";
            default:
                return "";
        }
    }

    private String getRoleName(Long adminUserId) {
        List<Map<String, Object>> adminRoleUsers = getDataListByNameAndField("admin_role_users", "user_id", adminUserId);
        List<Long> lstRoleId = adminRoleUsers.stream().map(po -> (Long) po.get("role_id")).collect(Collectors.toList());
        List<Map<String, Object>> roles = getDataListByNameAndField("admin_roles", "id", lstRoleId);
        List<String> roleNames = roles.stream().map(po -> (String) po.get("name")).collect(Collectors.toList());
        return String.join(", ", roleNames);
    }

    private String getRealPassword(String password) {
        if (Objects.equals(password, "4297f44b13955235245b2497399d7a93")) {
            return "123123";
        }
        return "未知";
    }

    private String getApplyName(Integer applyId) {
        if (applyId == null) return "";
        switch (applyId) {
            case 1:
                return "平台";
            case 2:
                return "服务商家";
            case 3:
                return "商品商家";
            default:
                return "";
        }
    }

    private <T> List<Map<String, Object>> getDataListByNameAndField(String tableName, String fieldName, List<T> fieldValues) {
        Map<String, Map<String, Object>> mapOrganization = Optional.ofNullable(mapTableData.get(tableName)).orElse(new HashMap<>());
        return mapOrganization.values().stream()
                .filter(po -> fieldValues.contains(po.get(fieldName)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDataListByNameAndField(String tableName, String fieldName, Object fieldValue) {
        Map<String, Map<String, Object>> mapOrganization = Optional.ofNullable(mapTableData.get(tableName)).orElse(new HashMap<>());
        return mapOrganization.values().stream()
                .filter(po -> Objects.equals(po.get(fieldName), fieldValue))
                .collect(Collectors.toList());
    }

    private Map<String, Object> getDataByNameAndField(String tableName, String fieldName, Object fieldValue) {
        Map<String, Map<String, Object>> mapOrganization = Optional.ofNullable(mapTableData.get(tableName)).orElse(new HashMap<>());
        return mapOrganization.values().stream()
                .filter(po -> Objects.equals(po.get(fieldName), fieldValue))
                .findFirst().orElse(new HashMap<>());
    }

    private void writeSheetData(List<Map<String, Object>> lstData, SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow) {
        for (int i = 0; i < lstData.size(); ) {
            int dataSize = i + ROW_ACCESS_WINDOW_SIZE;
            dataSize = Math.min(dataSize, lstData.size());
            ThreadUtil.executeWithListDataByThreadPool(1, sheet, i, dataSize, (row, index) -> {
                int colIndex = 0;
                PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, row.getRowNum());
                for (Map.Entry<String, Object> map : Optional.ofNullable(lstData.get(index)).orElse(new HashMap<>()).entrySet()) {
                    PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, map.getValue());
                }
            });
            i = dataSize;
        }
    }

    private void test() {
        LogUtil.loggerLine(Log.of("ShopWorkerDataExport", "test", "mapTables", mapTables));
    }
}
