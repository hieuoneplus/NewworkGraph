import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.NetworkGraph;
import model.Request;
import service.CommonService;
import service.GetKPath;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

public class JsonParse {

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Đọc tệp JSON vào một đối tượng model.Request
            Request request = mapper.readValue(new File("src/main/resources/graph.json"), new TypeReference<Request>() {
            });

            // Sử dụng đối tượng request theo cách bình thường
            System.out.println("Start: " + request.getStart());
            System.out.println("End: " + request.getEnd());
            System.out.println("VNF: " + request.getVNF());
            System.out.println("CPU: " + request.getCpu());
            System.out.println("Memory: " + request.getMemory());
            System.out.println("Bandwidth: " + request.getBandwidth());
        } catch (IOException e) {
            e.printStackTrace();
        }



        ///////////////////////////
        var graph = DataTxt.getNetwork("src/main/resources/network/cogent_centers_4_network.txt");
        var requests = DataTxt.getRequest("src/main/resources/request/cogent_rural_1_30requests.txt");
        requests.sort(Comparator.comparingDouble(request -> {
            double wCpu = 1.0;
            double wMemory = 1.0;
            double wBandwidth = 1.0;

            // Tính tổng tài nguyên theo công thức
            return (request.getCpu() * wCpu) + (request.getMemory() * wMemory) + (request.getBandwidth() * wBandwidth);
        }));

        NetworkGraph cloneGraph = graph.copy();
        Request rq = null;
        for(var pt : requests) {
            if (pt.getId().equals("11")) {
                rq = pt;
                break;
            }
        }
        var tes = GetKPath.getV2(graph, rq);
        for(var c : tes)
            CommonService.updateStatusNetwork(cloneGraph, rq , c);
        GetKPath.getMorePathV2(tes);


    }

}
