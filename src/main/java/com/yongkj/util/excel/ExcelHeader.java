package com.yongkj.util.excel;

import com.yongkj.pojo.dto.Coords;
import com.yongkj.util.GenUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelHeader {

    /** 表头合并时四方向扩展的移动向量（右、下、左、上）。 */
    private static final int[][] MOVE = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

    /** 数据行高（twip，1/20 磅）：4 * 180 = 720，即 36 磅。 */
    static final short ROW_HEIGHT = (short) (4 * 180);

    /** Excel 单列最大宽度（约 255 字符）。 */
    private static final int CELL_MAX_WIDTH = 255 * 256;
    /** 超长字段的展示封顶宽度（约 50 个汉字）。 */
    private static final int LONG_FIELD_WIDTH = 50 * 256 * 3;
    /** 列宽缓存上限与可缓存字符串长度上限。 */
    private static final int WIDTH_CACHE_MAX = 2048;
    private static final int WIDTH_CACHE_KEY_MAX_LEN = 128;

    /**
     * 列宽是纯函数（只依赖字符串内容），做有界缓存避免同一取值反复逐字符扫描。
     * 用 ConcurrentHashMap 保证不同 workbook（不同线程）并发计算时安全。
     * 缓存条数用原子计数维护，避免每次未命中都调用 O(n) 的 ConcurrentHashMap.size()。
     */
    private static final ConcurrentHashMap<String, Integer> WIDTH_CACHE = new ConcurrentHashMap<>();
    private static final AtomicInteger WIDTH_CACHE_SIZE = new AtomicInteger();

    private ExcelHeader() {
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, int dataCol) {
        writeHeader(sheet, lstHeader, dataCol, null);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, int dataCol, List<Integer> lstExcludeRow) {
        int colSize = lstHeader.size();
        int rowSize = lstHeader.get(0).size();
        boolean[][] lstFlag = new boolean[rowSize][colSize];
        ExcelContext ctx = ExcelContext.of(sheet.getWorkbook());
        List<CellStyle> lstCellStyle = ctx.getOrCreateLstCellStyle(sheet);
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                if (!lstFlag[row][col]) {
                    lstFlag[row][col] = true;

                    List<Coords> lstCoords = new ArrayList<>();
                    lstCoords.add(Coords.of(lstHeader, row, col));
                    //取消某些行的单元格合并
                    if (lstExcludeRow == null || lstExcludeRow.isEmpty() || !lstExcludeRow.contains(row)) {
                        checkMergeRange(lstHeader, lstFlag, lstCoords, row, col, lstHeader.get(col).get(row));
                    }

                    merge(sheet, lstCoords, lstCellStyle, ctx);
                }
            }
        }
        //单元格冻结：从上往下，冻结 dataRow 行；从左往右，冻结 dataCol 列
        sheet.createFreezePane(dataCol, ctx.getDataStartRow(), dataCol, ctx.getDataStartRow());
    }

    public static void checkMergeRange(List<List<String>> lstHeader, boolean[][] lstFlag, List<Coords> lstCoords, int x, int y, String value) {
        int colSize = lstHeader.size();
        int rowSize = lstHeader.get(0).size();
        for (int[] move : MOVE) {
            int moveX = x + move[0];
            int moveY = y + move[1];
            if (0 <= moveX && moveX < rowSize && 0 <= moveY && moveY < colSize && !lstFlag[moveX][moveY]) {
                if (Objects.equals(lstHeader.get(moveY).get(moveX), value)) {
                    lstFlag[moveX][moveY] = true;
                    lstCoords.add(Coords.of(lstHeader, moveX, moveY));
                    checkMergeRange(lstHeader, lstFlag, lstCoords, moveX, moveY, value);
                }
            }
        }
    }

    /** 按 (x, y) 字典序比较坐标，x 优先。 */
    static int compare(Coords a, Coords b) {
        int ax = a.getX(), ay = a.getY();
        int bx = b.getX(), by = b.getY();
        if (ax != bx) {
            return ax < bx ? -1 : 1;
        }
        if (ay != by) {
            return ay < by ? -1 : 1;
        }
        return 0;
    }

    static Coords minCoords(List<Coords> lstCoords) {
        Coords min = lstCoords.get(0);
        for (Coords c : lstCoords) {
            if (compare(c, min) < 0) {
                min = c;
            }
        }
        return min;
    }

    static Coords maxCoords(List<Coords> lstCoords) {
        Coords max = lstCoords.get(0);
        for (Coords c : lstCoords) {
            if (compare(c, max) > 0) {
                max = c;
            }
        }
        return max;
    }

    private static void merge(SXSSFSheet sheet, List<Coords> lstCoords, List<CellStyle> lstCellStyle, ExcelContext ctx) {
        //一次 O(n) 遍历求最小/最大坐标（替代原先非传递性的全量排序）
        Coords minCoords = minCoords(lstCoords);
        Coords maxCoords = maxCoords(lstCoords);
        //表头数据写入到最小坐标的单元格中
        ExcelWriter.setCellValue(sheet, minCoords.getX(), minCoords.getY(), minCoords.getValue());
        for (Coords coords : lstCoords) {
            //记录列宽（写盘时统一应用，避免逐单元格 setColumnWidth）
            setWidthColByAuto(sheet, coords.getY(), coords.getValue());
            //设置行高
            Row row = CellUtil.getRow(coords.getX(), sheet);
            row.setHeight(ROW_HEIGHT);
            //设置单元格样式
            Cell cell = CellUtil.getCell(row, coords.getY());
            cell.setCellStyle(lstCellStyle.get(0));
            //记录数据起始行号（表头最大行号 + 1）
            ctx.updateDataStartRow(coords.getX() + 1);
        }
        //合并单元格
        if (lstCoords.size() > 1) {
            sheet.addMergedRegion(new CellRangeAddress(minCoords.getX(), maxCoords.getX(), minCoords.getY(), maxCoords.getY()));
        }
    }

    /**
     * 记录某单元格内容所需的列宽（取各单元格最大宽度），写盘时统一应用。
     * 取代原先逐单元格立即 {@code sheet.setColumnWidth} 的做法，把 O(总字符数) 的重复调用
     * 收敛为每列一次。
     */
    public static void setWidthColByAuto(SXSSFSheet sheet, int col, String value) {
        ExcelContext ctx = ExcelContext.of(sheet.getWorkbook());
        updateColWidth(ctx.getOrCreateSheetColWidth(sheet), col, value);
    }

    private static int getWidthCol(String value) {
        //超长值不缓存（避免为长字符串计算 hash / 占用缓存）
        if (value.length() > WIDTH_CACHE_KEY_MAX_LEN) {
            return computeWidthCol(value);
        }
        Integer cached = WIDTH_CACHE.get(value);
        if (cached != null) {
            return cached;
        }
        int width = computeWidthCol(value);
        if (WIDTH_CACHE_SIZE.get() < WIDTH_CACHE_MAX && WIDTH_CACHE.putIfAbsent(value, width) == null) {
            WIDTH_CACHE_SIZE.incrementAndGet();
        }
        return width;
    }

    private static int computeWidthCol(String value) {
        int chineseSum = 0;
        int englishSum = 0;
        int charSum = 0;
        char[] lstChar = value.toCharArray();
        for (char c : lstChar) {
            if (isChineseByScript(c)) {
                chineseSum += 2;
                charSum += 2;
            } else {
                englishSum += 1;
                charSum += 1;
            }
        }
        charSum = charSum > 1 ? 13 + charSum - 1 : 13;
        if (chineseSum == 0 && englishSum > 0) {
            charSum += 4;
        } else if (chineseSum > 0 && englishSum > 0) {
            double percent = (double) englishSum / chineseSum;
            if (percent < 0.2) {
                charSum -= 2;
            } else if (percent > 0.8) {
                charSum += 2;
            }
        }
        //对于较长字段封顶展示（UTF-8 一个汉字占 3 个字节）
        if (value.getBytes(StandardCharsets.UTF_8).length * 256 > CELL_MAX_WIDTH) {
            charSum = LONG_FIELD_WIDTH / 256;
        }
        return charSum;
    }

    private static boolean isChineseByScript(char c) {
        Character.UnicodeScript sc = Character.UnicodeScript.of(c);
        return sc == Character.UnicodeScript.HAN;
    }

    public static List<CellStyle> getCellStyles(SXSSFWorkbook workbook) {
        return getCellStyles(workbook, new Color(242, 242, 242), new Color(250, 250, 250), new Color(212, 212, 212));
    }

    public static List<CellStyle> getCellStyles(SXSSFWorkbook workbook, Color headerBackColor, Color dataBackColor, Color borderColor) {
        List<XSSFColor> lstColor = Arrays.asList(
                new XSSFColor(headerBackColor, null),
                new XSSFColor(dataBackColor, null),
                new XSSFColor(borderColor, null)
        );
        return Arrays.asList(
                getCellStyle(workbook, lstColor, 0),
                getCellStyle(workbook, lstColor, 1),
                getCellStyle(workbook, lstColor, 2)
        );
    }

    private static XSSFCellStyle getCellStyle(SXSSFWorkbook workbook, List<XSSFColor> lstColor, int choose) {
        XSSFFont font = (XSSFFont) workbook.createFont();
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        if (choose == 0) {
            //设置表头字体样式：粗体、大小
            font.setBold(true);
            font.setFontHeightInPoints((short) 11);
            //设置背景色
            cellStyle.setFillForegroundColor(lstColor.get(0));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (choose == 1) {
            //设置数据显示字体样式：大小
            font.setFontHeightInPoints((short) 10);
            //设置背景色
            cellStyle.setFillForegroundColor(lstColor.get(1));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (choose == 2) {
            //设置数据显示字体样式：大小
            font.setFontHeightInPoints((short) 10);
        }
        //设置字体: 微软雅黑
        font.setFontName("Microsoft YaHei");
        cellStyle.setFont(font);
        //设置居中：垂直居中、水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置边框宽度
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        //设置边框颜色
        cellStyle.setTopBorderColor(lstColor.get(2));
        cellStyle.setBottomBorderColor(lstColor.get(2));
        cellStyle.setLeftBorderColor(lstColor.get(2));
        cellStyle.setRightBorderColor(lstColor.get(2));
        return cellStyle;
    }

    public static Map<Integer, Integer> getInitColWidths(SXSSFSheet sheet) {
        Map<Integer, Integer> mapColWidth = new HashMap<>();
        SXSSFRow row = sheet.getRow(0);
        if (row == null) {
            return mapColWidth;
        }
        for (int col = 0; col < row.getLastCellNum(); col++) {
            mapColWidth.put(col, sheet.getColumnWidth(col));
        }
        return mapColWidth;
    }

    public static void updateColWidth(Map<Integer, Integer> mapColWidth, Integer col, Object value) {
        int oldColWidth = mapColWidth.getOrDefault(col, 0);
        int newColWidth = getWidthCol(GenUtil.objToStr(value)) * 256;
        if (newColWidth > oldColWidth) {
            mapColWidth.put(col, newColWidth);
        }
    }

    public static void updateColWidths(SXSSFSheet sheet, Map<Integer, Integer> mapColWidth) {
        for (Map.Entry<Integer, Integer> entry : mapColWidth.entrySet()) {
            sheet.setColumnWidth(entry.getKey(), entry.getValue());
        }
    }

}
