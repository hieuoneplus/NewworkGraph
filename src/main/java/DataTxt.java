import config.Constants;
import model.NetworkGraph;
import model.Request;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class DataTxt {
//    public static void main(String[] args) {
//        getNetwork("src/main/resources/network/cogent_centers_4_network.txt");
//        getRequest("src/main/resources/request/cogent_rural_1_30requests.txt");
//    }

    public static String getRequestPath(String networkName, String numRequest) {

        String cutString = networkName.substring(0, networkName.indexOf("network"));

        // Chuỗi cần nối
        String appendString = numRequest.concat("requests.txt");

        // Nối chuỗi đã cắt với chuỗi mới
        return cutString + appendString;
    }
    public static Map<String, String> getInput() {
        Map<String,String> rs = new HashMap<>();
        String directoryPath = Constants.pathInput;

        File directory = new File(directoryPath);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith("network.txt")) {
                        rs.put(file.getName(),getRequestPath(file.getName(),"10"));
                    }
                }
            }
        } else {
            System.out.println("Đường dẫn không phải là một thư mục.");
        }
        return rs;
    }


    public static List<Request> getRequest(String path) {
        String directoryPath = Constants.pathInput;
        path = directoryPath.concat(path);
        List<Request> rqs = new ArrayList<>();
        File file = new File(path);
        try {
            Scanner sc = new Scanner(file);
            int num = Integer.parseInt(sc.nextLine());
            for(int i=1;i<=num;i++) {
                var pt = sc.nextLine().split(" ");
                var band = Double.parseDouble(pt[2]);
                var mem = Double.parseDouble(pt[3]);
                var cpu = Double.parseDouble(pt[4]);
                var start = pt[5];
                var end = pt[6];
                Queue<String> q = new LinkedList<>();
                if(pt.length == 8) {
                    String[] elementsArray = pt[7].split(",");
                    for (int k = 0;k<elementsArray.length;k++) {
                        q.offer(elementsArray[k]);
                    }
                }
                rqs.add(new Request(String.valueOf(i), start, end, q, cpu, mem, band));
            }
            return rqs;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static NetworkGraph getNetwork(String path) {
        String directoryPath = Constants.pathInput;
        path = directoryPath.concat(path);
        NetworkGraph graph = new NetworkGraph();
        File file = new File(path);
        try {
            Scanner sc = new Scanner(file);
            sc.nextLine();
            int n = Integer.parseInt(sc.nextLine());
            for(int i=1;i<=n;i++) {
                var pt = sc.nextLine().split(" ");
                var label = pt[0];
                var cpu = pt[1];
                ArrayList<String> vnf = new ArrayList<>();
                if(pt.length == 4) {
                    var inputString = pt[3];

                    String elementsString = inputString.substring(1, inputString.length() - 1);

                    // Tách chuỗi thành các phần tử dựa trên dấu phẩy
                    String[] elementsArray = elementsString.split(",");

                    // Tạo ArrayList từ mảng phần tử đã tách
                    vnf = new ArrayList<>(Arrays.asList(elementsArray));
                    graph.addVertex(label,vnf,0.0, Double.parseDouble(cpu));
                } else {
                    graph.addVertex(label, vnf, Double.parseDouble(cpu), 0.0);
                }
            }
            int numEdge = Integer.parseInt(sc.nextLine());
            for(int i=1;i<=numEdge;i++) {
                var pt = sc.nextLine().split(" ");
                var v1 = pt[0];
                var v2 = pt[1];
                var band = pt[2];
                graph.addEdge(v1,v2,Double.parseDouble(band));
            }
            return graph;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
