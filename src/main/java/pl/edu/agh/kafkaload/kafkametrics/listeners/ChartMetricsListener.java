package pl.edu.agh.kafkaload.kafkametrics.listeners;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import pl.edu.agh.kafkaload.kafkametrics.MetricsListener;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ChartMetricsListener implements MetricsListener {
    private final String outputFileNameWithoutExtension;
    private XYChart chart;
    private SwingWrapper<XYChart> wrapper;
    private JFrame frame;
    private double[] xData;
    private double[][] yData;
    private String[] series;

    public ChartMetricsListener(String outputFileNameWithoutExtension) {
        this.outputFileNameWithoutExtension = outputFileNameWithoutExtension;
    }


    @Override
    public void metricsInit(List<String> names) {
        // Create Chart
        List<String> namesWithoutTime = new LinkedList<>();
        for (int i = 1; i < names.size(); i++) {
            namesWithoutTime.add(names.get(i));
        }

        int size = names.size() - 1;
        xData = new double[] {0.0};
        yData = new double[size][1];
        for (int i = 0; i < yData.length; i++) {
            yData[i][0] = 0.0;
        }
        series = namesWithoutTime.toArray(new String[size]);

        chart = QuickChart.getChart(
                "Metrics chart",
                "Time (s)",
                "Value",
                series,
                xData,
                yData
        );

        // Show it
        wrapper = new SwingWrapper<>(chart);
        this.frame = wrapper.displayChart();
    }

    @Override
    public void metricsUpdate(List<Double> values) {
        if (chart == null || wrapper == null) {
            return;
        }

        xData = append(xData, values.get(0));
        append(yData, values);

        for (int i = 0; i < series.length; i++) {
            chart.updateXYSeries(series[i], xData, yData[i], null);
        }
        wrapper.repaintChart();
    }

    @Override
    public void close() {
        Dimension lastDimension = null;
        try {
            if (frame != null) {
                lastDimension = frame.getSize();
                DisplayMode displayMode = GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDisplayMode();

                frame.setSize(new Dimension(displayMode.getWidth(), displayMode.getHeight()));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            BitmapEncoder.saveBitmap(chart, outputFileNameWithoutExtension + ".png", BitmapEncoder.BitmapFormat.PNG);
            System.out.println("Metrics chart saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (frame != null && lastDimension != null) {
            frame.setSize(lastDimension);
        }
    }

    private void append(double[][] table, List<Double> values) {
        if (table.length != values.size() - 1) {
            return;
        }

        for (int i = 0; i < table.length; i++) {
            double[] subTable = table[i];
            table[i] = append(subTable, values.get(i + 1));  // i+1 because time is at index 0
        }
    }

    private double[] append(double[] oldTable, double value) {
        double[] newTable = new double[oldTable.length + 1];
        System.arraycopy(oldTable, 0, newTable, 0, oldTable.length);
        newTable[newTable.length - 1] = value;
        return newTable;
    }
}
