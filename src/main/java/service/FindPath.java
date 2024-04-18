package service;


import config.Constants;
import model.Edge;
import model.NetworkGraph;
import model.Request;
import model.Vertex;

import java.util.*;

public class FindPath {

    public List<Vertex> nodeToNFV(NetworkGraph graph, Request rq, Vertex sourceName, String sfc) {
        Map<Vertex, Edge> distance = new HashMap<>();
        Map<Vertex, Vertex> previous = new HashMap<>();
        Vertex targetName = null;

        graph.vertexMap.values().parallelStream().forEachOrdered(v -> {
            if (!v.label.equals(sourceName.label)) {
                distance.put(v, new Edge(sourceName.label, sourceName.label, Double.POSITIVE_INFINITY));
            }
        });
        distance.put(sourceName, new Edge(sourceName.label, sourceName.label, 0.0));
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(v -> distance.get(v).getBandwidth()));
        priorityQueue.add(sourceName);

        while (!priorityQueue.isEmpty()) {
            Vertex current = priorityQueue.poll();

            if (current.getFunction().contains(sfc)) {
                targetName = current;
                break;
            }
            for (Map.Entry<Vertex, Edge> neighborEntry : graph.edgeMap.get(current).entrySet()) {
                var neighbor = neighborEntry.getKey();
                double newDistance = distance.get(current).getBandwidth() + neighborEntry.getValue().getBandwidth();
                if (newDistance < distance.get(neighbor).getBandwidth()) {
                    distance.put(neighbor, new Edge(current.label, neighbor.label, newDistance));
                    previous.put(neighbor, current);
                    priorityQueue.add(neighbor);
                }
            }
        }
        List<Vertex> path = new ArrayList<>();
        if (!previous.isEmpty()) {
            var current = targetName;
            while (previous.containsKey(current)) {
                path.add(current);
                current = previous.get(current);
            }
            if (targetName != null) {
                path.add(sourceName);
            }
            Collections.reverse(path);
            return path;
        } else {
            if (targetName != null) {
                path.add(sourceName);
            }
        }
        return path;
    }

    public List<Vertex> dijkstraShortestPath(NetworkGraph graph, Request rq, Vertex sourceName, Vertex targetName) {
        Map<Vertex, Edge> distance = new HashMap<>();
        Map<Vertex, Vertex> previous = new HashMap<>();

        graph.vertexMap.values().parallelStream().forEachOrdered(v -> {
            if (!v.label.equals(sourceName.label)) {
                distance.put(v, new Edge(sourceName.label, sourceName.label, Double.POSITIVE_INFINITY));
            }
        });
        distance.put(sourceName, new Edge(sourceName.label, sourceName.label, 0.0));
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(v -> distance.get(v).getBandwidth()));
        priorityQueue.add(sourceName);

        while (!priorityQueue.isEmpty()) {
            Vertex current = priorityQueue.poll();

            if (current.equals(targetName)) {
                break;
            }

            for (Map.Entry<Vertex, Edge> neighborEntry : graph.edgeMap.get(current).entrySet()) {
                var neighbor = neighborEntry.getKey();
                double newDistance = distance.get(current).getBandwidth() + neighborEntry.getValue().getBandwidth();
                if (newDistance < distance.get(neighbor).getBandwidth()) {
                    distance.put(neighbor, new Edge(current.label, neighbor.label, newDistance));
                    previous.put(neighbor, current);
                    priorityQueue.add(neighbor);
                }
            }

        }

        List<Vertex> path = new ArrayList<>();
        var current = targetName;
        while (previous.containsKey(current)) {
            path.add(current);
            current = previous.get(current);
        }
        if (!previous.isEmpty()) {
            if (targetName != null) {
                path.add(sourceName);
            }
            Collections.reverse(path);
        }
        return path;
    }

    public List<List<List<Vertex>>> findOrder(NetworkGraph graph, Request rq) {
        List<List<List<Vertex>>> path = new ArrayList<>();
        Queue<String> vnf = new LinkedList<>(rq.getVNF());
        boolean noVNF = vnf.isEmpty();
        var current = graph.vertexMap.get(rq.getStart());
        boolean isFirst = true;
        boolean noWays = false;

        while (!vnf.isEmpty()) {
            var sfc = vnf.poll();
            var firstPath = nodeToNFV(graph, rq, current, sfc);

            if (firstPath.size() > 0) {
                current = firstPath.get(firstPath.size() - 1);
                var rs = yenKLargestBandwidthPathsV2(graph, rq, Constants.KSubPath, sfc, firstPath);
                for (var pth : rs) {
                    if (pth.size() != 1 && !isFirst) {
                        pth.remove(0);
                    }
                    if (pth.size() == 1 && !pth.get(0).getFunction().contains(sfc)) {
                        rs.remove(pth);
                        break;
                    }
                }
                if (rs.size() > 0) {
                    path.add(rs);
                    isFirst = false;
                } else {
                    noWays = true;
                }
            } else {
                return path;
            }
        }
        if (!noWays) {
            if (current.equals(graph.vertexMap.get(rq.getEnd()))) {
                if (vnf.isEmpty()) {
                    List<Vertex> one = new ArrayList<>();
                    one.add(current);
                    List<List<Vertex>> noOption = new ArrayList<>();
                    noOption.add(one);
                    path.add(noOption);
                }
                return path;
            } else {
                var firstPath = dijkstraShortestPath(graph, rq, current, graph.getVertex(rq.getEnd()));
                if (firstPath.size() > 0) {
                    var rs = yenKLargestBandwidthPathsV2(graph, rq, Constants.KSubPath, "no", firstPath);
                    for (var pth : rs) {
                        if (pth.size() != 1 && !noVNF) {
                            pth.remove(0);
                        }
                        if (pth.size() == 1 && !pth.get(0).equals(graph.vertexMap.get(rq.getEnd()))) {
                            rs.remove(pth);
                            break;
                        }
                    }
                    if (!rs.isEmpty()) {
                        path.add(rs);
                    }
                } else {
                    return path;
                }
            }
        } else {
            path.clear();
        }
        return path;
    }

    public List<List<Vertex>> yenKLargestBandwidthPaths(NetworkGraph graph, Request rq, int k, String sfc, List<Vertex> firstPath) {
        List<List<Vertex>> result = new ArrayList<>();
        PriorityQueue<Path> candidates = new PriorityQueue<>();
        var source = firstPath.get(0);
        var target = firstPath.get(firstPath.size() - 1);
// Tìm đường đi lớn nhất ban đầu từ nguồn đến đích
        List<Vertex> largestBandwidthPath = firstPath;
// service.GetKPath vaf = new service.GetKPath();
// vaf.getOnePath(largestBandwidthPath);
// Thêm đường đi lớn nhất vào danh sách kết quả

        result.add(largestBandwidthPath);
        for (int i = 1; i <= k; i++) {
// Lặp qua các đỉnh trên đường đi lớn nhất
            for (int j = 0; j < largestBandwidthPath.size() - 1; j++) {
// Loại bỏ cạnh từ đỉnh cuối cùng của đường đi lớn nhất đến đích
                var c = graph.getEdge(largestBandwidthPath.get(j).getLabel(), largestBandwidthPath.get(j + 1).getLabel());
                graph.removeEdge(largestBandwidthPath.get(j).getLabel(), largestBandwidthPath.get(j + 1).getLabel());
                boolean noWay = false;
// Tìm đường đi mới từ nguồn đến đích sau khi loại bỏ cạnh
                List<Vertex> newPath = dijkstraShortestPath(graph, rq, source, target);
// Kiểm tra xem đường đi mới có trùng với các đường đi đã có hay không
                if (!result.contains(newPath)) {
                    if (newPath.size() > 0) {
                        if (newPath.size() != 1 || newPath.get(0).getFunction().contains(sfc)) {
                            result.add(newPath);
                        } else {
                            noWay = true;
                        }
                    } else {
                        noWay = true;
                    }
                }
// Đặt lại cạnh đã loại bỏ
                graph.addEdge(largestBandwidthPath.get(j).getLabel(), largestBandwidthPath.get(j + 1).getLabel(), c);
// Xây dựng ứng viên cho đường đi tiếp theo
                if (!noWay) {
                    List<Vertex> subPath = newPath.subList(0, j + 1);
                    candidates.add(new Path(subPath, graph));
                }
            }
            if (candidates.isEmpty()) {
                break; // Nếu không còn đường đi nào để xem xét thêm, thoát khỏi vòng lặp
            }
// Chọn đường đi mới là đường đi lớn nhất trong ứng viên
            largestBandwidthPath = candidates.poll().getPath();
        }
        return result;
    }

    public List<List<Vertex>> yenKLargestBandwidthPathsV2(NetworkGraph graph, Request rq, int k, String sfc, List<Vertex> firstPath) {
        List<List<Vertex>> result = new ArrayList<>();
        PriorityQueue<Path> candidates = new PriorityQueue<>();
        var target = firstPath.get(firstPath.size() - 1);

        List<Vertex> largestBandwidthPath = firstPath;

        result.add(largestBandwidthPath);
        for (; result.size() < k;) {
            for (int j = 0; j < largestBandwidthPath.size() - 1; j++) {
                List<Edge> removeEdge = new ArrayList<>();
                List<Vertex> removeVertex = new ArrayList<>();

                var rootPath = largestBandwidthPath.subList(0, j + 1);
                var spurNode = largestBandwidthPath.get(j);

                int finalJ = j;
                result.parallelStream().forEachOrdered(p -> {
                    if(p.size() >= finalJ +1) {
                        if (new HashSet<>(p.subList(0, finalJ + 1)).containsAll(rootPath)) {
                            if (graph.hasEdge(p.get(finalJ).getLabel(), p.get(finalJ + 1).getLabel())) {
                                removeEdge.add(graph.edgeMap.get(graph.vertexMap.get(p.get(finalJ).getLabel())).get(graph.vertexMap.get(p.get(finalJ + 1).getLabel())));
                                graph.removeEdge(p.get(finalJ).getLabel(), p.get(finalJ + 1).getLabel());
                            }
                        }
                    }
                });
                rootPath.parallelStream().forEachOrdered(rootPathNode ->{
//                for (var rootPathNode : rootPath) {
                    if (!rootPathNode.equals(spurNode)) {
                        removeVertex.add(graph.vertexMap.get(rootPathNode.getLabel()));
                        removeEdge.addAll(graph.edgeMap.get(graph.vertexMap.get(rootPathNode.getLabel())).values());
                        graph.removeVertex(rootPathNode.getLabel());
                    }
//                }
                });
                boolean noWay = false;
                var spurPath = dijkstraShortestPath(graph, rq, graph.vertexMap.get(spurNode.getLabel()), graph.vertexMap.get(target.getLabel()));

                List<Vertex> totalPath = new ArrayList<>(rootPath);
                if (spurPath.size() > 0) {
                    if (spurPath.size() != 1 || spurPath.get(0).getFunction().contains(sfc)) {
                        totalPath.addAll(spurPath.subList(1, spurPath.size()));;
                    } else {
                        noWay = true;
                    }
                } else {
                    noWay = true;
                }

                removeVertex.parallelStream().forEachOrdered(graph::addOldVertex);
                removeEdge.parallelStream().forEachOrdered(e -> {
                    graph.addEdge(e.v1, e.v2, e.bandwidth);
                });
                if(!noWay) {
                    if (!result.contains(totalPath)) {
                        candidates.add(new Path(totalPath, graph));
                    }
                }

            }
            if(candidates.isEmpty()) {
                break;
            }
            largestBandwidthPath = candidates.poll().getPath();
            if(!result.contains(largestBandwidthPath)) {
                result.add(largestBandwidthPath);
            }

        }
        return result;
    }

    // Định nghĩa lớp Path để sử dụng trong hàng đợi ưu tiên
    private class Path implements Comparable<Path> {
        private final List<Vertex> path;
        private final NetworkGraph graph;

        public Path(List<Vertex> path, NetworkGraph graph) {
            this.path = path;
            this.graph = graph;
        }

        public List<Vertex> getPath() {
            return path;
        }

        @Override
        public int compareTo(Path other) {
            return Double.compare(findMaxBandwidth(path), findMaxBandwidth(other.path));
        }

        // Hàm tính toán băng thông lớn nhất trên đường đi
        private double findMaxBandwidth(List<Vertex> path) {
            double sum = 0;
            for (int i = 0; i < path.size() - 1; i++) {
                Edge edge = graph.edgeMap.get(graph.vertexMap.get(path.get(i).getLabel())).get(graph.vertexMap.get(path.get(i + 1).getLabel()));
                if (edge != null) {
                    sum += edge.getBandwidth();
                }
            }
            return sum;
//            double maxBandwidth = Double.POSITIVE_INFINITY;
//            for (int i = 0; i < path.size() - 1; i++) {
//                Edge edge = graph.edgeMap.get(path.get(i)).get(path.get(i + 1));
//                if (edge != null && edge.getBandwidth() < maxBandwidth) {
//                    maxBandwidth = edge.getBandwidth();
//                }
//            }
//            return maxBandwidth;
        }
    }
}
