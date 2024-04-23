
import model.NetworkGraph;
import model.Request;
import service.*;

import java.util.*;


public class Main {
    //Run với bộ cogent quá tải
    public static void main(String[] args) {
//        NetworkGraph graph = new NetworkGraph();
//        ArrayList<String> s = new ArrayList<>();
//        s.add("f1");
//        s.add("f2");
//        ArrayList<String> s1 = new ArrayList<>();
//        s1.add("f2");
//        s1.add("f4");
//        ArrayList<String> s2 = new ArrayList<>();
//        s2.add("f3");
//        s2.add("f5");
//
//        graph.addVertex("1", new ArrayList<>(),50.0, 60.0);
//        graph.addVertex("2", s,50.0, 60.0);
//        graph.addVertex("3",new ArrayList<>(),50.0, 60.0);
//        graph.addVertex("4", new ArrayList<>(),50.0, 60.0);
//        graph.addVertex("5", new ArrayList<>(),50.0, 60.0);
//        graph.addVertex("6",s2,50.0, 60.0);
//        graph.addVertex("7", new ArrayList<>(),50.0, 60.0);
//        graph.addVertex("8", new ArrayList<>(),50.0, 60.0);
//        graph.addVertex("9",new ArrayList<>(),50.0, 60.0);
//        graph.addVertex("10",s1,50.0, 60.0);
//        graph.addEdge("1", "2", 50.0);
//        graph.addEdge("1", "9", 10.0);
//        graph.addEdge("1", "8", 40.0);
//        graph.addEdge("2", "9", 20.0);
//        graph.addEdge("2", "3", 90.0);
//        graph.addEdge("3", "4", 20.0);
//        graph.addEdge("4", "9", 20.0);
//        graph.addEdge("4", "5", 70.0);
//        graph.addEdge("5", "6", 20.0);
//        graph.addEdge("5", "10", 30.0);
//        graph.addEdge("6", "10", 50.0);
//        graph.addEdge("6", "7", 60.0);
//        graph.addEdge("7", "8", 70.0);
//        graph.addEdge("8", "10", 10.0);
//        graph.addEdge("8", "9", 40.0);



//        ListRequest requests = null;
//        GraphInput input = null;
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            // Đọc tệp JSON vào một đối tượng model.Request
//                requests = mapper.readValue(new File("src/main/resources/rq.json"), new TypeReference<>() {
//            });
//                input = mapper.readValue(new File("src/main/resources/graph.json"), new TypeReference<>() {
//                });
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        var graph = CommonService.convert2Graph(input.getVertexList(), input.getEdgeList());
//
//        var cloneGraph = graph.copy();
//        requests.getRequests().sort(Comparator.comparingDouble(request -> {
//            double wCpu = 1.0;
//            double wMemory = 1.0;
//            double wBandwidth = 1.0;
//
//            // Tính tổng tài nguyên theo công thức
//            return (request.getCpu() * wCpu) + (request.getMemory() * wMemory) + (request.getBandwidth() * wBandwidth);
//        }));

        var graph = DataTxt.getNetwork("src/main/resources/network/cogent_rural_1_network.txt");
        var requests = DataTxt.getRequest("src/main/resources/request/cogent_rural_1_30requests.txt");
        requests.sort(Comparator.comparingDouble(request -> {
            double wCpu = 1.0;
            double wMemory = 1.0;
            double wBandwidth = 1.0;

            // Tính tổng tài nguyên theo công thức
            return (request.getCpu() * wCpu) + (request.getMemory() * wMemory) + (request.getBandwidth() * wBandwidth);
        }));
        var cloneGraph = graph.copy();
//        FindPath f = new FindPath();

//        GetKPath.getOnePath(f.dijkstraShortestPath(graph,new Request(), graph.vertexMap.get("7"), graph.vertexMap.get("6")));
        //GetKPath.getMorePathV2(f.yenKLargestBandwidthPathsV2(graph, new Request(), 12, "f2", f.dijkstraShortestPath(graph,new Request(), graph.vertexMap.get("8"), graph.vertexMap.get("10"))));
//        Queue<String> q = new LinkedList<>();
//        q.add("f1");
//        q.add("f2");
//        q.add("f3");
//        Request r = new Request("1", "1","2",q,1.0,1.0,1.0);
//
//        GetKPath.getMorePathV2(GetKPath.getV2(graph, r));
        NSGA_II.createFirstInd(graph, requests);
        NSGA_II.createPopulation();
        NSGA_II.printPathToFile("src/main/resources/path.txt");
        for(int i=0;i<1000;i++) {
            NSGA_II.evaluate(cloneGraph);
            NSGA_II.divRankV2();
            NSGA_II.filter(false);
            NSGA_II.hybrid();
            NSGA_II.mutation();
        }
        NSGA_II.evaluate(cloneGraph);
        NSGA_II.divRankV2();
        NSGA_II.drawImg();
        NSGA_II.filter(true);
        System.out.println(NSGA_II.countNN);
    }
}
 