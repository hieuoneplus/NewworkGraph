package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import model.RequestMapSerializer;

import java.io.FileWriter;
import java.io.IOException;

public class Utils {
    public static String toJson(Object o) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            //Converting the Object to JSONString
            String jsonString = mapper.writeValueAsString(o);
            return jsonString;
        } catch (Exception e) {

        }
        return null;
    }
    public static void outJson(Object o, String i) {

        ObjectMapper mapper = new ObjectMapper();

        // Tạo một SimpleModule mới để đăng ký Serializer cho Request
        SimpleModule module = new SimpleModule();
        module.addSerializer(new RequestMapSerializer());
        mapper.registerModule(module);

        String filePath = "src/main/java/data/output/NSGA-II/output_" + i + ".json";

        // Thử ghi chuỗi JSON vào tệp
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            mapper.writeValue(fileWriter, o);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

