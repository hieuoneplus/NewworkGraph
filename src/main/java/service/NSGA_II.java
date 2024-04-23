package service;

import config.Constants;
import model.Individual;
import model.NetworkGraph;
import model.Request;
import model.Vertex;
import org.knowm.xchart.*;
import service.CommonService;

import java.io.*;
import java.util.*;

public class NSGA_II {
    public static int countNN = 0;
    public static Random ran = new Random();

    public static List<Request> groupRq;

    public static List<Individual> ind = new ArrayList<>();
    public static List<Individual> newPopulation = new ArrayList<>();

    public static Map<Request, List<List<Vertex>>> allPath = new HashMap<>();

    public static List<Map<Request, Integer>> tapnghiem = new ArrayList<>();

    public static int allRequest = 0;

    public static List<Request> cloneGr;
    public static List<Individual> copyGen;

    // Khoi tao ca the dau tien va cac duong di cua cac request
    public static void createFirstInd(NetworkGraph graph, List<Request> group) {
        groupRq = new ArrayList<>(group);
        cloneGr = new ArrayList<>(group);
        allRequest = group.size();
        Individual individual = new Individual();
        Map<Request, Integer> arr = new HashMap<>();
        groupRq.parallelStream().forEachOrdered(rq -> {
            var cloneGraph = graph.copy();
            var path = GetKPath.getV2(cloneGraph, rq);
            if (path != null) {
                if(path.size()>0) {
                    Random rann = new Random();
                    var n = rann.nextInt(path.size());
                    arr.put(rq, n);
                    allPath.put(rq, path);
                } else {
                    cloneGr.remove(rq);
                }
            } else {
                cloneGr.remove(rq);
            }
        });
        if(!tapnghiem.contains(arr)) {
            individual.setOption(arr);
            ind.add(individual);
            tapnghiem.add(arr);
        }
        groupRq = new ArrayList<>(cloneGr);
        cloneGr.clear();
    }

    // khoi tao quan the
    public static void createPopulation() {
        int j = 0;
        for(int i=2; i<= Constants.numberPopulation;) {
            Individual individual = new Individual();
            Map<Request, Integer> arr = new HashMap<>();
            groupRq.parallelStream().forEachOrdered(rq -> {
                if(allPath.containsKey(rq)) {
                    Random rann = new Random();
                    var n = rann.nextInt(allPath.get(rq).size());
                    arr.put(rq, n);
                }
            });
            if(!tapnghiem.contains(arr)) {
                individual.setOption(arr);
                ind.add(individual);
                tapnghiem.add(arr);
                i++;
            } else {
                j++;
            }
            if(j == Constants.outLoop) {
                break;
            }
        }
    }

    // danh gia ca the: Fx, Lb, RatioAccepted
    public static void evaluate(NetworkGraph cloneGraph) {

        ind.parallelStream().forEach(duyet -> {
            Map<Request, Boolean> isAccepted = new HashMap<>();
//            for(var duyet : ind) {
                double count = 0.0;
                NetworkGraph temp = cloneGraph.copy();
                for (var rq : groupRq) {
                    NetworkGraph temp1 = temp.copy();
                    var index = duyet.getOption().get(rq);
                    if(index!=null && allPath.get(rq) != null) {
                        var size = allPath.get(rq).size();
                        if(index < size) {
                            if (CommonService.updateStatusNetwork(temp, rq, allPath.get(rq).get(index))) {
                                isAccepted.put(rq, true);
                                count++;
                            } else {
                                isAccepted.put(rq, false);
                                temp = temp1; // Gán lại giá trị cho mảng tempGraph
                            }
                        } else {
                            countNN++;
                            isAccepted.put(rq, false);
                        }
                    }

                }
                var Lb = (temp.allMemory / cloneGraph.allMemory + temp.allCpu / cloneGraph.allCpu + temp.allBandwidth / cloneGraph.allBandwidth) / 3.0;
                var ratioAccepted = count / ((double) allRequest);
                duyet.setLb(Lb);
                duyet.setRatioAccepted(ratioAccepted);
                duyet.setFx((Constants.alpha * Lb) + ((1 - Constants.alpha) * ratioAccepted));
                duyet.setAccepted(isAccepted);
//            }
        });
    }


    //chia rank
    public static void divRank() {
        int rank = 0;
        List<Individual> temp = new ArrayList<>();
        while (!ind.isEmpty()) {
            temp.addAll(CommonService.setRank(ind, rank));
            rank++;
            ind.removeAll(temp);
        }
        ind.addAll(temp);

        CommonService.Print(ind);
    }

    public static void divRankV2() {
        int rank = 0;
        List<Individual> temp = new ArrayList<>();
        while (!ind.isEmpty()) {
            temp.addAll(CommonService.nonDominatedRank(ind, rank));
            rank++;
            ind.removeAll(temp);
        }
        ind.addAll(temp);

        CommonService.Print(ind);

    }


    //chon loc ca the
    public static void filter(boolean ok) {
            int rank = 0;
            int sum = 0;
            int slotLast = 0;
            boolean notFound = false;
            List<Individual> last = new ArrayList<>();
            do {
                var listRank = CommonService.findInRank(ind, rank);
                if(listRank.size()==0) {
                    notFound = true;
                }
                sum+=listRank.size();
                if(sum <= Constants.numberPopulation) {
                    newPopulation.addAll(listRank);
                    rank++;
                } else {
                    last = listRank;
                    slotLast = Constants.numberPopulation - newPopulation.size();
                }
            } while (sum < Constants.numberPopulation && !notFound);
            if(slotLast != 0) {
                newPopulation.addAll(CommonService.findCroundingDistance(last, slotLast));
            }

            //reset evalute
            newPopulation.parallelStream().forEach(pt -> {
                pt.setRank(0);
                pt.setCrowdingDistance(0.0);
            });
        ind.clear();
        if(ok) {
            setViewAfterFilter();
            for(int i=0; i< newPopulation.size();i++) {
                Utils.outJson(newPopulation.get(i), String.valueOf(i));
            }
        }
    }

    // lai ghep
    public static void hybrid() {
        tapnghiem.clear();
        newPopulation.parallelStream().forEachOrdered(individual -> {
            tapnghiem.add(individual.getOption());
        });
        newPopulation.parallelStream().forEachOrdered(pt -> {
            Random rnn = new Random();
            if(rnn.nextInt(100)<Constants.ratioHyrid) {
                int out = 0;
                int cha = newPopulation.indexOf(pt);
                int me;
                do {
                    me = rnn.nextInt(newPopulation.size());
                    out++;
                    if(out==Constants.outLoop) {
                        break;
                    }
                } while (me == cha);

                var dad = newPopulation.get(cha);
                var mom = newPopulation.get(me);


                Individual in1 = new Individual();
                Individual in2 = new Individual();
                Map<Request, Integer> map1 = new HashMap<>(dad.getOption());
                Map<Request, Integer> map2 = new HashMap<>(mom.getOption());

                dad.getOption().entrySet().parallelStream().forEachOrdered(option -> {
                    Random rann = new Random();
                    if (rann.nextInt(3) == 1) {
                        var temp = option.getValue();
                        map1.put(option.getKey(), map2.get(option.getKey()));
                        map2.put(option.getKey(), temp);
                    }
                });


                if (!tapnghiem.contains(map1)) {
                    tapnghiem.add(map1);
                    in1.setOption(map1);
                    ind.add(in1);
                }
                if (!tapnghiem.contains(map2)) {
                    tapnghiem.add(map2);
                    in2.setOption(map2);
                    ind.add(in2);
                }
            }
        });
        copyGen = new ArrayList<>(ind);

    }

    //dot bien
    public static void mutation() {
        copyGen.parallelStream().forEachOrdered(pt -> {
            Random rann = new Random();
            if(rann.nextInt(100) <= Constants.ratioMutation) {
                var bit = rann.nextInt(groupRq.size());
                int bitTemp;
                do {
                    bitTemp = rann.nextInt(groupRq.size());
                } while(bit == bitTemp);
                var index = rann.nextInt(allPath.get(groupRq.get(bit)).size());
                var indexSwap = rann.nextInt(allPath.get(groupRq.get(bitTemp)).size());
                Individual evol = new Individual();
                evol.setOption(new HashMap<>(pt.getOption()));
                evol.getOption().put(groupRq.get(bit), index);
                evol.getOption().put(groupRq.get(bitTemp), indexSwap);

                if(!tapnghiem.contains(evol.getOption())) {
                    tapnghiem.add(evol.getOption());
                    ind.add(evol);
                }
            }

        });
        ind.addAll(newPopulation);
        newPopulation.clear();
    }

    public static void drawImg() {
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Pareto").xAxisTitle("RatioAccepted").yAxisTitle("Lb").build();
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);

        int rank = 0;
        for(var i : ind) {
            if(i.rank == rank) {
                List<Double> Lb = new ArrayList<>();
                List<Double> ratio = new ArrayList<>();
                CommonService.draw(ind, Lb, ratio, rank);

                // Thêm dữ liệu vào biểu đồ

                    // Nếu không phải lần lặp đầu tiên, nối điểm hiện tại với điểm trước đó
                chart.addSeries("Rank " + rank, ratio, Lb);//.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);


                rank++;
            }
        }
        new SwingWrapper<>(chart).displayChart();

    }
    public static void printPathToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (var rq : groupRq) {
                if(allPath.containsKey(rq)) {
                    // In ra ID của yêu cầu
                    writer.write("Request id " + rq.getId());
                    writer.newLine();

                    // In ra các đường đi cho từng yêu cầu sử dụng phương thức getMorePath()
                    GetKPath.getMorePath(allPath.get(rq), writer);

                    // In ra dấu phân cách sau mỗi lần lặp
                    writer.write("---------------");
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void setViewAfterFilter() {
        newPopulation.parallelStream().forEachOrdered(individual -> {
            Map<Request, String> view = new HashMap<>();
            individual.getOption().keySet().parallelStream().forEachOrdered(key -> {
                var index = individual.getOption().get(key);
                var size = allPath.get(key).size();
                if(index < size) {
                    var path = allPath.get(key).get(index);
                    if (path != null) {
                        if(individual.getAccepted().get(key)) {
                            ArrayList<String> ad = new ArrayList<>();
                            for (int each = 0; each < path.size(); each++) {
                                ad.add(path.get(each).label);
                            }
                            view.put(key, ad.toString());
                        }
                    }
                }
            });
            individual.setView(view);
        });
    }
}

