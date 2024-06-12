package serviceexperimantal;

import model.Individual;
import service.CommonService;
import service.Utils;
import serviceexperimantal.model.Average;
import serviceexperimantal.model.GenGA;
import serviceexperimantal.model.GenNSGA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Cmetric {
    public static String numK = "10";
    public static String pathGA = "src/main/java/data/output/GAk" + numK +"/";
    public static String pathNSGA_II = "src/main/java/data/output/NSGA-IIk" + numK + "/";

    public static String pathTest = "src/main/java/data/output/C_Metric_result_K" + numK + ".txt";

    public static String pathAvg = "src/main/java/data/output/C_Metric_average_K" + numK + ".txt";

    public static int count = 0;


    public static List<Average> C_metric_Result = new ArrayList<>();
    public static List<Average> exist = new ArrayList<>();
    public static void main(String[] args) {
        excute();
        calcAvg();
    }
    public static void calcAvg() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathAvg))) {
            writer.write(String.format("%-30s","Instance"));
            writer.write(String.format("%-25s","NSGA-II / GA"));

            writer.newLine();
            for (var pt : C_metric_Result) {
                if (!exist.contains(pt)) {
                    exist.add(pt);
                    double avgNSGA = pt.getNsga();
                    String[] sub = pt.getInstance().split("_");
                    var name = sub[0].concat("_".concat(sub[1]));
                    for (var other : C_metric_Result) {
                        if (!pt.equals(other) && !exist.contains(other) && other.getInstance().startsWith(name) && other.getInstance().endsWith(sub[3])) {
                            avgNSGA += other.getNsga();
                            exist.add(other);
                        }
                    }
                    avgNSGA = Math.round(10000.0*avgNSGA / 5.0)/10000.0;

                    writer.write(String.format("%-30s",sub[0] + "_" + sub[1] + "_" + sub[3]));
                    writer.write(String.format("%-25s",avgNSGA));

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
            writer.write(String.format("%-25s","NSGA-II / GA"));

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

                    var cmetric = calculateCMetric(rsNSGA, rsGA);
//                    System.out.println("GA : " + s1 + "\n" + "NSGA-II : " + s2);
                    Average ag = new Average();
                    ag.setInstance(each.getName());
                    ag.setNsga(cmetric);
                    C_metric_Result.add(ag);
                    try {
                        writer.write(String.format("%-30s",each.getName()));
                        writer.write(String.format("%-25s",cmetric));

                        if(cmetric > 0.0) {
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

    public static double calculateCMetric(List<Individual> alg1, List<Individual> alg2) {
        int count = 0;
        for (Individual ind2 : alg2) {
            for (Individual ind1 : alg1) {
                if (CommonService.dominates(ind1, ind2)) {
                    count++;
                    break;
                }
            }
        }
        int count1 = 0;
        for (Individual ind1 : alg1) {
            for (Individual ind2 : alg2) {
                if (CommonService.dominates(ind2, ind1)) {
                    count1++;
                    break;
                }
            }
        }

        var C1 = (double) count / alg2.size();
        var C2 = (double) count1 / alg1.size();
        return C1 - C2;
    }
}
