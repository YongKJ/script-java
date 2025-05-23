package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.CsvUtil;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MaxComputeStatisticsInitService extends BaseService {

    private final boolean enable;

    public MaxComputeStatisticsInitService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> maxComputeAssignment = GenUtil.getMap("max-compute-statistics-init");
        this.enable = GenUtil.objToBoolean(maxComputeAssignment.get("enable"));
    }

    public void apply() {
        if (!enable) return;

//        statisticsRegistrationData();
//        statisticsLoginData();
//        statisticsLoginDwsData();
//        statisticsEvaluateDwsData();
//        statisticsOverallCustomerAdsDwsData();
//        statisticsEvaluateDwdData();
//        statisticsLoginDwdData();
//        statisticsMerchantDwsData();
//        statisticsMerchantDwdData();
//        statisticsWorkerInfoDwdData();
//        statisticsWorkerShopDwdData();
//        statisticsWorkerEvaluateDwdData();
//        statisticsWorkerBaseInfoDwsData();
//        statisticsDwdCustomerPageVisitData();
//        statisticsDwsCustomerBrowsingBehaviorStatisticsData();
//        statisticsOdsBrowsePathInfoData();
//        statisticsDwdWorkerPortraitInfoData();
//        statisticsDwdWorkerLoadDistributeOrderData();
//        statisticsDwdWorkerLoadLongOrderDiData();
//        statisticsDwdCustomerScanDiData();
//        statisticsDwdCustomerScanDeepDiData();
//        statisticsDwsCustomerScanDiData();
//        statisticsDwsMarketingScreenDiData();
//        fixStatisticsDwsMarketingScreenDiData();
//        statisticsDwsCustomerScreenDiData();
//        statisticsDwdActiveCustomerScreenDiData();
//        fixStatisticsDwsMarketingScreenDiDataLatest();
//        statisticsDwsWorkerOrbitDiData();
//        statisticsDwdWorkerOrbitDetailDiData();
//        statisticsDwdWorkerOrbitOrderDiData();
//        statisticsDwsLifeCustomerOverviewStatisticsData();
//        statisticsDwsLifeCustomerWatchContentData();
//        statisticsDwsLifeCustomerLoginFrequencyData();
        statisticsDwsLifeCustomerAreaDistributedData();
    }

    private void statisticsDwsLifeCustomerAreaDistributedData() {
        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\marketing-screen-district-type-id.json");
        List<Map<String, Object>> lstDistrictTypeId = GenUtil.fromJsonString(content, List.class);

        List<Map<String, Object>> lstProvince = lstDistrictTypeId.stream().filter(po -> Objects.equals(po.get("level"), "province")).collect(Collectors.toList());
        List<Map<String, Object>> lstCity = lstDistrictTypeId.stream().filter(po -> Objects.equals(po.get("level"), "city")).collect(Collectors.toList());
        List<Map<String, Object>> lstDistrict = lstDistrictTypeId.stream().filter(po -> Objects.equals(po.get("level"), "district")).collect(Collectors.toList());

        List<Map<String, Object>> lstData = new ArrayList<>();
        for (int i = 0; i < 4198; i++) {
            Integer provinceIndex = GenUtil.random(0, lstProvince.size() - 1);
            Integer cityIndex = GenUtil.random(0, lstCity.size() - 1);
            Integer districtIndex = GenUtil.random(0, lstDistrict.size() - 1);

            Map<String, Object> provinceData = lstProvince.get(provinceIndex);
            Integer provinceId = (Integer) provinceData.get("id");
            String provinceName = (String) provinceData.get("name");

            Map<String, Object> cityData = lstCity.get(cityIndex);
            Integer cityId = (Integer) cityData.get("id");
            String cityName = (String) cityData.get("name");

            Map<String, Object> districtData = lstDistrict.get(districtIndex);
            Integer districtId = (Integer) districtData.get("id");
            String districtName = (String) districtData.get("name");

            int consumerId = i + 1;
            String consumerName = "顾客" + consumerId;

            String ipLocation = "广东省" + cityName + districtName;

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("customer_id", consumerId);
            mapData.put("customer_name", consumerName);
            mapData.put("ip_location", ipLocation);
            mapData.put("province_id", 440000);
            mapData.put("province_name", "广东省");
            mapData.put("city_id", cityId);
            mapData.put("city_name", cityName);
            mapData.put("district_id", districtId);
            mapData.put("district_name", districtName);
            mapData.put("ds", "20250225");
            lstData.add(mapData);
        }
        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_life_customer_area_distributed_di");
        String insertSql = getMaxComputeInsertListSQl(lstData, table);
        srcDataInsert(preDatabaseMaxCompute, insertSql);
    }

    private void statisticsDwsLifeCustomerLoginDistributedData() {
        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2025, 2, 16),
                LocalDate.of(2025, 2, 17),
                LocalDate.of(2025, 2, 18),
                LocalDate.of(2025, 2, 19),
                LocalDate.of(2025, 2, 20),
                LocalDate.of(2025, 2, 21),
                LocalDate.of(2025, 2, 22),
                LocalDate.of(2025, 2,23),
                LocalDate.of(2025, 2, 24),
                LocalDate.of(2025, 2, 25)
        );

        List<Map<String, Object>> lstData = new ArrayList<>();
        for (LocalDate date : lstDate) {
            int number = GenUtil.random(25, 50);
            for (int i = 0; i < number; i++) {
                int hour = GenUtil.random(0, 23);
                int minute = GenUtil.random(0, 59);
                String hourStr = hour < 10 ? "0" + hour : "" + hour;
                String minuteStr = minute < 10 ? "0" + minute : "" + minute;

                String dateStr = GenUtil.localDateToStr(date);
                String loginTimeStr = String.format("%s %s:%s:%s", dateStr, hourStr, minuteStr, "00");
                Long loginTime = GenUtil.strToTimestamp(loginTimeStr);

                Integer consumerId = i + 1;
                String consumerName = "顾客" + consumerId;
                Integer osType = GenUtil.random(1, 2);
                String dsStr = GenUtil.localDateToStr(date, "yyyyMMdd");

                Map<String, Object> mapData = new HashMap<>();
                mapData.put("ds", dsStr);
                mapData.put("os_type", osType);
                mapData.put("login_time", loginTime);
                mapData.put("customer_id", consumerId);
                mapData.put("customer_name", consumerName);
                lstData.add(mapData);
            }
            Table table = preDatabaseMaxCompute.getMapTable().get("dwd_life_customer_login_distributed_di");
            String insertSql = getMaxComputeInsertListSQl(lstData, table);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
            lstData = new ArrayList<>();
        }
    }

    private void statisticsDwsLifeCustomerLoginFrequencyData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Integer consumerId = i + 1;
            String consumerName = "顾客" + consumerId;
            Integer loginFrequency1w = GenUtil.random(1, 25);

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("ds", "20250225");
            mapData.put("customer_id", consumerId);
            mapData.put("customer_name", consumerName);
            mapData.put("login_frequency_1w", loginFrequency1w);
            lstData.add(mapData);
        }
        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_life_customer_login_frequency_di");
        String insertSql = getMaxComputeInsertListSQl(lstData, table);
        srcDataInsert(preDatabaseMaxCompute, insertSql);
    }

    private void statisticsDwsLifeCustomerWatchContentData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/life-customer-watch-content.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_life_customer_watch_content_di");
        for (Map<String, Object> mapData : lstData) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsDwsLifeCustomerOverviewStatisticsData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/life-customer-overview-dws.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_life_customer_overview_statistics_di");
        for (Map<String, Object> mapData : lstData) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsDwdWorkerOrbitOrderDiData() {
        String content = FileUtil.read("D:\\Document\\MyCodes\\Github\\script-java\\src\\main\\resources\\csv\\max-compute\\statistics\\worker-orbit-detail-dwd.json");
        Map<String, Object> mapData = GenUtil.fromJsonString(content, Map.class);
        LocalDate nowDate = LocalDate.now();

        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, Object>> lstWorker = (List<Map<String, Object>>) mapData.get("worker_names");
        List<Map<String, Object>> lstCategory = (List<Map<String, Object>>) mapData.get("categories");
        for (Map<String, Object> worker : lstWorker) {
            Long workerId = (Long) worker.get("id");
            String workerName = (String) worker.get("name");
            List<Map<String, Object>> lstJob = (List<Map<String, Object>>) worker.get("job");
            for (int i = 0; i < lstJob.size(); i++) {
                Map<String, Object> job = lstJob.get(i);
                Double lng = (Double) job.get("lng");
                Double lat = (Double) job.get("lat");
                Integer time = (Integer) job.get("time");
                Long jobId = (Long) job.get("id");
                LocalDate date = nowDate.minusDays(lstJob.size() - i);
                String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

                Integer categoryIndex = GenUtil.random(1, lstCategory.size());
                Map<String, Object> category = lstCategory.get(categoryIndex - 1);
                Integer categoryId = (Integer) category.get("id");
                String categoryName = (String) category.get("name");
                List<Map<String, Object>> lstSonCategory = (List<Map<String, Object>>) category.get("son_category");

                Integer sonCategoryIndex = GenUtil.random(1, lstSonCategory.size());
                Map<String, Object> sonCategory = lstSonCategory.get(sonCategoryIndex - 1);
                Long sonCategoryId = (Long) sonCategory.get("id");
                String sonCategoryName = (String) sonCategory.get("name");
                List<Map<String, Object>> lstItem = (List<Map<String, Object>>) sonCategory.get("item");

                Integer itemIndex = GenUtil.random(1, lstItem.size());
                Map<String, Object> item = lstItem.get(itemIndex - 1);
                Long itemId = (Long) item.get("id");
                String itemName = (String) item.get("name");

                Map<String, Object> data = new HashMap<>();
                data.put("job_id", jobId);
                data.put("worker_id", workerId);
                data.put("worker_name", workerName);
                data.put("category_id", categoryId);
                data.put("category_name", categoryName);
                data.put("son_category_id", sonCategoryId);
                data.put("son_category_name", sonCategoryName);
                data.put("item_id", itemId);
                data.put("item_name", itemName);
                data.put("appointment_time", time);
                data.put("lng", lng);
                data.put("lat", lat);
                data.put("ds", ds);
                lstData.add(data);
            }
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_worker_orbit_job_di");
        String insertSql = getMaxComputeInsertListSQlNonePartition(lstData, table);
        srcDataInsert(preDatabaseMaxCompute, insertSql);
    }

    private void statisticsDwdWorkerOrbitDetailDiData() {
        String content = FileUtil.read("D:\\Document\\MyCodes\\Github\\script-java\\src\\main\\resources\\csv\\max-compute\\statistics\\worker-orbit-detail-dwd.json");
        Map<String, Object> mapData = GenUtil.fromJsonString(content, Map.class);
        LocalDate date = LocalDate.of(2024, 12, 24);
        String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, Object>> lstStatus = getWorkerStatus();
        List<String> lstAvatar = (List<String>) mapData.get("worker_avatars");
        List<Map<String, Object>> lstWorker = (List<Map<String, Object>>) mapData.get("worker_names");
        for (Map<String, Object> worker : lstWorker) {
            Long id = (Long) worker.get("id");
            String name = (String) worker.get("name");

            Integer avatarIndex = GenUtil.random(1, lstAvatar.size());
            String avatar = lstAvatar.get(avatarIndex - 1);

            Integer orderCompletedCnt = GenUtil.random(576, 1988);
            Integer workerServiceTime = GenUtil.random(54000, 198000);
            Integer workerMovingDistance = GenUtil.random(66, 288);
            Double consumerSatisfactionRate = GenUtil.round(GenUtil.randomDouble(0.6666, 0.9988), 4);
            Integer orderCompletedCnt1m = GenUtil.random(22, 88);

            Integer statusIndex = GenUtil.random(1, lstStatus.size());
            Map<String, Object> mapStatus = lstStatus.get(statusIndex - 1);
            String statusName = (String) mapStatus.get("name");
            Integer status = (Integer) mapStatus.get("id");

            Map<String, Object> data = new HashMap<>();
            data.put("worker_id", id);
            data.put("worker_name", name);
            data.put("worker_avatar", avatar);
            data.put("order_completed_cnt", orderCompletedCnt);
            data.put("worker_service_time", workerServiceTime);
            data.put("worker_moving_distance", workerMovingDistance);
            data.put("consumer_satisfaction_rate", consumerSatisfactionRate);
            data.put("order_completed_cnt_1m", orderCompletedCnt1m);
            data.put("worker_status", status);
            data.put("worker_status_name", statusName);
            data.put("ds", ds);
            lstData.add(data);
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_worker_orbit_detail_di");
        String insertSql = getMaxComputeInsertListSQlNonePartition(lstData, table);
        srcDataInsert(preDatabaseMaxCompute, insertSql);
    }

    private List<Map<String, Object>> getWorkerStatus() {
        List<Map<String, Object>> lstData = new ArrayList<>();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("id", 1);
        map1.put("name", "在线");
        lstData.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("id", 2);
        map2.put("name", "离线");
        lstData.add(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("id", 3);
        map3.put("name", "工作");
        lstData.add(map3);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("id", 4);
        map4.put("name", "空闲");
        lstData.add(map4);

        return lstData;
    }

    private void statisticsDwsWorkerOrbitDiData() {
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/worker-orbit-dws.csv");
        List<Map<String, Object>> lstData = new ArrayList<>();
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        LocalDate endDate = LocalDate.of(2024, 12, 24);
        LocalDate startDate = endDate.minusDays(11);
        List<Map<String, Object>> dataList = new ArrayList<>();
        LocalDate date = startDate;
        for (Map<String, Object> mapData : lstData) {
            date = date.plusDays(1);
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");
            mapData.put("ds", ds);
            dataList.add(mapData);
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_worker_orbit_di");
        String insertSql = getMaxComputeInsertListSQlNonePartition(dataList, table);
        srcDataInsert(preDatabaseMaxCompute, insertSql);
    }

    private void fixStatisticsDwsMarketingScreenDiDataLatest() {
        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\marketing-screen-district-type-id.json");
        List<Map<String, Object>> lstDistrictTypeId = GenUtil.fromJsonString(content, List.class);
        System.out.println("lstDistrictTypeId.size(): " + lstDistrictTypeId.size());

        Table table = preDatabaseHologres.getMapTable().get("dws_marketing_screen_di");
        for (Map<String, Object> mapDistrictTypeId : lstDistrictTypeId) {
            Integer districtTypeId = GenUtil.strToInteger(mapDistrictTypeId.get("id").toString());

            Long orderQuantityAssistedLiving = GenUtil.random(66, 199).longValue();
            Long orderQuantityCloudClassroom = GenUtil.random(66, 199).longValue();

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("order_quantity_assisted_living", orderQuantityAssistedLiving);
            mapData.put("order_quantity_cloud_classroom", orderQuantityCloudClassroom);

            String updateSql = getUpdateSQl(mapData,
                    Wrappers.lambdaQuery(table)
                            .eq("district_type_id", districtTypeId));

            System.out.println("updateSql: " + updateSql);
            desDataUpdate(preDatabaseHologres, updateSql);
        }
    }

    private void statisticsDwdActiveCustomerScreenDiData() {
        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\marketing-screen-district-type-id.json");
        List<Map<String, Object>> lstDistrictTypeId = GenUtil.fromJsonString(content, List.class);
        System.out.println("lstDistrictTypeId.size(): " + lstDistrictTypeId.size());

        List<LocalDate> lstDate = Arrays.asList(
//                LocalDate.of(2024, 11, 15),
//                LocalDate.of(2024, 11, 16),
//                LocalDate.of(2024, 11, 17),
//                LocalDate.of(2024, 11, 18),
//                LocalDate.of(2024, 11, 19),
//                LocalDate.of(2024, 11, 20),
//                LocalDate.of(2024, 11, 21),
//                LocalDate.of(2024, 11, 22),
//                LocalDate.of(2024, 11, 23),
//                LocalDate.of(2024, 11, 24),
//                LocalDate.of(2024, 11, 25),
//                LocalDate.of(2024, 11, 26),
//                LocalDate.of(2024, 11, 27),
                LocalDate.of(2024, 12, 19)
        );

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_active_customer_screen_di");
        for (LocalDate date : lstDate) {
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");
            List<Map<String, Object>> lstData = new ArrayList<>();
            for (Map<String, Object> mapDistrictTypeId : lstDistrictTypeId) {

                String name = (String) mapDistrictTypeId.get("name");
                String level = (String) mapDistrictTypeId.get("level");
                Integer id = GenUtil.strToInteger(mapDistrictTypeId.get("id").toString());

                for (int i = 0; i < 20; i++) {
                    Integer customerSex = GenUtil.random(1, 2);
                    Integer ageIndex = GenUtil.random(1, 5);
                    Integer customerAge = getCustomerAge(ageIndex);

                    Map<String, Object> mapData = new HashMap<>();
                    mapData.put("customer_id", i);
                    mapData.put("customer_name", "青冥" + (i + 1));
                    mapData.put("customer_sex", customerSex);
                    mapData.put("customer_age", customerAge);
                    mapData.put("district_type_name", name);
                    mapData.put("district_type_id", id);
                    mapData.put("level", level);
                    mapData.put("ds", ds);
                    lstData.add(mapData);
                }

                if (lstData.size() >= 10000) {
                    String insertSql = getMaxComputeInsertListSQlNonePartition(lstData, table);
                    srcDataInsert(preDatabaseMaxCompute, insertSql);
                    lstData = new ArrayList<>();
                }
            }
            if (!lstData.isEmpty()) {
                String insertSql = getMaxComputeInsertListSQlNonePartition(lstData, table);
                srcDataInsert(preDatabaseMaxCompute, insertSql);
            }
        }
    }

    private Integer getCustomerAge(Integer ageIndex) {
        switch (ageIndex) {
            case 1:
                return GenUtil.random(16, 29);
            case 2:
                return GenUtil.random(30, 39);
            case 3:
                return GenUtil.random(40, 49);
            case 4:
                return GenUtil.random(50, 59);
            case 5:
                return GenUtil.random(60, 89);
            default:
                return 90;
        }
    }

    private void statisticsDwsCustomerScreenDiData() {
        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\marketing-screen-district-type-id.json");
        List<Map<String, Object>> lstDistrictTypeId = GenUtil.fromJsonString(content, List.class);
        System.out.println("lstDistrictTypeId.size(): " + lstDistrictTypeId.size());

        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 11, 15),
                LocalDate.of(2024, 11, 16),
                LocalDate.of(2024, 11, 17),
                LocalDate.of(2024, 11, 18),
                LocalDate.of(2024, 11, 19),
                LocalDate.of(2024, 11, 20),
                LocalDate.of(2024, 11, 21),
                LocalDate.of(2024, 11, 22),
                LocalDate.of(2024, 11, 23),
                LocalDate.of(2024, 11, 24),
                LocalDate.of(2024, 11, 25),
                LocalDate.of(2024, 11, 26),
                LocalDate.of(2024, 11, 27),
                LocalDate.of(2024, 11, 28)
        );

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_customer_screen_di");
        for (LocalDate date : lstDate) {
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");
            List<Map<String, Object>> lstData = new ArrayList<>();
            for (Map<String, Object> mapDistrictTypeId : lstDistrictTypeId) {

                String name = (String) mapDistrictTypeId.get("name");
                String level = (String) mapDistrictTypeId.get("level");
                Integer id = GenUtil.strToInteger(mapDistrictTypeId.get("id").toString());

                Long totalRegisteredUserCnt = GenUtil.random(8888, 19999).longValue();
                Long activeUserCnt1m = GenUtil.random(2222, 5555).longValue();
                Long activeUserCnt1d = GenUtil.random(222, 555).longValue();
                Double registrationConversionRateTencentAdvertising = GenUtil.round(GenUtil.randomDouble(0.2222, 0.5555), 4);
                Double registrationConversionRateOceanEngine = GenUtil.round(GenUtil.randomDouble(0.2222, 0.5555), 4);
                Double registrationConversionRateNaturalGrowth = GenUtil.round(GenUtil.randomDouble(0.2222, 0.5555), 4);
                Double paymentConversionRateTencentAdvertising = GenUtil.round(GenUtil.randomDouble(0.3333, 0.6666), 4);
                Double paymentConversionRateOceanEngine = GenUtil.round(GenUtil.randomDouble(0.3333, 0.6666), 4);
                Double paymentConversionRateNaturalGrowth = GenUtil.round(GenUtil.randomDouble(0.3333, 0.6666), 4);

                Map<String, Object> mapData = new HashMap<>();
                mapData.put("total_registered_user_cnt", totalRegisteredUserCnt);
                mapData.put("active_user_cnt_1m", activeUserCnt1m);
                mapData.put("active_user_cnt_1d", activeUserCnt1d);
                mapData.put("registration_conversion_rate_tencent_advertising", registrationConversionRateTencentAdvertising);
                mapData.put("registration_conversion_rate_ocean_engine", registrationConversionRateOceanEngine);
                mapData.put("registration_conversion_rate_natural_growth", registrationConversionRateNaturalGrowth);
                mapData.put("payment_conversion_rate_tencent_advertising", paymentConversionRateTencentAdvertising);
                mapData.put("payment_conversion_rate_ocean_engine", paymentConversionRateOceanEngine);
                mapData.put("payment_conversion_rate_natural_growth", paymentConversionRateNaturalGrowth);
                mapData.put("district_type_name", name);
                mapData.put("district_type_id", id);
                mapData.put("level", level);
                mapData.put("ds", ds);
                lstData.add(mapData);
            }

            String insertSql = getMaxComputeInsertListSQlNonePartition(lstData, table);
//            System.out.println(insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
//            break;
        }
    }

    private void fixStatisticsDwsMarketingScreenDiData() {
        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\marketing-screen-district-type-id.json");
        Map<String, String> mapDistrictTypeIds = GenUtil.fromJsonString(content, Map.class);
        System.out.println("mapDistrictTypeIds.size(): " + mapDistrictTypeIds.size());

        Table table = preDatabaseHologres.getMapTable().get("dws_marketing_screen_di");
        for (Map.Entry<String, String> map : mapDistrictTypeIds.entrySet()) {
            Integer districtTypeId = GenUtil.strToInteger(map.getKey());
            String level = map.getValue();

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("level", level);

            String updateSql = getUpdateSQl(mapData,
                    Wrappers.lambdaQuery(table)
                            .eq("district_type_id", districtTypeId));
            System.out.println("updateSql: " + updateSql);
            desDataUpdate(preDatabaseHologres, updateSql);
        }
    }

    private void statisticsDwsMarketingScreenDiData() {
        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\marketing-screen-district-type-id.json");
        List<Map<String, Object>> lstDistrictTypeId = GenUtil.fromJsonString(content, List.class);
        System.out.println("lstDistrictTypeId.size(): " + lstDistrictTypeId.size());

        List<LocalDate> lstDate = Arrays.asList(
//                LocalDate.of(2024, 11, 14),
                LocalDate.of(2024, 11, 15),
                LocalDate.of(2024, 11, 16),
                LocalDate.of(2024, 11, 17),
                LocalDate.of(2024, 11, 18),
                LocalDate.of(2024, 11, 19),
                LocalDate.of(2024, 11, 20),
                LocalDate.of(2024, 11, 21),
                LocalDate.of(2024, 11, 22),
                LocalDate.of(2024, 11, 23),
                LocalDate.of(2024, 11, 24),
                LocalDate.of(2024, 11, 25),
                LocalDate.of(2024, 11, 26),
                LocalDate.of(2024, 11, 27)
        );

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_marketing_screen_di");
        for (LocalDate date : lstDate) {
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");
            List<Map<String, Object>> lstData = new ArrayList<>();
            for (Map<String, Object> mapDistrictTypeId : lstDistrictTypeId) {

                Integer id = (Integer) mapDistrictTypeId.get("id");
                Integer name = (Integer) mapDistrictTypeId.get("name");
                Integer level = (Integer) mapDistrictTypeId.get("level");

                Long totalSales = GenUtil.random(8888, 88888).longValue();
                Double totalSalesMonthComparison = GenUtil.round(GenUtil.randomDouble(0.6666, 0.9999), 4);
                Double totalSalesDayComparison = GenUtil.round(GenUtil.randomDouble(0.6666, 0.9999), 4);
                Long totalOrderQuantity = GenUtil.random(6666, 9999).longValue();
                Long customerOrderAvg = GenUtil.random(222, 555).longValue();
                Double customerRefundRate = GenUtil.round(GenUtil.randomDouble(0.0666, 0.1999), 4);
                Long registeredUserCnt = GenUtil.random(6666, 19999).longValue();
                Long orderQuantity1d = GenUtil.random(333, 555).longValue();
                Long orderQuantityHomeCare = GenUtil.random(66, 199).longValue();
                Long orderQuantityRehabilitationAssistance = GenUtil.random(66, 199).longValue();
                Long orderQuantityHospitalization = GenUtil.random(66, 199).longValue();
                Long orderQuantitySuitableProducts = GenUtil.random(66, 199).longValue();
                Long orderQuantityAccompanyGeneral = GenUtil.random(66, 199).longValue();
                Long orderQuantityAccompanyErrand = GenUtil.random(66, 199).longValue();
                Long orderQuantityHousekeeping = GenUtil.random(66, 199).longValue();
                Long orderQuantityAdaptiveAge = GenUtil.random(66, 199).longValue();
                Long orderQuantityHealthCare = GenUtil.random(66, 199).longValue();
                Double registrationConversionRate = GenUtil.round(GenUtil.randomDouble(0.6666, 0.9999), 4);
                Double userRetentionRate = GenUtil.round(GenUtil.randomDouble(0.6666, 0.9999), 4);
                Double shoppingCartConversionRate = GenUtil.round(GenUtil.randomDouble(0.6666, 0.9999), 4);

                Map<String, Object> mapData = new HashMap<>();
                mapData.put("total_sales", totalSales);
                mapData.put("total_sales_month_comparison", totalSalesMonthComparison);
                mapData.put("total_sales_day_comparison", totalSalesDayComparison);
                mapData.put("total_order_quantity", totalOrderQuantity);
                mapData.put("customer_order_avg", customerOrderAvg);
                mapData.put("customer_refund_rate", customerRefundRate);
                mapData.put("registered_user_cnt", registeredUserCnt);
                mapData.put("order_quantity_1d", orderQuantity1d);
                mapData.put("order_quantity_home_care", orderQuantityHomeCare);
                mapData.put("order_quantity_rehabilitation_assistance", orderQuantityRehabilitationAssistance);
                mapData.put("order_quantity_hospitalization", orderQuantityHospitalization);
                mapData.put("order_quantity_suitable_products", orderQuantitySuitableProducts);
                mapData.put("order_quantity_accompany_general", orderQuantityAccompanyGeneral);
                mapData.put("order_quantity_accompany_errand", orderQuantityAccompanyErrand);
                mapData.put("order_quantity_housekeeping", orderQuantityHousekeeping);
                mapData.put("order_quantity_adaptive_age", orderQuantityAdaptiveAge);
                mapData.put("order_quantity_health_care", orderQuantityHealthCare);
                mapData.put("registration_conversion_rate", registrationConversionRate);
                mapData.put("user_retention_rate", userRetentionRate);
                mapData.put("shopping_cart_conversion_rate", shoppingCartConversionRate);
                mapData.put("district_type_name", name);
                mapData.put("district_type_id", id);
                mapData.put("level", level);
                mapData.put("ds", ds);
                lstData.add(mapData);
            }

            String insertSql = getMaxComputeInsertListSQlNonePartition(lstData, table);
//            System.out.println(insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
//            break;
        }
    }

    private void statisticsDwsCustomerScanDiData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/customer-scan-dws.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_customer_scan_di");
        for (Map<String, Object> mapData : lstData) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsDwdWorkerLoadLongOrderDiData() {
        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 11, 4),
                LocalDate.of(2024, 11, 5),
                LocalDate.of(2024, 11, 6),
                LocalDate.of(2024, 11, 7),
                LocalDate.of(2024, 11, 8),
                LocalDate.of(2024, 11, 9),
                LocalDate.of(2024, 11, 11),
                LocalDate.of(2024, 11, 11)
        );

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_worker_load_short_order_di");
        for (int i = 0; i < 50; i++) {
            Long utcCreated = System.currentTimeMillis() / 1000;
            Integer weekNum = GenUtil.random(1, 7);
            Integer serviceTime = getLoadServiceTime();

            LocalDate date = lstDate.get(GenUtil.random(0, lstDate.size() - 1));
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("service_time", serviceTime);
            mapData.put("utc_created", utcCreated);
            mapData.put("week_num", weekNum);
            mapData.put("job_id", i);
            mapData.put("ds", ds);

            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println(insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private int getLoadServiceTime() {
        int jobId = GenUtil.random(1, 3);
        switch (jobId) {
            case 1:
                return GenUtil.random(1, 3) * 3 * 60 * 60;
            case 2:
                return GenUtil.random(4, 6) * 3 * 60 * 60;
            case 3:
                return GenUtil.random(7, 9) * 3 * 60 * 60;
        }
        return 0;
    }

    private void statisticsDwdWorkerLoadDistributeOrderData() {
        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 11, 4),
                LocalDate.of(2024, 11, 5),
                LocalDate.of(2024, 11, 6),
                LocalDate.of(2024, 11, 7),
                LocalDate.of(2024, 11, 8),
                LocalDate.of(2024, 11, 9),
                LocalDate.of(2024, 11, 11),
                LocalDate.of(2024, 11, 11)
        );

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_worker_load_distribute_order_di");
        for (int i = 0; i < 100; i++) {
            try {
                Integer overallJob = getJobNum();
                TimeUnit.SECONDS.sleep(1);
                Integer shortJob = getJobNum();
                TimeUnit.SECONDS.sleep(1);
                Integer longJob = getJobNum();

                LocalDate date = lstDate.get(GenUtil.random(0, lstDate.size() - 1));
                String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

                Map<String, Object> mapData = new HashMap<>();
                mapData.put("worker_id", i);
                mapData.put("worker_name", i + "");
                mapData.put("overall_job_orders_2m", overallJob);
                mapData.put("short_job_orders_1m", shortJob);
                mapData.put("long_job_orders_3m", longJob);
                mapData.put("ds", ds);

                String insertSql = getMaxComputeInsertSQl(mapData, table);
                System.out.println(insertSql);
                srcDataInsert(preDatabaseMaxCompute, insertSql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getJobNum() {
        int jobId = GenUtil.random(1, 5);
        switch (jobId) {
            case 1:
                return GenUtil.random(1, 2);
            case 2:
                return GenUtil.random(3, 5);
            case 3:
                return GenUtil.random(6, 8);
            case 4:
                return GenUtil.random(9, 10);
            case 5:
                return GenUtil.random(11, 15);
        }
        return 0;
    }

    private void statisticsDwdWorkerPortraitInfoData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/worker-portrait-info-dwd.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_worker_portrait_info_di");
        for (Map<String, Object> mapData : lstData) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsOdsBrowsePathInfoData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/customer-page-visit.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("ods_browse_path_info");
        for (Map<String, Object> mapData : lstData) {
            String pageLink = (String) mapData.get("page_link");
            String pageName = (String) mapData.get("page_name");

            Map<String, Object> sqlData = new HashMap<>();
            sqlData.put("page_link", pageLink);
            sqlData.put("page_name", pageName);
            sqlData.put("page_group_link", pageLink);
            sqlData.put("page_group_name", pageName);
            sqlData.put("ds", "20241024");

            String insertSql = getMaxComputeInsertSQl(sqlData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsDwdCustomerScanDiData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/customer-page-visit.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        List<String> lstDs = lstData.stream()
                .map(po -> GenUtil.objToStr(po.get("ds").toString()))
                .collect(Collectors.toList());

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_customer_scan_di");
        for (int i = 0; i < lstData.size(); i++) {
            String pageLink = (String) lstData.get(i).get("page_link");
            String pageName = (String) lstData.get(i).get("page_name");
            Integer visitCnt = (Integer) lstData.get(i).get("visit_cnt");
            Integer customerCnt = (Integer) lstData.get(i).get("customer_cnt");
            Integer stayTime = (Integer) lstData.get(i).get("stay_time");
            Double jumpOutRate = (Double) lstData.get(i).get("jump_out_rate");
            String ds = lstDs.get(i);

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("page_link", pageLink);
            mapData.put("page_name", pageName);
            mapData.put("visit_cnt", visitCnt);
            mapData.put("customer_cnt", customerCnt);
            mapData.put("jump_out_rate", jumpOutRate);
            mapData.put("stay_time", stayTime);
            mapData.put("ds", ds);

            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }

    }

    private void statisticsDwsCustomerBrowsingBehaviorStatisticsData() {
        List<Map<String, Object>> datas = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/customer-page-visit-latest.csv");
        for (Map<String, String> data : csvData) {
            datas.add(getMapData(data));
        }

        List<Map<String, Object>> lstData = datas.stream()
                .filter(po -> !(Objects.equals(po.get("page_name"), "启动") || Objects.equals(po.get("page_name"), "退出"))).collect(Collectors.toList());

        List<Integer> lstUserCnt = lstData.stream()
                .map(po -> GenUtil.strToInteger(po.get("user_cnt").toString()))
                .collect(Collectors.toList());

        List<String> lstDs = lstData.stream()
                .map(po -> GenUtil.objToStr(po.get("ds").toString()))
                .collect(Collectors.toList());

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_customer_browsing_behavior_statistics_di");
        for (int num = 0; num < lstData.size(); num++) {
            int deep = GenUtil.random(10, 30);
            int dsIndex = lstData.size() - 1 - num;
            List<Integer> lstIndex = new ArrayList<>();
            List<Map<String, Object>> lstLinkData = new ArrayList<>();
            for (int i = 0; i < deep; i++) {
                int preIndex = lstIndex.isEmpty() ? 0 : lstIndex.get(lstIndex.size() - 1);
                int curIndex = GenUtil.random(1, lstData.size()) - 1;
                if (i != deep - 1 && curIndex == preIndex) {
                    do {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        curIndex = GenUtil.random(1, lstData.size()) - 1;
                    } while (curIndex == preIndex);
                }
                lstIndex.add(curIndex);

                Map<String, Object> preData = i == 0 ? datas.get(datas.size() - 2) : lstData.get(preIndex);
                Map<String, Object> curData = i == deep - 1 ? datas.get(datas.size() - 1) : lstData.get(curIndex);

                String prePageLink = (String) preData.get("page_link");
                String curPageLink = (String) curData.get("page_link");

                String prePageName = (String) preData.get("page_name");
                String curPageName = (String) curData.get("page_name");

                int userCntIndex = GenUtil.random(0, lstData.size() - 1);

                String ds = lstDs.get(dsIndex);
                int userCnt = lstUserCnt.get(userCntIndex);
                double trafficWeight = GenUtil.randomDouble(0.1000, 0.9999);

                BigDecimal originalValue = new BigDecimal(trafficWeight);
                BigDecimal trafficWeightRoundedValue = originalValue.setScale(4, RoundingMode.HALF_UP);

                Map<String, Object> mapData = new HashMap<>();
                mapData.put("pre_page_link", prePageLink);
                mapData.put("cur_page_link", curPageLink);
                mapData.put("pre_page_link_name", prePageName);
                mapData.put("cur_page_link_name", curPageName);
                mapData.put("user_cnt", userCnt);
                mapData.put("traffic_weight", trafficWeightRoundedValue);
                mapData.put("ds", ds);

                lstLinkData.add(mapData);
            }

            String insertSql = getMaxComputeInsertListSQl(lstLinkData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsDwdCustomerPageVisitData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/customer-page-visit.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_customer_page_visit_di");
        for (Map<String, Object> mapData : lstData) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsWorkerBaseInfoDwsData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/worker-base-info-dws.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_worker_base_info_statistics_di");
        for (Map<String, Object> mapData : lstData) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsWorkerEvaluateDwdData() {
        List<Long> workerIds = getWorkerIds();

        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 10, 1),
                LocalDate.of(2024, 10, 2),
                LocalDate.of(2024, 10, 3),
                LocalDate.of(2024, 10, 4),
                LocalDate.of(2024, 10, 5),
                LocalDate.of(2024, 10, 6),
                LocalDate.of(2024, 10, 7),
                LocalDate.of(2024, 10, 8),
                LocalDate.of(2024, 10, 9),
                LocalDate.of(2024, 10, 10),
                LocalDate.of(2024, 10, 11)
        );

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_worker_evaluate_di");
        for (int i = 0; i < 300; i++) {
            int workerIdIndex = GenUtil.random(0, workerIds.size() - 1);

            Long workerId = workerIds.get(workerIdIndex);
            Double rating_star = GenUtil.round(GenUtil.randomDouble(0, 5), 2);
            Integer workerType = GenUtil.random(1, 2);

            LocalDate date = lstDate.get(GenUtil.random(0, lstDate.size() - 1));
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("worker_id", workerId);
            mapData.put("rating_star", rating_star);
            mapData.put("worker_type", workerType);
            mapData.put("ds", ds);

            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println(insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }

    }

    private void statisticsWorkerShopDwdData() {
        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 10, 1),
                LocalDate.of(2024, 10, 2),
                LocalDate.of(2024, 10, 3),
                LocalDate.of(2024, 10, 4),
                LocalDate.of(2024, 10, 5),
                LocalDate.of(2024, 10, 6),
                LocalDate.of(2024, 10, 7),
                LocalDate.of(2024, 10, 8),
                LocalDate.of(2024, 10, 9),
                LocalDate.of(2024, 10, 10),
                LocalDate.of(2024, 10, 11)
        );

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_worker_shop_di");
        String workerShopInfoPath = "C:\\Users\\Admin\\Desktop\\worker-shop-info.json";
        String workerShopInfoContent = FileUtil.read(workerShopInfoPath);
        List<Map<String, Object>> lstWorkerShopInfo = GenUtil.fromJsonString(workerShopInfoContent, List.class);
        for (Map<String, Object> mapWorkerInfo : lstWorkerShopInfo) {
            LocalDate date = lstDate.get(GenUtil.random(0, lstDate.size() - 1));
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");
            mapWorkerInfo.put("ds", ds);

            String insertSql = getMaxComputeInsertSQl(mapWorkerInfo, table);
            System.out.println(insertSql);
            System.out.println("------------------------------------------------------------");
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    public void statisticsWorkerInfoDwdData() {
        List<Long> workerIds = getWorkerIds();

        LogUtil.loggerLine(Log.of("MaxComputeStatisticsInitService", "statisticsWorkerInfoDwdData", "workerIds", workerIds));

        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 10, 1),
                LocalDate.of(2024, 10, 2),
                LocalDate.of(2024, 10, 3),
                LocalDate.of(2024, 10, 4),
                LocalDate.of(2024, 10, 5),
                LocalDate.of(2024, 10, 6),
                LocalDate.of(2024, 10, 7),
                LocalDate.of(2024, 10, 8),
                LocalDate.of(2024, 10, 9),
                LocalDate.of(2024, 10, 10),
                LocalDate.of(2024, 10, 11)
        );

        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_worker_info_di");
        for (Long workerId : workerIds) {
            int serviceTimeRank = GenUtil.random(1, 4);
            int serviceAgeRank = GenUtil.random(1, 4);
            int ageRank = GenUtil.random(1, 5);

            Integer age = getWorkerAge(ageRank);
            Integer gender = GenUtil.random(1, 3);
            Integer serviceAge = getWorkerServiceAge(serviceAgeRank);
            Integer serviceTime = getWorkerServiceTime(serviceTimeRank);

            LocalDate date = lstDate.get(GenUtil.random(0, lstDate.size() - 1));
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("service_time", serviceTime);
            mapData.put("service_age", serviceAge);
            mapData.put("worker_id", workerId);
            mapData.put("gender", gender);
            mapData.put("age", age);
            mapData.put("ds", ds);

            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println(insertSql);
            System.out.println("------------------------------------------------------------");
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private Integer getWorkerServiceTime(Integer rank) {
        switch (rank) {
            case 1:
                return GenUtil.random(501, 600);
            case 2:
                return GenUtil.random(101, 500);
            case 3:
                return GenUtil.random(51, 100);
            case 4:
                return GenUtil.random(1, 50);
        }
        return 0;
    }

    private Integer getWorkerServiceAge(Integer rank) {
        switch (rank) {
            case 1:
                return GenUtil.random(21, 40);
            case 2:
                return GenUtil.random(11, 20);
            case 3:
                return GenUtil.random(5, 10);
            case 4:
                return GenUtil.random(1, 4);
        }
        return 0;
    }

    private Integer getWorkerAge(Integer rank) {
        switch (rank) {
            case 1:
                return GenUtil.random(61, 75);
            case 2:
                return GenUtil.random(51, 60);
            case 3:
                return GenUtil.random(41, 50);
            case 4:
                return GenUtil.random(31, 40);
            case 5:
                return GenUtil.random(18, 30);
        }
        return 0;
    }

    private List<Long> getWorkerIds() {
        String workerIdsPath = "C:\\Users\\Admin\\Desktop\\worker-ids.json";
        String workerIdsContent = FileUtil.read(workerIdsPath);
        List<Map<String, Object>> lstWorkerIdData = GenUtil.fromJsonString(workerIdsContent, List.class);
        return lstWorkerIdData.stream().map(po -> (Long) po.get("id")).collect(Collectors.toList());
    }

    private void statisticsMerchantDwdData() {
        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_merchant_review_di");
        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 11, 2),
                LocalDate.of(2024, 11, 3),
                LocalDate.of(2024, 11, 4),
                LocalDate.of(2024, 11, 5),
                LocalDate.of(2024, 11, 6),
                LocalDate.of(2024, 11, 7),
                LocalDate.of(2024, 11, 8),
                LocalDate.of(2024, 11, 9),
                LocalDate.of(2024, 11, 10),
                LocalDate.of(2024, 11, 11)
        );
        List<Integer> seconds = Arrays.asList(
                0,
                24 * 60 * 60 - 1,
                2 * 24 * 60 * 60 - 1,
                3 * 24 * 60 * 60 - 1
        );
        for (int i = 0; i < 300; i++) {
            int review_submit_cnt = GenUtil.random(1, 12);
            int review_fail_type = GenUtil.random(1, 6);

            int index = GenUtil.random(0, seconds.size() - 1);
            Integer startSeconds = seconds.get(index);
            int endSeconds = startSeconds + 24 * 60 * 60;
            Integer review_time = GenUtil.random(startSeconds, endSeconds);

            LocalDate date = lstDate.get(GenUtil.random(0, lstDate.size() - 1));
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("review_submit_cnt", review_submit_cnt);
            mapData.put("review_fail_type", review_fail_type);
            mapData.put("review_time", review_time);
            mapData.put("ds", ds);

            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println(insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsMerchantDwsData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/merchant-registered-dws.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_registered_merchant_statistics_di");
        for (Map<String, Object> mapData : lstData) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsDwdCustomerScanDeepDiData() {
        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_customer_scan_deep_di");
        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 11, 16),
                LocalDate.of(2024, 11, 17),
                LocalDate.of(2024, 11, 18),
                LocalDate.of(2024, 11, 19),
                LocalDate.of(2024, 11, 20),
                LocalDate.of(2024, 11, 21),
                LocalDate.of(2024, 11, 22),
                LocalDate.of(2024, 11, 23),
                LocalDate.of(2024, 11, 24)
        );
        for (int i = 0; i < 300; i++) {
            int hour = GenUtil.random(0, 23);
            int minute = GenUtil.random(0, 59);
            String hourStr = hour < 10 ? "0" + hour : "" + hour;
            String minuteStr = minute < 10 ? "0" + minute : "" + minute;

            LocalDate date = lstDate.get(GenUtil.random(0, lstDate.size() - 1));
            String dateStr = GenUtil.localDateToStr(date);

            String login_time_str = String.format("%s %s:%s:%s", dateStr, hourStr, minuteStr, "00");
            System.out.println(login_time_str);
            Long scanTime = GenUtil.strToTimestamp(login_time_str);

            Integer os_type = GenUtil.random(1, 2);
            int scanIndex = GenUtil.random(1, 5);
            Integer scanDeep = getScanDeep(scanIndex);

            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("customer_id", i);
            mapData.put("scan_time", scanTime);
            mapData.put("scan_deep", scanDeep);
            mapData.put("os_type", os_type);
            mapData.put("ds", ds);

            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println(insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private int getScanDeep(int index) {
        switch (index) {
            case 1:
                return GenUtil.random(1, 3);
            case 2:
                return GenUtil.random(4, 8);
            case 3:
                return GenUtil.random(9, 15);
            case 4:
                return GenUtil.random(16, 20);
            case 5:
                return GenUtil.random(21, 25);
            default:
                return 0;
        }
    }

    private void statisticsLoginDwdData() {
        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_customer_login_di");
        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 9, 10),
                LocalDate.of(2024, 9, 11),
                LocalDate.of(2024, 9, 12),
                LocalDate.of(2024, 9, 13),
                LocalDate.of(2024, 9, 14),
                LocalDate.of(2024, 9, 15),
                LocalDate.of(2024, 9, 16),
                LocalDate.of(2024, 9, 17),
                LocalDate.of(2024, 9, 18)
        );
        for (int i = 0; i < 300; i++) {
            int hour = GenUtil.random(0, 23);
            int minute = GenUtil.random(0, 59);
            String hourStr = hour < 10 ? "0" + hour : "" + hour;
            String minuteStr = minute < 10 ? "0" + minute : "" + minute;

            LocalDate date = lstDate.get(GenUtil.random(0, lstDate.size() - 1));
            String dateStr = GenUtil.localDateToStr(date);

            String login_time_str = String.format("%s %s:%s:%s", dateStr, hourStr, minuteStr, "00");
            System.out.println(login_time_str);
            Long login_time = GenUtil.strToTimestamp(login_time_str);

            Integer os_type = GenUtil.random(1, 2);

            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("login_time", login_time);
            mapData.put("os_type", os_type);
            mapData.put("ds", ds);

            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println(insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsEvaluateDwdData() {
        Table table = preDatabaseMaxCompute.getMapTable().get("dwd_customer_evaluate_di");
        List<LocalDate> lstDate = Arrays.asList(
                LocalDate.of(2024, 9, 10),
                LocalDate.of(2024, 9, 11),
                LocalDate.of(2024, 9, 12),
                LocalDate.of(2024, 9, 13),
                LocalDate.of(2024, 9, 14),
                LocalDate.of(2024, 9, 15),
                LocalDate.of(2024, 9, 16),
                LocalDate.of(2024, 9, 17),
                LocalDate.of(2024, 9, 18)
        );
        for (int i = 0; i < 300; i++) {
            Integer evaluation_length = GenUtil.random(50, 225);
            Double rating_star = GenUtil.round(GenUtil.randomDouble(0, 5), 2);
            Integer review_content_type = GenUtil.random(1, 2);
            Integer evaluation_user_type = GenUtil.random(1, 3);
            LocalDate date = lstDate.get(GenUtil.random(0, lstDate.size() - 1));
            String ds = GenUtil.localDateToStr(date, "yyyyMMdd");

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("evaluation_length", evaluation_length);
            mapData.put("rating_star", rating_star);
            mapData.put("review_content_type", review_content_type);
            mapData.put("evaluation_user_type", evaluation_user_type);
            mapData.put("ds", ds);

            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println(insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsOverallCustomerAdsDwsData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/overall-customer.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("ads_overall_customer_data_trends_di");
        for (Map<String, Object> mapData : lstData) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsEvaluateDwsData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/customer-evaluate-dws.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_customer_evaluate_statistics_di");
        for (Map<String, Object> mapData : lstData) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private void statisticsLoginDwsData() {
        List<Map<String, Object>> lstData = new ArrayList<>();
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/customer-login.csv");
        for (Map<String, String> data : csvData) {
            lstData.add(getMapData(data));
        }

        LocalDate endDate = LocalDate.now();
        List<Map<String, Object>> lstStatistics = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2024, 9, 11);
        for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
            lstStatistics.add(statisticsLoginDwsData(lstData, date));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_customer_login_statistics_di");
        for (Map<String, Object> statisticsData : lstStatistics) {
            String insertSql = getMaxComputeInsertSQl(statisticsData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }

    }


    private Map<String, Object> statisticsLoginDwsData(List<Map<String, Object>> lstData, LocalDate date) {
        Integer login_customer_cnt_1d = lstData.stream().filter(po -> getLocalDate(po).equals(date.minusDays(1))).mapToInt(po -> GenUtil.objToInteger(po.get("login_customer_cnt"))).sum();
        Integer login_customer_cnt_1d_miniprogram = lstData.stream().filter(po -> getLocalDate(po).equals(date.minusDays(1))).mapToInt(po -> GenUtil.objToInteger(po.get("login_customer_miniprogram_cnt"))).sum();
        Integer login_customer_cnt_1d_app = lstData.stream().filter(po -> getLocalDate(po).equals(date.minusDays(1))).mapToInt(po -> GenUtil.objToInteger(po.get("login_customer_app_cnt"))).sum();
        Integer login_customer_cnt_3d = lstData.stream().filter(po -> date.minusDays(4).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("login_customer_cnt"))).sum();
        Integer login_customer_cnt_1w = lstData.stream().filter(po -> date.minusDays(8).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("login_customer_cnt"))).sum();
        Integer login_customer_cnt_1m = lstData.stream().filter(po -> date.minusDays(31).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("login_customer_cnt"))).sum();

        Integer active_customer_cnt_1d = lstData.stream().filter(po -> date.minusDays(2).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("active_customer_cnt"))).sum();
        Integer active_customer_cnt_3d = lstData.stream().filter(po -> date.minusDays(4).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("active_customer_cnt"))).sum();
        Integer active_customer_cnt_1w = lstData.stream().filter(po -> date.minusDays(8).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("active_customer_cnt"))).sum();
        Integer active_customer_cnt_1m = lstData.stream().filter(po -> date.minusDays(31).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("active_customer_cnt"))).sum();

        double login_customer_percent_3d = getPercentData(date, 3, lstData, login_customer_cnt_3d);
        double login_customer_percent_1w = getPercentData(date, 7, lstData, login_customer_cnt_1w);
        double login_customer_percent_1m = getPercentData(date, 30, lstData, login_customer_cnt_1m);

        double active_customer_percent_3d = getPercentData(date, 3, lstData, active_customer_cnt_3d);
        double active_customer_percent_1w = getPercentData(date, 7, lstData, active_customer_cnt_1w);
        double active_customer_percent_1m = getPercentData(date, 30, lstData, active_customer_cnt_1m);

        double login_customer_retention_rate_1d = getPercentData(date, 1, lstData);
        double login_customer_retention_rate_1w = getPercentData(date, 3, lstData);
        double login_customer_retention_rate_1m = getPercentData(date, 7, lstData);

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("login_customer_cnt_1d", login_customer_cnt_1d);
        mapData.put("login_customer_cnt_1d_miniprogram", login_customer_cnt_1d_miniprogram);
        mapData.put("login_customer_cnt_1d_app", login_customer_cnt_1d_app);
        mapData.put("active_customer_cnt_1d", active_customer_cnt_1d);
        mapData.put("active_customer_cnt_1w", active_customer_cnt_1w);
        mapData.put("active_customer_cnt_1m", active_customer_cnt_1m);
        mapData.put("login_customer_percent_3d", login_customer_percent_3d);
        mapData.put("login_customer_percent_1w", login_customer_percent_1w);
        mapData.put("login_customer_percent_1m", login_customer_percent_1m);
        mapData.put("active_customer_percent_3d", active_customer_percent_3d);
        mapData.put("active_customer_percent_1w", active_customer_percent_1w);
        mapData.put("active_customer_percent_1m", active_customer_percent_1m);
        mapData.put("login_customer_retention_rate_1d", login_customer_retention_rate_1d);
        mapData.put("login_customer_retention_rate_1w", login_customer_retention_rate_1w);
        mapData.put("login_customer_retention_rate_1m", login_customer_retention_rate_1m);
        mapData.put("ds", GenUtil.localDateToStr(date, "yyyyMMdd"));
        return mapData;
    }

    private Double getPercentData(LocalDate date, Integer dateNum, List<Map<String, Object>> lstData) {
        Integer customer_login_cnt = lstData.stream().filter(po -> date.minusDays(dateNum).minusDays(1).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("login_customer_cnt"))).sum();
        Integer registered_customer_cnt = lstData.stream().filter(po -> date.minusDays(dateNum).minusDays(1).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_cnt"))).sum();
        return GenUtil.round(customer_login_cnt / registered_customer_cnt.doubleValue(), 2);
    }

    private Double getPercentData(LocalDate date, Integer dateNum, List<Map<String, Object>> lstData, Integer loginNumber) {
        Integer registered_customer_cnt = lstData.stream().filter(po -> date.minusDays(dateNum).minusDays(1).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_cnt"))).sum();
        return GenUtil.round(loginNumber / registered_customer_cnt.doubleValue(), 2);
    }

    private void statisticsLoginData() {
        Table table = preDatabaseMaxCompute.getMapTable().get("dws_customer_registration_atomic_di");
        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/customer-registration.csv");
//        List<Map<String, String>> csvData = CsvUtil.toMap("/csv/max-compute/statistics/customer-login.csv");
        for (Map<String, String> data : csvData) {
            String insertSql = getMaxComputeInsertSQl(getMapData(data), table);
            System.out.println(insertSql);

            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
    }

    private Map<String, Object> getMapData(Map<String, String> mapData) {
        Map<String, Object> mapCsvData = new HashMap<>();
        for (Map.Entry<String, String> map : mapData.entrySet()) {
            if (Objects.equals(map.getKey(), "ds")) {
                mapCsvData.put(map.getKey(), map.getValue());
                continue;
            }
            if (map.getKey().contains("rate")) {
                mapCsvData.put(map.getKey(), Double.parseDouble(map.getValue()));
                continue;
            }
            if (map.getKey().contains("cnt")) {
                mapCsvData.put(map.getKey(), Integer.parseInt(map.getValue()));
                continue;
            }
            Integer value = GenUtil.strToInteger(map.getValue());
            mapCsvData.put(map.getKey(), value == null ? map.getValue() : value);
        }
        return mapCsvData;
    }

    private void statisticsRegistrationData() {
//        Table table = preDatabaseMaxCompute.getMapTable().get("dws_customer_registration_atomic_di");
//        List<Map<String, Object>> lstData = srcDataList(preDatabaseMaxCompute, table);
//        FileUtil.write("C:\\Users\\Admin\\Desktop\\table-data (2).json",
//                GenUtil.toJsonString(lstData)
//        );
        LocalDate endDate = LocalDate.now();
        List<Map<String, Object>> lstStatistics = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2024, 9, 11);
        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\table-data (2).json");
        List<Map<String, Object>> lstData = GenUtil.fromJsonString(content, List.class);
        for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
            lstStatistics.add(statisticsData(lstData, date));
        }

        Table table = preDatabaseMaxCompute.getMapTable().get("dws_customer_registration_statistics_di");
        for (Map<String, Object> mapData : lstStatistics) {
            String insertSql = getMaxComputeInsertSQl(mapData, table);
            System.out.println("insertSql: " + insertSql);
            srcDataInsert(preDatabaseMaxCompute, insertSql);
        }
        FileUtil.write("C:\\Users\\Admin\\Desktop\\des-table-data.json",
                GenUtil.toJsonString(lstStatistics)
        );
    }

    private Map<String, Object> statisticsData(List<Map<String, Object>> lstData, LocalDate date) {
        Integer registered_customer_total_cnt = lstData.stream().filter(po -> getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_cnt"))).sum();

        double registered_customer_cnt_avg_1w = lstData.stream().filter(po -> date.minusWeeks(1).minusDays(1).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_cnt"))).sum() / 7D;
        registered_customer_cnt_avg_1w = GenUtil.round(registered_customer_cnt_avg_1w, 2);

        Integer registered_customer_cnt_1d_miniprogram = lstData.stream().filter(po -> getLocalDate(po).equals(date.minusDays(1))).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_miniprogram_cnt"))).sum();
        Integer registered_customer_cnt_1d_app = lstData.stream().filter(po -> getLocalDate(po).equals(date.minusDays(1))).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_app_cnt"))).sum();
        Integer registered_customer_cnt_1d = registered_customer_cnt_1d_miniprogram + registered_customer_cnt_1d_app;

        double registered_customer_retention_rate_1d = getCustomerRetentionRate(date, 1, lstData);
        double registered_customer_retention_rate_3d = getCustomerRetentionRate(date, 3, lstData);
        double registered_customer_retention_rate_1w = getCustomerRetentionRate(date, 7, lstData);
        double registered_customer_retention_rate_10d = getCustomerRetentionRate(date, 10, lstData);
        double registered_customer_retention_rate_2w = getCustomerRetentionRate(date, 14, lstData);
        double registered_customer_retention_rate_1m = getCustomerRetentionRate(date, 30, lstData);
        double registered_customer_retention_rate_2m = getCustomerRetentionRate(date, 60, lstData);

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("registered_customer_total_cnt", registered_customer_total_cnt);
        mapData.put("registered_customer_cnt_avg_1w", registered_customer_cnt_avg_1w);
        mapData.put("registered_customer_cnt_1d", registered_customer_cnt_1d);
        mapData.put("registered_customer_cnt_1d_miniprogram", registered_customer_cnt_1d_miniprogram);
        mapData.put("registered_customer_cnt_1d_app", registered_customer_cnt_1d_app);
        mapData.put("registered_customer_retention_rate_1d", registered_customer_retention_rate_1d);
        mapData.put("registered_customer_retention_rate_3d", registered_customer_retention_rate_3d);
        mapData.put("registered_customer_retention_rate_1w", registered_customer_retention_rate_1w);
        mapData.put("registered_customer_retention_rate_10d", registered_customer_retention_rate_10d);
        mapData.put("registered_customer_retention_rate_2w", registered_customer_retention_rate_2w);
        mapData.put("registered_customer_retention_rate_1m", registered_customer_retention_rate_1m);
        mapData.put("registered_customer_retention_rate_2m", registered_customer_retention_rate_2m);
        mapData.put("ds", GenUtil.localDateToStr(date, "yyyyMMdd"));
        return mapData;
    }

    private double getCustomerRetentionRate(LocalDate date, Integer dateNum, List<Map<String, Object>> lstData) {
        Integer registered_customer_login_cnt = lstData.stream().filter(po -> date.minusDays(dateNum).minusDays(1).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_login_cnt"))).sum();
        Integer registered_customer_cnt = lstData.stream().filter(po -> date.minusDays(dateNum).minusDays(1).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_cnt"))).sum();
        return GenUtil.round(registered_customer_login_cnt / registered_customer_cnt.doubleValue(), 2);
    }

    private LocalDate getLocalDate(Map<String, Object> mapData) {
        String ds = mapData.get("ds").toString();
        return GenUtil.strToLocalDate(ds, "yyyyMMdd");
    }

}
