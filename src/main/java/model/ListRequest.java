package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Request;

import java.util.List;

@Data
@NoArgsConstructor
public class ListRequest {
    @JsonProperty("requests")
    private List<Request> requests;
}
