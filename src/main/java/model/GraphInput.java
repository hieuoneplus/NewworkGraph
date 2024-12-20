package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GraphInput {
    @JsonProperty("vertexList")
    private List<Vertex> vertexList;

    @JsonProperty("edgeList")
    private List<Edge> edgeList;
}
