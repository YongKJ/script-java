package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.util.CsvUtil;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;

import java.time.LocalDate;
import java.util.*;

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
        statisticsLoginDwdData();
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
            mapCsvData.put(map.getKey(), GenUtil.strToInteger(map.getValue()));
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
