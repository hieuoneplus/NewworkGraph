import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JsonParse {

    public static void main(String[] args) {

        // Create Chart
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Biểu đồ đường").xAxisTitle("X").yAxisTitle("Y").build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setXAxisLabelRotation(60);

        // Series data
        String[] xData = new String[]{"AVDCSDFDSaaaaaaaaaaaa", "B", "C", "D", "E", "F", "G", "H"};
        double[] yData = new double[]{1, 4, 3, 5, 5, 7, 7, 8};

        // Create a Function for the string labels
        Function<Double, String> xAxisLabelFunction = d -> {
            int index = d.intValue();
            return xData[index];
        };

        // Convert string X data to numeric values
        double[] xNumericData = new double[xData.length];
        for (int i = 0; i < xData.length; i++) {
            xNumericData[i] = (double) i;
        }

        // Add series to chart
        XYSeries series = chart.addSeries("Dữ liệu 1", xNumericData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);

        // Customize X-Axis labels to use the string labels
        chart.setCustomXAxisTickLabelsFormatter(xAxisLabelFunction);

        // Show it
        new SwingWrapper<>(chart).displayChart();
    }
}
