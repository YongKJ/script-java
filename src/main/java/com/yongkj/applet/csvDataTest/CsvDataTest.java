package com.yongkj.applet.csvDataTest;

import com.yongkj.pojo.dto.Log;
import com.yongkj.util.CsvUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.util.*;

public class CsvDataTest {

    private final String csvReadPath;
    private final String csvWritePath;

    private CsvDataTest() {
        this.csvReadPath = GenUtil.getValue("csv-read-path");
        this.csvWritePath = GenUtil.getValue("csv-write-path");
    }

    private void apply() {
//        readTestOne();
        writeTestThree();
//        writeTestTwo();
//        writeTestOne();
    }

    private void readTestOne() {
        List<String> lstHeader = CsvUtil.getHeaders(csvReadPath);
        LogUtil.loggerLine(Log.of("CsvDataTest", "readTestOne", "lstHeader", lstHeader));
        List<CSVRecord> lstRecord = CsvUtil.getRecords(csvReadPath);
        LogUtil.loggerLine(Log.of("CsvDataTest", "readTestOne", "lstRecord", lstRecord));
        List<Map<String, String>> lstData = CsvUtil.toMap(csvReadPath);
        LogUtil.loggerLine(Log.of("CsvDataTest", "readTestOne", "lstData", lstData));
    }

    private void writeTestThree() {
        CsvUtil.csvToExcel(csvReadPath, 1);
    }

    private void writeTestTwo() {
        String[] headers = new String[]{"序号", "书名", "作者", "年代", "字数"};
        List<Map<String, String>> lstData = getMapData(Arrays.asList(headers));
        CsvUtil.printRecords(csvReadPath, lstData, headers);
        CsvUtil.printRecords(csvWritePath, lstData, headers);
    }

    private void writeTestOne() {
        String path = "C:\\Users\\admin\\Desktop\\read-demo-test-" + System.currentTimeMillis() + ".csv";
        String[] headers = new String[]{"序号", "书名", "作者", "年代", "字数"};
        List<Map<String, String>> lstData = getMapData(Arrays.asList(headers));
        CSVPrinter printer = CsvUtil.getPrinter(path, headers);
        CsvUtil.printRecords(printer, lstData, headers);
    }

    private List<Map<String, String>> getMapData(List<String> lstHeader) {
        List<List<String>> lstData = getListData();
        List<Map<String, String>> tempLstData = new ArrayList<>();
        for (List<String> data : lstData) {
            Map<String, String> mapData = new HashMap<>();
            for (int i = 0; i < lstHeader.size(); i++) {
                String header = lstHeader.get(i);
                String value = data.get(i);
                mapData.put(header, value);
            }
            tempLstData.add(mapData);
        }
        return tempLstData;
    }

    private List<List<String>> getListData() {
        List<List<String>> lstData = new ArrayList<>();
        lstData.add(Arrays.asList("1", "《水浒传》", "施耐庵", "宋朝", "96 万字"));
        lstData.add(Arrays.asList("2", "《三国演义》", "罗贯中", "元朝", "73.4 万字"));
        lstData.add(Arrays.asList("3", "《西游记》", "吴承恩", "明代", "82 万字"));
        lstData.add(Arrays.asList("4", "《红楼梦》", "曹雪芹", "清代", "107.5 万字"));
        lstData.add(Arrays.asList("5", "《聊斋志异》", "蒲松龄", "清代", "70.8 万字"));
        return lstData;
    }

    public static void run(String[] args) {
        new CsvDataTest().apply();
    }

}
