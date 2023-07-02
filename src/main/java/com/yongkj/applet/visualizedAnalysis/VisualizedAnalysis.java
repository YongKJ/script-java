package com.yongkj.applet.visualizedAnalysis;

import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.LinePlot;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class VisualizedAnalysis {

    private final String excelPath;
    private final String outputPath;

    private VisualizedAnalysis() {
        excelPath = GenUtil.getValue("excel-path");
        outputPath = GenUtil.getValue("output-path");
    }

    private void apply() {
        Table table = Table.read().file(excelPath);
        List<String> cols = Arrays.asList(
                "train_loss", "test_loss",
                "train_acc", "test_acc"
        );
        FileUtil.mkdir(outputPath);
        for (String col : cols) {
            String title = "epoch / " + col;
            Table outputTable = table.selectColumns("epoch");
            StringColumn stringColumn = table.stringColumn(col);
            DoubleColumn doubleColumn = stringColumn.parseDouble();
            String fileName = outputPath + "epoch-" + col + ".html";

            doubleColumn.setName(col);
            outputTable.addColumns(doubleColumn);
            Plot.show(LinePlot.create(title, outputTable, "epoch", col), new File(fileName));
        }
    }

    private void multipleLineCharts() {
        List<String> cols = Arrays.asList(
                "train_loss", "test_loss",
                "train_acc", "test_acc"
        );
        Table table = Table.read().file(excelPath);
        ScatterTrace[] traces = getTraces(table, cols);
        String fileName = outputPath + "epoch-data.html";
        String title = "visual analysis of training data";
        Layout layout = Layout.builder(title, "epoch", "training data").showLegend(true).build();
        Plot.show(new Figure(layout, traces), new File(fileName));
    }

    private ScatterTrace[] getTraces(Table table, List<String> cols) {
        ScatterTrace[] traces = new ScatterTrace[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            traces[i] = ScatterTrace.builder(
                            table.numberColumn("epoch"),
                            getColData(table, cols.get(i)))
                        .showLegend(true)
                        .name(cols.get(i))
                        .mode(ScatterTrace.Mode.LINE)
                        .build();
        }
        return traces;
    }

    private DoubleColumn getColData(Table table, String col) {
        return table.stringColumn(col).parseDouble();
    }

    public static void run(String[] args) {
//        new VisualizedAnalysis().apply();
        new VisualizedAnalysis().multipleLineCharts();
    }

}
