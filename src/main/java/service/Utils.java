package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import config.Constants;
import model.RequestMapSerializer;
import serviceexperimantal.model.GenGA;

import java.io.File;
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
    public static void outJson(Object o,String dic, String i) {

        ObjectMapper mapper = new ObjectMapper();

        // Tạo một SimpleModule mới để đăng ký Serializer cho Request
        SimpleModule module = new SimpleModule();
        module.addSerializer(new RequestMapSerializer());
        mapper.registerModule(module);

        String filePath = Constants.pathOutput + dic + "/" + i + ".json";

        // Thử ghi chuỗi JSON vào tệp
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            mapper.writeValue(fileWriter, o);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static <T> T jsonToObject(String path, Class<T> valueType) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(path), valueType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

