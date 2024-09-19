package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaxComputeStatisticsInitService extends BaseService {

    private final boolean enable;

    public MaxComputeStatisticsInitService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> maxComputeAssignment = GenUtil.getMap("max-compute-statistics-init");
        this.enable = GenUtil.objToBoolean(maxComputeAssignment.get("enable"));
    }

    public void apply() {
        if (!enable) return;

        statisticsRegistrationData();
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
        Integer registered_customer_total_cnt = lstData.stream().filter(po -> !getLocalDate(po).isAfter(date)).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_cnt"))).sum();

        double registered_customer_cnt_avg_1w = lstData.stream().filter(po -> date.minusWeeks(1).minusDays(1).isBefore(getLocalDate(po)) && getLocalDate(po).isBefore(date)).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_cnt"))).sum() / 7D;
        registered_customer_cnt_avg_1w = GenUtil.round(registered_customer_cnt_avg_1w, 2);

        Integer registered_customer_cnt_1d = lstData.stream().filter(po -> getLocalDate(po).equals(date.minusDays(1))).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_cnt"))).sum();
        Integer registered_customer_cnt_1d_miniprogram = lstData.stream().filter(po -> getLocalDate(po).equals(date.minusDays(1))).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_miniprogram_cnt"))).sum();
        Integer registered_customer_cnt_1d_app = lstData.stream().filter(po -> getLocalDate(po).equals(date.minusDays(1))).mapToInt(po -> GenUtil.objToInteger(po.get("registered_customer_app_cnt"))).sum();

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
