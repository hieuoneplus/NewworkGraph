package serviceexperimantal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Individual;
import service.CommonService;
import service.NSGA_II;
import service.Utils;
import serviceexperimantal.model.GenGA;
import serviceexperimantal.model.GenNSGA;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HV {
    public static String pathGA = "src/main/java/data/output/GA/";
    public static String pathNSGA_II = "src/main/java/data/output/NSGA-II/";

    public static String pathTest = "nsf_urban_4_30requests";
    public static double[] point = {0.0,0.0};

    public static void main(String[] args) {
        var gas = getGA();
        var nsgas = getNSGA();

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

        var s1 = calculateHypervolume(rsGA, point);
        var s2 = calculateHypervolume(rsNSGA, point);
        System.out.println("GA : " + s1 + "\n" + "NSGA-II : " + s2);

    }
    public static List<Individual> getGA() {
        List<Individual> list = new ArrayList<>();
        File dic = new File(pathGA + pathTest);
        if(dic.isDirectory()) {
            File[] files = dic.listFiles();
            if(files != null) {
                for(var j : files) {
                    GenGA i = Utils.jsonToObject(j.getPath(), GenGA.class);
                    Individual ind = new Individual();
                    ind.setRatioAccepted(i.getAccepted()/i.getTotal());
                    ind.setLb(1.0 - i.getUtilization());
                    list.add(ind);
                }
            }
        }
        return list;
    }
    public static List<Individual> getNSGA() {
        List<Individual> list = new ArrayList<>();
        File dic = new File(pathNSGA_II + pathTest);
        if(dic.isDirectory()) {
            File[] files = dic.listFiles();
            if(files != null) {
                for(var j : files) {
                    if(j.getName().endsWith(".json")) {
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
    public static double calculateHypervolume(List<Individual> individuals, double[] referencePoint) {
        individuals.sort(Comparator.comparingDouble(Individual::getLb)); // Sắp xếp theo giá trị Lb
        double hypervolume = 0.0;
        double prevFb = referencePoint[1];
        for (int i=0;i<individuals.size();i++) {
            var ind = individuals.get(i);
            if(i != individuals.size()-1) {
                var indLbPre = individuals.get(i+1);
                double leng = Math.abs(indLbPre.getLb() - ind.getLb());
                double width = Math.abs(prevFb - ind.getRatioAccepted());
                hypervolume += leng*width;
            } else {
                double leng = Math.abs(prevFb - ind.getLb());
                double width = Math.abs(prevFb - ind.getRatioAccepted());
                hypervolume += leng*width;
            }
        }
        return hypervolume;
    }
}
