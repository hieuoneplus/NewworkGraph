package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
}
