package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class NetworkGraph {
    public final Map<Vertex, Map<Vertex, Edge>> edgeMap;
    public final Map<String, Vertex> vertexMap;

    public Double allCpu;
    public Double allMemory;
    public Double allBandwidth;
    public NetworkGraph() {
        vertexMap = new HashMap<>();
        edgeMap = new HashMap<>();
        allCpu = 0.0;
        allBandwidth = 0.0;
        allMemory = 0.0;
    }
    public NetworkGraph copy() {
        NetworkGraph newGraph = new NetworkGraph();

        // Sao chép vertexMap
        for (Map.Entry<String, Vertex> entry : this.vertexMap.entrySet()) {
            String key = entry.getKey();
            Vertex value = entry.getValue().copy(); // Assume model.Vertex has a copy method
            newGraph.vertexMap.put(key, value);
        }

        // Sao chép edgeMap
        for (Map.Entry<Vertex, Map<Vertex, Edge>> entry : this.edgeMap.entrySet()) {

            Vertex key = newGraph.vertexMap.get(entry.getKey().getLabel()); // Assume model.Vertex has a copy method
            Map<Vertex, Edge> innerMap = entry.getValue();
            Map<Vertex, Edge> newInnerMap = new HashMap<>();
            for (Map.Entry<Vertex, Edge> innerEntry : innerMap.entrySet()) {
                Vertex innerKey = newGraph.vertexMap.get(innerEntry.getKey().getLabel());
                Edge innerValue = innerEntry.getValue().copy();//new Edge();
//                innerValue.setBandwidth(innerEntry.getValue().getBandwidth());
//                innerValue.setV1(innerEntry.getValue().getV1());
//                innerValue.setV2(innerEntry.getValue().getV2());
                newInnerMap.put(innerKey, innerValue);
            }
            newGraph.edgeMap.put(key, newInnerMap);

        }
        newGraph.allCpu = this.allCpu.doubleValue();
        newGraph.allMemory = this.allMemory.doubleValue();
        newGraph.allBandwidth = this.allBandwidth.doubleValue();
        return newGraph;
    }

    public void addVertex(String label, ArrayList<String> function, Double resource, Double cpu) {
        Vertex v = new Vertex(label, function, resource, cpu);
        vertexMap.put(label, v);
        edgeMap.put(v, new HashMap<>());
        allCpu += cpu;
        allMemory += resource;
    }
    public void addOldVertex(Vertex v) {

        vertexMap.put(v.getLabel(), v);
        edgeMap.put(v, new HashMap<>());
        allCpu += v.cpu;
        allMemory += v.memory;
    }
    public Vertex getVertex(String label) {
        return vertexMap.get(label);
    }
    public void addEdge(String label1, String label2, double bandwidth) {
        boolean addSuccess = false;
        Vertex v1 = vertexMap.get(label1);
        Vertex v2 = vertexMap.get(label2);
        if (v1 == null || v2 == null) {
//            System.out.println("Không thể thêm cạnh vì một hoặc cả hai đỉnh không tồn tại.");
            return;
        }
        if (!edgeMap.containsKey(v1)) {
            Map<Vertex, Edge> canh1 = new HashMap<>();
            var edge = new Edge(v1.label, v2.label, bandwidth);
            canh1.put(v2, edge);
            if(!edgeMap.containsKey(v2)) {
                Map<Vertex, Edge> canh2 = new HashMap<>();
                canh2.put(v1, edge);
                edgeMap.put(v2, canh2);
            } else {
                edgeMap.get(v2).put(v1, edge);
            }
            edgeMap.put(v1, canh1);
            addSuccess = true;
        } else if (!edgeMap.get(v1).containsKey(v2)) {
            var edge = new Edge(v1.label, v2.label, bandwidth);
            edgeMap.get(v1).put(v2, edge);
            if (!edgeMap.containsKey(v2)) {
                edgeMap.put(v2, new HashMap<>());
            }
            edgeMap.get(v2).put(v1, edge);
            addSuccess = true;
        } else {
//            System.out.println("Cạnh đã tồn tại.");
        }
        if(addSuccess) {
            allBandwidth += bandwidth;
        }
    }
    public Double getEdge(String label1, String label2) {
        var c = vertexMap.get(label1);
        var d = vertexMap.get(label2);
        if (edgeMap.get(c) != null) {
            if (edgeMap.get(c).get(d) != null) {
                return edgeMap.get(c).get(d).getBandwidth();
            } else {
                return 0.0;
            }
        }
        return 0.0;
    }
    public void removeVertex(String label) {
        Vertex v = vertexMap.get(label);
        var c = edgeMap.get(v);
        if (c != null) {
            ArrayList<Vertex> list = new ArrayList<>(c.keySet());
            for (Vertex pt : list) {
                allBandwidth -= edgeMap.get(pt).get(v).getBandwidth();
                edgeMap.get(pt).remove(v);
            }
        }
        edgeMap.remove(v);
        allMemory -= v.getMemory();
        allCpu -= v.getCpu();
        vertexMap.remove(label);
    }
    public void removeEdge(String label1, String label2) {
        Vertex v1 = vertexMap.get(label1);
        Vertex v2 = vertexMap.get(label2);
        if (edgeMap.get(v1) != null) {
            if(edgeMap.get(v1).get(v2) != null) {
                allBandwidth -= edgeMap.get(v1).get(v2).getBandwidth();
                edgeMap.get(v1).remove(v2);
//            if (edgeMap.get(v1).size() == 0) {
//                edgeMap.remove(v1);
//            }
            }
        }
        if (edgeMap.get(v2) != null) {
            edgeMap.get(v2).remove(v1);
//            if (edgeMap.get(v2).size() == 0) {
//                edgeMap.remove(v2);
//            }
        }
    }
    public void editBandWidth(String label1, String label2, double bandwidth) {
        Vertex v1 = vertexMap.get(label1);
        Vertex v2 = vertexMap.get(label2);
        if (v1 == null || v2 == null) {
            System.out.println("Không edit cạnh vì một hoặc cả hai đỉnh không tồn tại.");
            return;
        }
        if (edgeMap.containsKey(v1)) {
            if (edgeMap.get(v1).containsKey(v2)) {
                edgeMap.get(v1).get(v2).setBandwidth(bandwidth);
            }
        }
    }
    public void editLabel(String label1, String label2) {
        Vertex v1 = vertexMap.get(label1);
        if (v1 == null) {
            System.out.println("Không edit cạnh vì một hoặc cả hai đỉnh không tồn tại.");
            return;
        }
        v1.setLabel(label2);
        vertexMap.remove(label1);
        vertexMap.put(label2, v1);
    }
    public List<Vertex> getAllVertex() {
        List<Vertex> list = new ArrayList<>();
        list.addAll(vertexMap.values());
        return list;
    }
    public boolean hasEdge(String v1, String v2) {
        if(edgeMap.get(vertexMap.get(v1)) != null) {
            if(edgeMap.get(vertexMap.get(v1)).get(vertexMap.get(v2)) != null) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
 