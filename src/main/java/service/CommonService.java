package service;

import model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CommonService {

    public static NetworkGraph convert2Graph(List<Vertex> vertexList, List<Edge> edgeList) {
        NetworkGraph networkGraph = new NetworkGraph();
        vertexList.parallelStream().forEachOrdered(vertex -> {
            networkGraph.addVertex(vertex.getLabel(), vertex.getFunction(), vertex.memory, vertex.cpu);
        });
        edgeList.parallelStream().forEachOrdered(edge -> {
            networkGraph.addEdge(edge.v1, edge.v2, edge.bandwidth);
        });
        return networkGraph;
    }
    public static void Print(List<Individual> ind) {
        ind.parallelStream().forEachOrdered(pt -> {
            if(pt.getRank() == 0) {
                System.out.print(Math.round(pt.getFx()*10000.0)/10000.0 + " ");
//            System.out.print(pt.getOption().values() + " ");
            }
        });
        System.out.println();

    }

    /**
     * Calculate crounding-distance for individual in last rank
     * @param list
     * @param slotLast
     * @return
     */
    public static List<Individual> findCroundingDistance(List<Individual> list, int slotLast) {

        list.sort(Comparator.comparingDouble(Individual::getLb));

        list.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
        list.get(list.size() - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);


        var LbRange = list.get(list.size()-1).getLb() - list.get(0).getLb();
        var AcceptedRange = list.get(0).getRatioAccepted() - list.get(list.size()-1).getRatioAccepted();

        var cloneList = new ArrayList<>(list);
        cloneList.sort(Comparator.comparingDouble(Individual::getLb));
        cloneList.remove(cloneList.get(0));
        cloneList.remove(cloneList.get(cloneList.size()-1));
        if(!cloneList.isEmpty()) {
            cloneList.parallelStream().forEachOrdered(pt -> {
                pt.crowdingDistance += ((list.get(list.indexOf(pt) + 1).Lb - list.get(list.indexOf(pt) - 1).Lb) / LbRange) + ((list.get(list.indexOf(pt) - 1).ratioAccepted - list.get(list.indexOf(pt) + 1).ratioAccepted) / AcceptedRange);
            });
        }
        list.sort(Comparator.comparingDouble(Individual::getCrowdingDistance).reversed());

        return list.subList(0, slotLast);
    }


    /**
     * Find individuals have the value rank
     * @param individuals
     * @param rank
     * @return
     */
    public static List<Individual> findInRank(List<Individual> individuals, int rank) {
        List<Individual> pt = new ArrayList<>();
        individuals.parallelStream().forEachOrdered(individual -> {
            if(individual.getRank() == rank) {
                pt.add(individual);
            }
        });
        return pt;
    }


    /**
     * Set rank for individual
     * @param indd
     * @param rank
     * @return
     */
    public static List<Individual> setRank(List<Individual> indd, int rank) {
        if (indd.isEmpty()) {
            throw new IllegalArgumentException("List is empty");
        }

        // Bước 1: Tìm giá trị Fx lớn nhất
        double maxFx = indd.stream().parallel()
                .mapToDouble(Individual::getFx)
                .max()
                .orElseThrow();

        // Bước 2: Lấy ra tất cả các phần tử có Fx bằng với giá trị lớn nhất
        List<Individual> maxFxIndividuals = indd.stream()
                .filter(indivisual -> indivisual.getFx() == maxFx)
                .toList();

        // Bước 3: Tính toán rank cho các đối tượng có giá trị Fx lớn nhất
        AtomicInteger rankSet = new AtomicInteger(rank);
        maxFxIndividuals.parallelStream().forEach(indivisual -> indivisual.setRank(rankSet.get()));
        return maxFxIndividuals;
    }

    /**
     * Update status Network
     * @param cloneGraph
     * @param rq
     * @param path
     * @return true if the path of request is accepted
     */
    public static boolean updateStatusNetwork(NetworkGraph cloneGraph, Request rq, List<Vertex> path) {
        Queue<Vertex> pathResolve = new LinkedList<>(path);
        Queue<String> vnf = new LinkedList<>(rq.getVNF());
        Stack<Vertex> vertexConnect = new Stack<>();
        List<Edge> listEdge = new ArrayList<>();

        var firstVertex = cloneGraph.getVertex(pathResolve.poll().getLabel());
        if(firstVertex.getFunction().contains(vnf.peek())) {
            if(firstVertex.getCpu() >= rq.getCpu() && firstVertex.getMemory() >= rq.getMemory()) {
                firstVertex.setCpu(firstVertex.getCpu() - rq.getCpu());
                cloneGraph.allCpu -= rq.getCpu();
                firstVertex.setMemory(firstVertex.getMemory() - rq.getMemory());
                cloneGraph.allMemory -= rq.getMemory();
                vnf.poll();
            } else {
                return false;
            }
        } else {
            if(firstVertex.getMemory() >= rq.getMemory()) {
                firstVertex.setMemory(firstVertex.getMemory() - rq.getMemory());
                cloneGraph.allMemory -= rq.getMemory();
            } else {
                return false;
            }
        }
        vertexConnect.push(firstVertex);

        while(!pathResolve.isEmpty()) {
            var vertex = cloneGraph.getVertex(pathResolve.poll().getLabel());


            if(vertex.getFunction().contains(vnf.peek())) {
                if(vertex.getCpu() >= rq.getCpu() && vertex.getMemory() >= rq.getMemory()) {
                    vertex.setCpu(vertex.getCpu() - rq.getCpu());
                    cloneGraph.allCpu -= rq.getCpu();
                    vertex.setMemory(vertex.getMemory() - rq.getMemory());
                    cloneGraph.allMemory -= rq.getMemory();
                    vnf.poll();
                } else {
                    return false;
                }
            } else {
                if(vertex.getMemory() >= rq.getMemory()) {
                    vertex.setMemory(vertex.getMemory() - rq.getMemory());
                    cloneGraph.allMemory -= rq.getMemory();
                } else {
                    return false;
                }
            }
            if(vertexConnect.size()>0) {
                var node1 = vertexConnect.peek();
                if(!node1.equals(vertex)) {
                    var cc = cloneGraph.edgeMap.get(node1);
                    Edge edge = cloneGraph.edgeMap.get(node1).get(vertex);
                    if (!listEdge.contains(edge)) {
                        if (edge.getBandwidth() >= rq.getBandwidth()) {
                            edge.setBandwidth(edge.getBandwidth() - rq.getBandwidth());
                            listEdge.add(edge);
                            cloneGraph.allBandwidth -= rq.getBandwidth();
                        }

                    }
                }
                vertexConnect.push(vertex);
            }

        }

        return true;
    }
}
