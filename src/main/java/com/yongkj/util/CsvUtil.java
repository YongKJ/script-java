package com.yongkj.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

public class CsvUtil {

    private CsvUtil() {
    }

    public static List<String> getHeaders(String fileName) {
        try {
            InputStream stream = Arrays.asList("/", "\\").contains(fileName.substring(0, 1)) ?
                    new ClassPathResource(fileName).getInputStream() :
                    new FileInputStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            return CSVFormat.Builder.create()
                    .setSkipHeaderRecord(true)
                    .setHeader().build()
                    .parse(reader)
                    .getHeaderNames();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<CSVRecord> getRecords(String fileName) {
        if (!StringUtils.hasText(fileName)) return new ArrayList<>();
        try {
            InputStream stream = Arrays.asList("/", "\\").contains(fileName.substring(0, 1)) ?
                    new ClassPathResource(fileName).getInputStream() :
                    new FileInputStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            return CSVFormat.Builder.create()
                    .setSkipHeaderRecord(true)
                    .setHeader().build()
                    .parse(reader)
                    .getRecords();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<Map<String, String>> toMap(String fileName) {
        if (!StringUtils.hasText(fileName)) return new ArrayList<>();
        try {
            InputStream stream = Arrays.asList("/", "\\").contains(fileName.substring(0, 1)) ?
                    new ClassPathResource(fileName).getInputStream() :
                    new FileInputStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            List<String> lstHeader = getHeaders(fileName);
            List<CSVRecord> lstRecord = CSVFormat.Builder.create()
                    .setSkipHeaderRecord(true)
                    .setHeader().build()
                    .parse(reader)
                    .getRecords();
            return toMap(lstRecord, lstHeader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<Map<String, String>> toMap(List<CSVRecord> lstRecord, List<String> lstHeader) {
        List<Map<String, String>> lstData = new ArrayList<>();
        for (CSVRecord record : lstRecord) {
            Map<String, String> mapData = new HashMap<>();
            for (int i = 0; i < lstHeader.size(); i++) {
                String key = lstHeader.get(i);
                String value = record.get(i);
                mapData.put(key, value);
            }
            lstData.add(mapData);
        }
        return lstData;
    }

    public static CSVPrinter getPrinter(String fileName, String... headers) {
        try {
            return CSVFormat.Builder.create()
                    .setHeader(headers).build()
                    .print(new PrintWriter(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void printRecords(String fileName, List<Map<String, String>> lstRecord, String... headers) {
        try {
            CSVPrinter printer = getPrinter(fileName, headers);
            for (Map<String, String> record : lstRecord) {
                Object[] datas = new String[headers.length];
                for (int i = 0; i < headers.length; i++) {
                    datas[i] = record.get(headers[i]);
                }
                printer.printRecord(datas);
            }
            printer.close(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printRecords(CSVPrinter printer, List<Map<String, String>> lstRecord, String... headers) {
        try {
            for (Map<String, String> record : lstRecord) {
                Object[] datas = new String[headers.length];
                for (int i = 0; i < headers.length; i++) {
                    datas[i] = record.get(headers[i]);
                }
                printer.printRecord(datas);
            }
            printer.close(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void csvToExcel(String path, int dataCol) {
        List<String> headers = getHeaders(path);
        List<List<String>> lstHeader = new ArrayList<>();
        for (String header : headers) {
            lstHeader.add(Collections.singletonList(header));
        }
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet();
        PoiExcelUtil.writeHeader(sheet, lstHeader, dataCol);

        int rowIndex = lstHeader.get(0).size();
        List<CSVRecord> lstRecord = getRecords(path);
        for (CSVRecord record : lstRecord) {
            int colIndex = 0;
            for (String header : headers) {
                PoiExcelUtil.writeCellData(sheet, rowIndex, colIndex++, record.get(header));
            }
            rowIndex++;
        }
        int index = path.lastIndexOf(".");
        String fileName = path.substring(0, index);
        PoiExcelUtil.write(workbook, String.format("%s-%s.xlsx", fileName, System.currentTimeMillis()));
    }

}
