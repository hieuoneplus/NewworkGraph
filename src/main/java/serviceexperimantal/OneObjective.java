package serviceexperimantal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Individual;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import service.CommonService;
import service.GetKPath;
import service.NSGA_II;
import service.Utils;
import serviceexperimantal.model.Average;
import serviceexperimantal.model.GenGA;
import serviceexperimantal.model.GenNSGA;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OneObjective {
    private static String obj = "Lb";
    public static String numK = "10";
    public static String pathGA = "src/main/java/data/output/GAk" + numK +"/";
    public static String pathNSGA_II = "src/main/java/data/output/NSGA-IIk" + numK + "/";

    public static String pathTest = "src/main/java/data/output/" + obj +" _result_K" + numK + ".txt";

    public static String pathAvg = "src/main/java/data/output/" + obj + "_average_K" + numK + ".txt";

    public static int count = 0;


    public static List<Average> hvResult = new ArrayList<>();

    public static List<Average> view = new ArrayList<>();
    public static List<Average> exist = new ArrayList<>();
    public static void main(String[] args) {
        excute();
        calcAvg();
        draw();
    }
    public static void calcAvg() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathAvg))) {
            writer.write(String.format("%-30s","Instance"));
            writer.write(String.format("%-25s","NSGA-II"));
            writer.write(String.format("%-25s","GA"));

            writer.newLine();
            for (var pt : hvResult) {
                if (!exist.contains(pt)) {
                    exist.add(pt);
                    double avgNSGA = pt.getNsga();
                    double avgGA = pt.getGa();
                    String[] sub = pt.getInstance().split("_");
                    var name = sub[0].concat("_".concat(sub[1]));
                    for (var other : hvResult) {
                        if (!pt.equals(other) && !exist.contains(other) && other.getInstance().startsWith(name) && other.getInstance().endsWith(sub[3])) {
                            if(other.getGa() > avgGA) {
                               avgGA = other.getGa();
                            }
                            if(other.getNsga() > avgNSGA) {
                                avgNSGA = other.getNsga();
                            }
                            exist.add(other);
                        }
                    }
                    Average ag = new Average();
                    ag.setNsga(avgNSGA);
                    ag.setGa(avgGA);
                    ag.setInstance(sub[0] + "_" + sub[1] + "_" + sub[3].substring(0,sub[3].indexOf("requests")));
                    view.add(ag);
                    writer.write(String.format("%-30s",sub[0] + "_" + sub[1] + "_" + sub[3]));
                    writer.write(String.format("%-25s",avgNSGA));
                    writer.write(String.format("%-25s",avgGA));

                    writer.newLine();
                }
            }
        } catch (Exception e) {
            System.out.println("error!");
        }
    }
    public static void excute() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathTest))) {
            File direct = new File(pathNSGA_II);
            File[] eachs = direct.listFiles();
            writer.write(String.format("%-30s","Instance"));
            writer.write(String.format("%-25s","NSGA-II"));
            writer.write(String.format("%-10s","SL"));
            writer.write(String.format("%-25s","GA"));
            writer.write(String.format("%-10s","SL"));

            writer.newLine();
            if (eachs != null) {
                Arrays.stream(eachs).parallel().forEachOrdered(each -> {

                    var gas = getGA(each.getName());
                    var nsgas = getNSGA(each.getName());

                    Set<String> uniqueFbLbPairs = new HashSet<>();

                    List<Individual> distinctGAs = gas.stream()
                            .filter(individual -> {
                                String fbLbPair = individual.getLb() + "-" + individual.getRatioAccepted();
                                if (uniqueFbLbPairs.contains(fbLbPair)) {
                                    return false; // Nếu cặp Fb và Lb đã xuất hiện, loại bỏ phần tử này
                                } else {
                                    uniqueFbLbPairs.add(fbLbPair);
                                    return true;
                                }
                            })
                            .toList();

                    uniqueFbLbPairs.clear();
                    List<Individual> distinctNSGAs = nsgas.stream()
                            .filter(individual -> {
                                String fbLbPair = individual.getLb() + "-" + individual.getRatioAccepted();
                                if (uniqueFbLbPairs.contains(fbLbPair)) {
                                    return false; // Nếu cặp Fb và Lb đã xuất hiện, loại bỏ phần tử này
                                } else {
                                    uniqueFbLbPairs.add(fbLbPair);
                                    return true;
                                }
                            })
                            .toList();

                    var rsGA = new ArrayList<>(distinctGAs);
                    var rsNSGA = new ArrayList<>(distinctNSGAs);
                    divRankV2(rsGA);
                    divRankV2(rsNSGA);
                    rsGA.removeIf(individual -> individual.getRank() != 0);
                    rsNSGA.removeIf(individual -> individual.getRank() != 0);
                    var index = each.getName().indexOf("requests") - 2;
                    var num = Double.parseDouble(each.getName().substring(index, each.getName().indexOf("requests")));
                    var s1 = findMaxObj(rsGA, num);
                    var s2 = findMaxObj(rsNSGA, num);
//                    System.out.println("GA : " + s1 + "\n" + "NSGA-II : " + s2);
                    Average ag = new Average();
                    ag.setInstance(each.getName());
                    ag.setGa(s1);
                    ag.setNsga(s2);
                    hvResult.add(ag);
                    try {
                        writer.write(String.format("%-30s",each.getName()));
                        writer.write(String.format("%-25s",s2));
                        writer.write(String.format("%-10s",rsNSGA.size()));
                        writer.write(String.format("%-25s",s1));
                        writer.write(String.format("%-10s",rsGA.size()));
                        if(s2 > s1) {
                            writer.write(String.format("%-30s","Good"));
                            count++;
                        } else {
                            writer.write(String.format("%-30s","Bad"));
                        }
                        writer.newLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Good: " + count);

    }

    public static List<Individual> getGA(String pathTest) {
        Random ran = new Random();
        List<Individual> list = new ArrayList<>();
        File dic = new File(pathGA + pathTest);
        if (dic.isDirectory()) {
            File[] files = dic.listFiles();
            if (files != null) {
//                for (int index = 1; index < 10; index++) {
                for (var file : files) {
//                        if (file.getName().endsWith("0." + index + ".json")) {
//                        if(ran.nextInt(100) < 10) {
                    GenGA i = Utils.jsonToObject(file.getPath(), GenGA.class);
                    Individual ind = new Individual();
                    ind.setRatioAccepted(i.getAccepted() / i.getTotal());
                    ind.setLb(1.0 - i.getUtilization());
                    list.add(ind);
//                            break;
                }
//                        if (file.getName().endsWith("0.5" + index + ".json")) {
////                        if(ran.nextInt(100) < 10) {
//                            GenGA i = Utils.jsonToObject(file.getPath(), GenGA.class);
//                            Individual ind = new Individual();
//                            ind.setRatioAccepted(i.getAccepted() / i.getTotal());
//                            ind.setLb(1.0 - i.getUtilization());
//                            list.add(ind);
//                            break;
//                        }
            }
//                }
//            }
        }
        return list;
    }

    public static List<Individual> getNSGA(String pathTest) {
        List<Individual> list = new ArrayList<>();
        File dic = new File(pathNSGA_II + pathTest);
        if (dic.isDirectory()) {
            File[] files = dic.listFiles();
            if (files != null) {
                for (var j : files) {
                    if (j.getName().endsWith(".json")) {
                        GenNSGA i = Utils.jsonToObject(j.getPath(), GenNSGA.class);
                        Individual ind = new Individual();
                        ind.setRatioAccepted(i.getRatioAccepted());
                        ind.setLb(i.getLb());
                        list.add(ind);
                    }
                }
            }
        }
        return list;
    }

    public static void divRankV2(List<Individual> ind) {
        int rank = 0;
        List<Individual> temp = new ArrayList<>();
        while (!ind.isEmpty()) {
            List<Individual> nonDominated = CommonService.nonDominatedRank(ind, rank);
            temp.addAll(nonDominated);
            rank++;
            ind.removeAll(nonDominated);
        }
        ind.addAll(temp);
    }

    public static double findMaxObj(List<Individual> individuals, double num) {
        individuals.sort(Comparator.comparingDouble(Individual::getLb).reversed()); // Sắp xếp theo giá trị Lb
        if(obj.equals("Lb")) {
            individuals.sort(Comparator.comparingDouble(Individual::getLb).reversed());
            return individuals.get(0).getLb();
        } else {
            individuals.sort(Comparator.comparingDouble(Individual::getRatioAccepted).reversed());
            return (double) Math.round(individuals.get(0).getRatioAccepted() * num);
        }
    }


    public static void draw() {
        String[] xData = new String[view.size()];
        double[] NSGA = new double[view.size()];
        double[] GA = new double[view.size()];
        for (int i = 0; i < view.size(); i++) {
            xData[i] = view.get(i).getInstance();
            NSGA[i] = view.get(i).getNsga();
            GA[i] = view.get(i).getGa();
        }

        // Create Chart
        CategoryChart chart = new CategoryChartBuilder().width(1400).height(750).title("").xAxisTitle("Instance").yAxisTitle(obj.equals("Lb") ? "Lb" : "Number Accepted Requests").build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setXAxisLabelRotation(90);
        chart.getStyler().setOverlapped(false); // Ensure bars are not overlapped
        chart.getStyler().setAvailableSpaceFill(0.5);

        // Series data
        List<String> xDataList = List.of(xData);
        List<Double> nsgaDataList = new ArrayList<>();
        List<Double> gaDataList = new ArrayList<>();

        for (double value : NSGA) {
            nsgaDataList.add(value);
        }
        for (double value : GA) {
            gaDataList.add(value);
        }

        // Add series to chart
        chart.addSeries("NSGA-II", xDataList, nsgaDataList);
        chart.addSeries("GA", xDataList, gaDataList);

        // Show it
        new SwingWrapper<>(chart).displayChart();
    }
}
