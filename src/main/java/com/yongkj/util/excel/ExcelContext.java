package com.yongkj.util.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单次导出（单个 workbook）的上下文，承载原先挂在 PoiExcelUtil 上的静态可变状态。
 *
 * <p>背景：旧实现把 lstCellStyle / drawing / dataRow / mapSheetColWidth 声明为 PoiExcelUtil 的
 * static 字段，导致：
 * <ul>
 *     <li>并发导出时共享同一份状态，互相覆盖；</li>
 *     <li>同一 JVM 内多次导出会复用上一个 workbook 的 CellStyle，POI 会抛
 *         "This Style does not belong to the supplied Workbook"；</li>
 *     <li>状态无清理时机，dataRow / drawing 泄漏到下一次导出。</li>
 * </ul>
 *
 * <p>现在每个 workbook 一份上下文，通过 {@link #of(Workbook)} 以 workbook 身份做弱引用缓存，
 * workbook 被 GC 后上下文自动回收，无需显式清理。不同 workbook（不同线程）之间天然隔离。
 */
public class ExcelContext {

    /** 以 workbook 为弱 key 的上下文注册表，避免跨导出串台，同时不阻止 workbook 被回收。 */
    private static final Map<Workbook, ExcelContext> REGISTRY =
            Collections.synchronizedMap(new WeakHashMap<Workbook, ExcelContext>());

    /** 表头占用行数 = 数据起始行号（表头自第 0 行起）。默认 1，与旧实现保持一致。 */
    private int dataStartRow = 1;
    /** 三种单元格样式（表头 / 斑马纹浅 / 斑马纹深），首次写表头时懒加载。 */
    private List<CellStyle> lstCellStyle;
    /** 每张 sheet 的列宽（列号 -> 宽度），写盘时统一应用。 */
    private final Map<String, Map<Integer, Integer>> mapSheetColWidth =
            new ConcurrentHashMap<String, Map<Integer, Integer>>();
    /** 每张 sheet 的图片绘制对象（sheet 级，懒加载），解决 drawing 跨 sheet 串用。 */
    private final Map<String, SXSSFDrawing> mapDrawing = new ConcurrentHashMap<String, SXSSFDrawing>();

    private ExcelContext() {
    }

    /** 获取（或创建）指定 workbook 的上下文。 */
    public static ExcelContext of(Workbook workbook) {
        synchronized (REGISTRY) {
            ExcelContext ctx = REGISTRY.get(workbook);
            if (ctx == null) {
                ctx = new ExcelContext();
                REGISTRY.put(workbook, ctx);
            }
            return ctx;
        }
    }

    public int getDataStartRow() {
        return dataStartRow;
    }

    /** 记录表头最大行号 + 1（即数据起始行），只增不减。 */
    public void updateDataStartRow(int row) {
        if (row > dataStartRow) {
            dataStartRow = row;
        }
    }

    public List<CellStyle> getLstCellStyle() {
        return lstCellStyle;
    }

    public void setLstCellStyle(List<CellStyle> lstCellStyle) {
        this.lstCellStyle = lstCellStyle;
    }

    /** 获取（或懒加载）当前 workbook 的三种单元格样式。 */
    public List<CellStyle> getOrCreateLstCellStyle(SXSSFSheet sheet) {
        if (lstCellStyle == null) {
            lstCellStyle = ExcelHeader.getCellStyles(sheet.getWorkbook());
        }
        return lstCellStyle;
    }

    public Map<String, Map<Integer, Integer>> getMapSheetColWidth() {
        return mapSheetColWidth;
    }

    /** 获取某 sheet 的列宽表，不存在时按该 sheet 当前列宽初始化。 */
    public Map<Integer, Integer> getOrCreateSheetColWidth(SXSSFSheet sheet) {
        return mapSheetColWidth.computeIfAbsent(sheet.getSheetName(), k -> ExcelHeader.getInitColWidths(sheet));
    }

    /** 写盘完成后清空列宽缓存，避免跨次 write 重复应用。 */
    public void clearSheetColWidth() {
        mapSheetColWidth.clear();
    }

    /** 获取（或创建）某 sheet 的图片绘制对象。 */
    public SXSSFDrawing getOrCreateDrawing(SXSSFSheet sheet) {
        return mapDrawing.computeIfAbsent(sheet.getSheetName(), k -> sheet.createDrawingPatriarch());
    }
}
