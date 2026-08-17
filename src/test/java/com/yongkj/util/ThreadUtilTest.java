package com.yongkj.util;

import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ThreadUtilTest {

    /** 并行处理：所有元素都被消费，结果正确（加法与顺序无关）。 */
    @Test
    public void testParallelProcessesAllItems() {
        List<Integer> lst = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            lst.add(i);
        }
        AtomicInteger sum = new AtomicInteger();
        ThreadUtil.executeWithListDataByThreadPool(1, lst, sum::addAndGet);
        assertEquals(4950, sum.get());
    }

    /** 任务异常应被收集并向上抛出，而不是静默吞掉。 */
    @Test
    public void testParallelPropagatesException() {
        List<Integer> lst = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            lst.add(i);
        }
        try {
            ThreadUtil.executeWithListDataByThreadPool(1, lst, i -> {
                if (i == 25) {
                    throw new IllegalStateException("boom");
                }
            });
            fail("应当抛出异常");
        } catch (IllegalStateException e) {
            assertEquals("boom", e.getMessage());
        }
    }

    /** 串行写 sheet：按区间创建行并写入正确值。 */
    @Test
    public void testSerialSheetWriteByRange() {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        try {
            SXSSFSheet sheet = wb.createSheet();
            ThreadUtil.executeWithListDataByThreadPool(1, sheet, 0, 10, (row, index) ->
                    row.createCell(0).setCellValue(index));
            assertEquals(10, sheet.getLastRowNum() + 1);
            assertEquals(5.0, sheet.getRow(5).getCell(0).getNumericCellValue(), 0.0);
        } finally {
            wb.dispose();
        }
    }

    /** 串行写 sheet：按 List 写入。 */
    @Test
    public void testSerialSheetWriteByList() {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        try {
            SXSSFSheet sheet = wb.createSheet();
            List<String> data = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                data.add("v" + i);
            }
            ThreadUtil.executeWithListDataByThreadPool(1, sheet, data, (row, value) ->
                    row.createCell(0).setCellValue(value));
            assertEquals(5, sheet.getLastRowNum() + 1);
            assertEquals("v3", sheet.getRow(3).getCell(0).getStringCellValue());
            assertTrue(true);
        } finally {
            wb.dispose();
        }
    }
}
