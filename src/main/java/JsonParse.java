import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Request;

import java.io.File;
import java.io.IOException;

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
    }

}
