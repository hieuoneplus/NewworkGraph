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
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(v -> distance.get(v).getBandwidth()));

        distance.put(sourceName, new Edge(sourceName.label,sourceName.label, 0.0));

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
                if(!distance.containsKey(neighbor)) {
                    if(graph.edgeMap.get(current).get(neighbor).getBandwidth() >= rq.getBandwidth()
                        && neighbor.getCpu() >= rq.getCpu()
                        && neighbor.getMemory() >= rq.getMemory()) {
                        distance.put(neighbor, new Edge(current.label, neighbor.label, newDistance));
                        previous.put(neighbor, current);
                        priorityQueue.add(neighbor);
                    }
                }
                else if (newDistance < distance.get(neighbor).getBandwidth()
                        && graph.edgeMap.get(current).get(neighbor).getBandwidth() >= rq.getBandwidth()
                        && neighbor.getCpu() >= rq.getCpu()
                        && neighbor.getMemory() >= rq.getMemory()) {
                    distance.put(neighbor, new Edge(current.label, neighbor.label,newDistance));
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
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(v -> distance.get(v).getBandwidth()));

        distance.put(sourceName, new Edge(sourceName.label, sourceName.label, 0.0));

        priorityQueue.add(sourceName);

        while (!priorityQueue.isEmpty()) {
            Vertex current = priorityQueue.poll();
            if (current.equals(targetName)) {
                break;
            }

            for (Map.Entry<Vertex, Edge> neighborEntry : graph.edgeMap.get(current).entrySet()) {
                var neighbor = neighborEntry.getKey();
                double newDistance = distance.get(current).getBandwidth() + neighborEntry.getValue().getBandwidth();
                if(!distance.containsKey(neighbor)) {
                    if(graph.edgeMap.get(current).get(neighbor).getBandwidth() >= rq.getBandwidth()
                            && neighbor.getCpu() >= rq.getCpu()
                            && neighbor.getMemory() >= rq.getMemory()) {
                        distance.put(neighbor, new Edge(current.label, neighbor.label, newDistance));
                        previous.put(neighbor, current);
                        priorityQueue.add(neighbor);
                    }
                }
                else if (newDistance < distance.get(neighbor).getBandwidth()
                        && graph.edgeMap.get(current).get(neighbor).getBandwidth() >= rq.getBandwidth()
                        && neighbor.getCpu() >= rq.getCpu()
                        && neighbor.getMemory() >= rq.getMemory()) {
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
        if(!previous.isEmpty()) {
            if(targetName!=null) {
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
            var firstPath = nodeToNFV(graph,rq, current, sfc);

            if(firstPath.size()>0) {
                current = firstPath.get(firstPath.size() - 1);
                var rs = yenKLargestBandwidthPaths(graph,rq, Constants.KSubPath, sfc, firstPath);
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
                if(vnf.isEmpty()) {
                    List<Vertex> one = new ArrayList<>();
                    one.add(current);
                    List<List<Vertex>> noOption = new ArrayList<>();
                    noOption.add(one);
                    path.add(noOption);
                }
                return path;
            } else {
                var firstPath = dijkstraShortestPath(graph,rq, current, graph.getVertex(rq.getEnd()));
                if(firstPath.size()>0) {
                    var rs = yenKLargestBandwidthPaths(graph,rq, Constants.KSubPath, "no", firstPath);
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
        var target = firstPath.get(firstPath.size()-1);
// Tìm đường đi lớn nhất ban đầu từ nguồn đến đích
        List<Vertex> largestBandwidthPath = firstPath;
// service.GetKPath vaf = new service.GetKPath();
// vaf.getOnePath(largestBandwidthPath);
// Thêm đường đi lớn nhất vào danh sách kết quả

        result.add(largestBandwidthPath);
        for (int i = 1; i < k; i++) {
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
                    if (newPath.size() != 1 || newPath.get(0).getFunction().contains(sfc)) {
                        result.add(newPath);
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
            double maxBandwidth = Double.POSITIVE_INFINITY;
            for (int i = 0; i < path.size() - 1; i++) {
                Edge edge = graph.edgeMap.get(path.get(i)).get(path.get(i + 1));
                if (edge != null && edge.getBandwidth() < maxBandwidth) {
                    maxBandwidth = edge.getBandwidth();
                }
            }
            return maxBandwidth;
        }
    }
}
