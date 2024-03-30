package service;

import config.Constants;
import model.Individual;
import model.NetworkGraph;
import model.Request;
import model.Vertex;
import service.CommonService;

import java.util.*;

public class NSGA_II {
    public static Random ran = new Random();

    public static List<Request> groupRq;

    public static List<Individual> ind = new ArrayList<>();
    public static List<Individual> newPopulation = new ArrayList<>();

    public static Map<Request, List<List<Vertex>>> allPath = new HashMap<>();

    public static List<Map<Request, Integer>> tapnghiem = new ArrayList<>();

    public static int allRequest = 0;

    public static List<Request> cloneGr;

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
//            for(var duyet : ind) {
                double count = 0.0;
                NetworkGraph temp = cloneGraph.copy();
                for (var rq : groupRq) {
                    NetworkGraph temp1 = temp.copy();
                    var index = duyet.getOption().get(rq);
                    if(index!=null && allPath.get(rq) != null) {
                        var size = allPath.get(rq).size() - 1;
                        if (CommonService.updateStatusNetwork(temp, rq, allPath.get(rq).get(index > size ? size : index))) {
                            count++;
                        } else {
                            temp = temp1; // Gán lại giá trị cho mảng tempGraph
                        }
                    }

                }
                var Lb = (temp.allMemory / cloneGraph.allMemory + temp.allCpu / cloneGraph.allCpu + temp.allBandwidth / cloneGraph.allBandwidth) / 3.0;
                var ratioAccepted = count / ((double) allRequest);
                duyet.setLb(Lb);
                duyet.setRatioAccepted(ratioAccepted);
                duyet.setFx((Constants.alpha * Lb) + (Constants.alpha * ratioAccepted));
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


    //chon loc ca the
    public static void filter() {
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

    }

    // lai ghep
    public static void hybrid() {
        tapnghiem.clear();
        newPopulation.parallelStream().forEachOrdered(individual -> {
            tapnghiem.add(individual.getOption());
        });
        newPopulation.parallelStream().forEachOrdered(pt -> {
            Random rnn = new Random();
//            if(rnn.nextInt(2)==0) {
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
                    in1.setOption(map1);
                    ind.add(in1);
                }
                if (!tapnghiem.contains(map2)) {
                    in2.setOption(map2);
                    ind.add(in2);
                }
//            }
        });


    }

    //dot bien
    public static void mutation() {
        newPopulation.parallelStream().forEachOrdered(pt -> {
            Random rann = new Random();
//            if(rann.nextInt(2) == 0) {
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

                ind.add(evol);
//            }

        });
        ind.addAll(newPopulation);
        newPopulation.clear();
    }



}

