package com.yongkj.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * PoiExcelUtil 端到端验证：写读往返、合并表头、显式样式、多 workbook 状态隔离。
 * 输出文件统一写到 target/ 下。
 */
public class PoiExcelUtilTest {

    private static File outFile(String name) {
        return new File("target", name + "-" + System.currentTimeMillis() + ".xlsx");
    }

    /** 多行合并表头 + 便捷重载 + 写读往返 + 数据区合并单元格读取。 */
    @Test
    public void testMergedHeaderWriteRead() throws Exception {
        File out = outFile("merged-header");
        SXSSFWorkbook wb = new SXSSFWorkbook();
        SXSSFSheet sheet = wb.createSheet("Sheet1");

        // 两列、两行表头（各自纵向合并）
        List<List<String>> header = new ArrayList<>();
        header.add(Arrays.asList("序号", "序号"));
        header.add(Arrays.asList("名称", "名称"));
        PoiExcelUtil.writeHeader(sheet, header, 1);

        // 数据从第 2 行开始；第 2 行两列合并，模拟 Excel 合并后右侧单元格为空
        PoiExcelUtil.writeCellData(sheet, 2, 0, 1);
        PoiExcelUtil.writeCellData(sheet, 2, 1, "");
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 1));
        for (int i = 1; i < 5; i++) {
            PoiExcelUtil.writeCellData(sheet, 2 + i, 0, i + 1);
            PoiExcelUtil.writeCellData(sheet, 2 + i, 1, "商品" + i);
        }

        assertTrue(PoiExcelUtil.write(wb, out.getAbsolutePath()));
        wb.close();

        // headerRow=0, dataRow=2
        List<Map<String, String>> data = PoiExcelUtil.toMap(out.getAbsolutePath(), 0, 0, 2);
        assertEquals(5, data.size());
        assertTrue(data.get(0).containsKey("序号"));
        // 合并单元格：第 2 行第 1 列（名称）为空，应取合并区左上角值
        assertEquals("1", data.get(0).get("序号"));
        assertEquals("1", data.get(0).get("名称"));
        assertEquals("商品1", data.get(1).get("名称"));
    }

    /** 显式样式路径（与 dataMigration 各 service 一致的写法）。 */
    @Test
    public void testExplicitStyleWriteRead() throws Exception {
        File out = outFile("explicit-style");
        SXSSFWorkbook wb = new SXSSFWorkbook();
        SXSSFSheet sheet = wb.createSheet("Sheet1");

        List<List<String>> header = new ArrayList<>();
        header.add(Collections.singletonList("序号"));
        header.add(Collections.singletonList("名称"));

        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(wb);
        PoiExcelUtil.writeHeader(sheet, header, lstCellStyle, 1);

        int rowIndex = 1;
        for (int i = 0; i < 5; i++) {
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 0, i + 1);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex++, 1, "商品" + i);
        }
        assertTrue(PoiExcelUtil.write(wb, out.getAbsolutePath()));
        wb.close();

        List<Map<String, String>> data = PoiExcelUtil.toMap(out.getAbsolutePath());
        assertEquals(5, data.size());
        assertEquals("3", data.get(2).get("序号"));
        assertEquals("商品4", data.get(4).get("名称"));
    }

    /** 同一 JVM 内连续导出两个 workbook：验证状态按 workbook 隔离（原静态字段会串台）。 */
    @Test
    public void testStateIsolationAcrossWorkbooks() throws Exception {
        List<List<String>> header = new ArrayList<>();
        header.add(Collections.singletonList("序号"));
        header.add(Collections.singletonList("名称"));

        File out1 = outFile("isolation-a");
        SXSSFWorkbook wb1 = new SXSSFWorkbook();
        SXSSFSheet sheet1 = wb1.createSheet("Sheet1");
        PoiExcelUtil.writeHeader(sheet1, header, 1);
        for (int i = 0; i < 5; i++) {
            Map<Integer, Object> row = new HashMap<>();
            row.put(0, i + 1);
            row.put(1, "A-" + i);
            PoiExcelUtil.writeRowData(sheet1, row);
        }
        assertTrue(PoiExcelUtil.write(wb1, out1.getAbsolutePath()));
        wb1.close();

        File out2 = outFile("isolation-b");
        SXSSFWorkbook wb2 = new SXSSFWorkbook();
        SXSSFSheet sheet2 = wb2.createSheet("Sheet1");
        PoiExcelUtil.writeHeader(sheet2, header, 1);
        for (int i = 0; i < 5; i++) {
            Map<Integer, Object> row = new HashMap<>();
            row.put(0, i + 1);
            row.put(1, "B-" + i);
            PoiExcelUtil.writeRowData(sheet2, row);
        }
        assertTrue(PoiExcelUtil.write(wb2, out2.getAbsolutePath()));
        wb2.close();

        List<Map<String, String>> data1 = PoiExcelUtil.toMap(out1.getAbsolutePath());
        List<Map<String, String>> data2 = PoiExcelUtil.toMap(out2.getAbsolutePath());
        assertEquals(5, data1.size());
        assertEquals(5, data2.size());
        assertEquals("A-0", data1.get(0).get("名称"));
        assertEquals("B-0", data2.get(0).get("名称"));
    }
}
