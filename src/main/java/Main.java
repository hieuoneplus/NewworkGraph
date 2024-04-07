
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;
import service.CommonService;
import service.NSGA_II;

import java.io.File;
import java.io.IOException;
import java.util.*;
public class Main {
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



        ListRequest requests = null;
        GraphInput input = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Đọc tệp JSON vào một đối tượng model.Request
                requests = mapper.readValue(new File("src/main/resources/rq.json"), new TypeReference<>() {
            });
                input = mapper.readValue(new File("src/main/resources/graph.json"), new TypeReference<>() {
                });

        } catch (IOException e) {
            e.printStackTrace();
        }

        var graph = CommonService.convert2Graph(input.getVertexList(), input.getEdgeList());

        NetworkGraph cloneGraph = graph.copy();

        requests.getRequests().sort(Comparator.comparingDouble(Request::getBandwidth));

        NSGA_II.createFirstInd(graph, requests.getRequests());
        NSGA_II.createPopulation();
        NSGA_II.printPathToFile("src/main/resources/path.txt");
        for(int i=0;i<100;i++) {
            NSGA_II.evaluate(cloneGraph);
//            NSGA_II.divRank();
            NSGA_II.divRankV2();
            NSGA_II.filter();
            NSGA_II.hybrid();
            NSGA_II.mutation();
        }
        NSGA_II.evaluate(cloneGraph);
        NSGA_II.divRankV2();
        NSGA_II.drawImg();

    }
}
 