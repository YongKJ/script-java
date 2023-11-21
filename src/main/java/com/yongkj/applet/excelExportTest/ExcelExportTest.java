package com.yongkj.applet.excelExportTest;

import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import com.yongkj.util.PoiExcelUtil;
import com.yongkj.util.ThreadUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.*;

public class ExcelExportTest {

    private final String excelReadPath;
    private final String excelWritePath;
    private final int ROW_ACCESS_WINDOW_SIZE;

    private ExcelExportTest() {
        this.ROW_ACCESS_WINDOW_SIZE = 100;
        this.excelReadPath = GenUtil.getValue("excel-read-path");
        this.excelWritePath = GenUtil.getValue("excel-write-path");
    }

    private void apply() {
//        readTestOne();
        writeTestSix();
//        writeTestFive();
//        writeTestFour();
//        writeTestThree();
//        writeTestTwo();
//        writeTestOne();
    }

    private void readTestOne() {
        List<Map<String, String>> lstData = PoiExcelUtil.toMap(excelReadPath);

        LogUtil.loggerLine(Log.of("ExcelExportTest", "readTestOne", "lstData.size()", lstData.size()));
        LogUtil.loggerLine(Log.of("ExcelExportTest", "readTestOne", "lstData", lstData));
        System.out.println("------------------------------------------------------------------------------------------------------------");
    }

    private void writeTestSix() {
        List<List<String>> lstHeader = new ArrayList<>();
        lstHeader.add(Collections.singletonList("序号"));
        lstHeader.add(Collections.singletonList("书名"));
        lstHeader.add(Collections.singletonList("作者"));
        lstHeader.add(Collections.singletonList("年代"));
        lstHeader.add(Collections.singletonList("字数"));

        Map<Integer, List<String>> mapData = new HashMap<>();
        mapData.put(0, Arrays.asList("《水浒传》", "施耐庵", "宋朝", "96 万字"));
        mapData.put(1, Arrays.asList("《三国演义》", "罗贯中", "元朝", "73.4 万字"));
        mapData.put(2, Arrays.asList("《西游记》", "吴承恩", "明代", "82 万字"));
        mapData.put(3, Arrays.asList("《红楼梦》", "曹雪芹", "清代", "107.5 万字"));
        mapData.put(4, Arrays.asList("《聊斋志异》", "蒲松龄", "清代", "70.8 万字"));

        int dataRow = lstHeader.get(0).size();
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        List<SXSSFSheet> sheets = getSheets(workbook);
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        ThreadUtil.executeWithListDataByThreadPool(1, sheets, sheet -> {
            PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
            Map<Integer, Integer> mapColWidth = PoiExcelUtil.getInitColWidths(sheet, lstHeader);
            for (int i = 0; i < mapData.size(); ) {
                int dataSize = i + ROW_ACCESS_WINDOW_SIZE;
                dataSize = Math.min(dataSize, mapData.size());
                ThreadUtil.executeWithListDataByThreadPool(1, sheet, i, dataSize, (row, index) -> {
                    int colIndex = 0;
                    PoiExcelUtil.updateColWidth(mapColWidth, colIndex, row.getRowNum());
                    PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, row.getRowNum());
                    for (String data : Optional.ofNullable(mapData.get(index)).orElse(new ArrayList<>())) {
                        PoiExcelUtil.updateColWidth(mapColWidth, colIndex, data);
                        PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, data);
                    }
                });
                i = dataSize;
            }
            PoiExcelUtil.updateColWidths(sheet, mapColWidth);
        });

        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\demo-test-by-thread-" + System.currentTimeMillis() + ".xlsx");
    }

    private void writeTestFive() {
        List<List<String>> lstHeader = new ArrayList<>();
        lstHeader.add(Collections.singletonList("序号"));
        lstHeader.add(Collections.singletonList("书名"));
        lstHeader.add(Collections.singletonList("作者"));
        lstHeader.add(Collections.singletonList("年代"));
        lstHeader.add(Collections.singletonList("字数"));

        Map<Integer, List<String>> mapData = new HashMap<>();
        mapData.put(0, Arrays.asList("《水浒传》", "施耐庵", "宋朝", "96 万字"));
        mapData.put(1, Arrays.asList("《三国演义》", "罗贯中", "元朝", "73.4 万字"));
        mapData.put(2, Arrays.asList("《西游记》", "吴承恩", "明代", "82 万字"));
        mapData.put(3, Arrays.asList("《红楼梦》", "曹雪芹", "清代", "107.5 万字"));
        mapData.put(4, Arrays.asList("《聊斋志异》", "蒲松龄", "清代", "70.8 万字"));

        int dataRow = lstHeader.get(0).size();
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        List<SXSSFSheet> sheets = getSheets(workbook);
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        ThreadUtil.executeWithListDataByThreadPool(1, sheets, sheet -> {
            PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1, 18);
            for (int i = 0; i < mapData.size(); ) {
                int dataSize = i + ROW_ACCESS_WINDOW_SIZE;
                dataSize = Math.min(dataSize, mapData.size());
                ThreadUtil.executeWithListDataByThreadPool(1, sheet, i, dataSize, (row, index) -> {
                    int colIndex = 0;
                    PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, row.getRowNum());
                    for (String data : Optional.ofNullable(mapData.get(index)).orElse(new ArrayList<>())) {
                        PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, data);
                    }
                });
                i = dataSize;
            }
        });

        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\demo-test-by-thread-" + System.currentTimeMillis() + ".xlsx");
    }

    private void writeTestFour() {
        List<List<String>> lstHeader = new ArrayList<>();
        lstHeader.add(Collections.singletonList("序号"));
        lstHeader.add(Collections.singletonList("书名"));
        lstHeader.add(Collections.singletonList("作者"));
        lstHeader.add(Collections.singletonList("年代"));
        lstHeader.add(Collections.singletonList("字数"));

        List<List<String>> lstData = new ArrayList<>();
        lstData.add(Arrays.asList("《水浒传》", "施耐庵", "宋朝", "96 万字"));
        lstData.add(Arrays.asList("《三国演义》", "罗贯中", "元朝", "73.4 万字"));
        lstData.add(Arrays.asList("《西游记》", "吴承恩", "明代", "82 万字"));
        lstData.add(Arrays.asList("《红楼梦》", "曹雪芹", "清代", "107.5 万字"));
        lstData.add(Arrays.asList("《聊斋志异》", "蒲松龄", "清代", "70.8 万字"));

        int dataRow = lstHeader.get(0).size();
        SXSSFWorkbook workbook = new SXSSFWorkbook(-1);
        List<SXSSFSheet> sheets = getSheets(workbook);
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        ThreadUtil.executeWithListDataByThreadPool(1, sheets, sheet -> {
            PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
            ThreadUtil.executeWithListDataByThreadPool(1, sheet, lstData, (row, tempLstData) -> {
                int colIndex = 0;
                PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, row.getRowNum());
                for (String data : tempLstData) {
                    PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, data);
                }
            });
        });

        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\demo-test-by-thread-" + System.currentTimeMillis() + ".xlsx");
    }

    private List<SXSSFSheet> getSheets(SXSSFWorkbook workbook) {
        List<SXSSFSheet> lstSheet = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String sheetName = String.format("Sheet%s", i);
            lstSheet.add(workbook.createSheet(sheetName));
        }
        return lstSheet;
    }

    private void writeTestThree() {
        List<List<String>> lstHeader = new ArrayList<>();
        lstHeader.add(Collections.singletonList("序号"));
        lstHeader.add(Collections.singletonList("书名"));
        lstHeader.add(Collections.singletonList("作者"));
        lstHeader.add(Collections.singletonList("年代"));
        lstHeader.add(Collections.singletonList("字数"));

        List<List<String>> lstData = new ArrayList<>();
        lstData.add(Arrays.asList("《水浒传》", "施耐庵", "宋朝", "96 万字"));
        lstData.add(Arrays.asList("《三国演义》", "罗贯中", "元朝", "73.4 万字"));
        lstData.add(Arrays.asList("《西游记》", "吴承恩", "明代", "82 万字"));
        lstData.add(Arrays.asList("《红楼梦》", "曹雪芹", "清代", "107.5 万字"));
        lstData.add(Arrays.asList("《聊斋志异》", "蒲松龄", "清代", "70.8 万字"));

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet();
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
        for (int i = 0, rowIndex = lstHeader.get(0).size(); i < lstData.size(); i++, rowIndex++) {
            int colIndex = 0;
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, (i + 1) + "");
            for (int j = 0; j < lstData.get(i).size(); j++) {
                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, lstData.get(i).get(j));
            }
        }
        PoiExcelUtil.write(workbook, excelReadPath);
        PoiExcelUtil.write(workbook, excelWritePath);
    }

    private void writeTestTwo() {
        List<List<String>> lstHeader = new ArrayList<>();
        lstHeader.add(Collections.singletonList("序号"));
        lstHeader.add(Collections.singletonList("书名"));
        lstHeader.add(Collections.singletonList("作者"));
        lstHeader.add(Collections.singletonList("年代"));
        lstHeader.add(Collections.singletonList("字数"));

        List<List<String>> lstData = new ArrayList<>();
        lstData.add(Arrays.asList("《水浒传》", "施耐庵", "宋朝", "96 万字"));
        lstData.add(Arrays.asList("《三国演义》", "罗贯中", "元朝", "73.4 万字"));
        lstData.add(Arrays.asList("《西游记》", "吴承恩", "明代", "82 万字"));
        lstData.add(Arrays.asList("《红楼梦》", "曹雪芹", "清代", "107.5 万字"));
        lstData.add(Arrays.asList("《聊斋志异》", "蒲松龄", "清代", "70.8 万字"));

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        for (int num = 1; num <= 5; num++) {
            SXSSFSheet sheet = workbook.createSheet();
            PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
            for (int i = 0, rowIndex = lstHeader.get(0).size(); i < lstData.size(); i++, rowIndex++) {
                int colIndex = 0;
                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, (i + 1) + "");
                for (int j = 0; j < lstData.get(i).size(); j++) {
                    PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, lstData.get(i).get(j));
                }
            }
        }
        PoiExcelUtil.write(workbook, "C:\\Users\\admin\\Desktop\\demo-width-auto-" + System.currentTimeMillis() + ".xlsx");
    }

    private void writeTestOne() {
        List<List<String>> lstHeader = new ArrayList<>();
        lstHeader.add(Arrays.asList("a", "a", "e"));
        lstHeader.add(Arrays.asList("b", "b", "e"));
        lstHeader.add(Arrays.asList("b", "b", "f"));
        lstHeader.add(Arrays.asList("c", "a", "a"));
        lstHeader.add(Arrays.asList("c", "a", "a"));
        lstHeader.add(Arrays.asList("测", "测", "测"));
        lstHeader.add(Arrays.asList("测试", "测试", "测试"));
        lstHeader.add(Arrays.asList("t", "t", "t"));
        lstHeader.add(Arrays.asList("test", "test", "test"));

        String fileName = "C:\\Users\\admin\\Desktop\\demo.xlsx";
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("Sheet1");
        PoiExcelUtil.writeHeader(sheet, lstHeader, 0);

        for (int i = 0; i < 500; i++) {
            PoiExcelUtil.writeRowData(sheet, getMapData("test"));
            PoiExcelUtil.writeRowData(sheet, getMapData("demo"));
        }
        PoiExcelUtil.write(workbook, fileName);
    }

    private Map<Integer, Object> getMapData(String value) {
        Map<Integer, Object> mapData = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            mapData.put(i, value);
        }
        return mapData;
    }

    public static void run(String[] args) {
        new ExcelExportTest().apply();
    }
}
