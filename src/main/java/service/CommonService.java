package service;

import model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CommonService {


    public static void draw(List<Individual> list, List<Double> Lb, List<Double> ratio, int rank) {
        for(int i=0; i<list.size();i++) {
            if(list.get(i).rank == rank) {
                Lb.add(Math.round(list.get(i).Lb * 1000.0) / 1000.0);
                ratio.add(Math.round(list.get(i).ratioAccepted * 1000.0) / 1000.0);
            }
        }
    }

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
        System.out.println(ind.size());
        ind.parallelStream().forEachOrdered(pt -> {
            if(pt.getRank() == 0) {
//                if(pt.Lb == 1.0) {
//                    System.out.print(pt.getOption().values() + " ");
//                }
                System.out.print(Math.round(pt.getFx()*10000.0)/10000.0 + " ");

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




    public static List<Individual> nonDominatedRank(List<Individual> list, int rank) {
        var rs = list.parallelStream()
                .filter(pt -> list.stream().noneMatch(other -> dominates(other, pt)))
                .collect(Collectors.toList());
        rs.parallelStream().forEachOrdered(pt -> {
            pt.setRank(rank);
        });
        return rs;
    }

    public static boolean dominates(Individual p, Individual q) {
        return (p.Lb >= q.Lb && p.ratioAccepted >= q.ratioAccepted && (p.Lb > q.Lb || p.ratioAccepted > q.ratioAccepted));
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
                        try {
                            if (edge.getBandwidth() >= rq.getBandwidth()) {
                                edge.setBandwidth(edge.getBandwidth() - rq.getBandwidth());
                                listEdge.add(edge);
                                cloneGraph.allBandwidth -= rq.getBandwidth();
                            }
                        } catch (NullPointerException e) {
                            System.out.println("error: " + node1.label + " " + vertex.label + " " + rq.getId());
                        }
                    }
                }
                vertexConnect.push(vertex);
            }

        }

        return true;
    }

    public static void checkGraph(NetworkGraph graph, NetworkGraph cloneGraph) {
        int sum =0;
        for(Map.Entry<Vertex, Map<Vertex, Edge>> c : graph.edgeMap.entrySet()) {
            sum+=c.getValue().size();
        }
        int sumQ =0;
        for(Map.Entry<Vertex, Map<Vertex, Edge>> c : cloneGraph.edgeMap.entrySet()) {
            sumQ+=c.getValue().size();
        }
        System.out.println("clone " + sumQ);
        System.out.println("graph " + " " + sum);


        for(Map.Entry<Vertex, Map<Vertex, Edge>> c : graph.edgeMap.entrySet()) {
            var hieu = c.getValue().size() - cloneGraph.edgeMap.get(cloneGraph.vertexMap.get(c.getKey().getLabel())).size();
            System.out.println("node " + c.getKey().label + " " + hieu);

        }
    }
}
