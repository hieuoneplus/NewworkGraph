
import config.Constants;
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


        var input = DataTxt.getInput();

        CommonService.createDic(Constants.pathOutput,"");
        input.entrySet().parallelStream().forEach(entry -> {
            try {
                String nameDic = entry.getKey().substring(0, entry.getKey().indexOf(".txt"));
                var graph = DataTxt.getNetwork(entry.getValue());
                var requests = DataTxt.getRequest(entry.getKey());
                requests.sort(Comparator.comparingDouble(request -> {
                    double wCpu = 1.0;
                    double wMemory = 1.0;
                    double wBandwidth = 1.0;

                    // Tính tổng tài nguyên theo công thức
                    return (request.getCpu() * wCpu) + (request.getMemory() * wMemory) + (request.getBandwidth() * wBandwidth);
                }));
                var cloneGraph = graph.copy();
                NSGA_II ag = new NSGA_II();
                ag.createFirstInd(graph, requests);
                ag.createPopulation();
                CommonService.createDic(Constants.pathOutput, nameDic);
                ag.printPathToFile(Constants.pathOutput + nameDic + "/path.txt");
                for (int i = 1; i <= Constants.gSize; i++) {
                    ag.hybrid();
                    ag.mutation();
                    ag.evaluate(cloneGraph);
                    ag.divRankV2();
                    ag.filter();
                }
//            ag.drawImg();
                ag.getIndRankZeroAfterFilter(nameDic);

            } catch (Exception e) {
                System.out.println("error: " + entry.getKey());
            }
        });

    }
}
 