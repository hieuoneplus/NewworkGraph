import org.knowm.xchart.*;

import java.util.Random;

public class Test {
    public static void main(String[] args) {
        double[] xData = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yData = new double[]{10.0, 15.0, 20.0, 25.0, 30.0};

        // Tạo đồ thị scatter plot
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Biểu đồ Scatter Plot").xAxisTitle("X").yAxisTitle("Y").build();
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);

        // Thêm dữ liệu vào biểu đồ
        chart.addSeries("Dữ liệu", xData, yData);

        // Hiển thị đồ thị
//        new SwingWrapper<>(chart).displayChart();

        Random n = new Random();
        System.out.println(n.nextInt(3));
    }
}
