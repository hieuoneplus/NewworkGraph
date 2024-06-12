package model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class RequestMapSerializer extends JsonSerializer<Map<Request, String>> {
    @Override
    public void serialize(Map<Request, String> option, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        for (Map.Entry<Request, String> entry : option.entrySet()) {
            // Sử dụng ID của Request làm key trong JSON
            jsonGenerator.writeStringField(entry.getKey().getId(), entry.getValue().toString());
        }
        jsonGenerator.writeEndObject();
    }
    @Override
    public Class<Map<Request, String>> handledType() {
        return (Class<Map<Request, String>>) (Class<?>) Map.class;
    }
}
