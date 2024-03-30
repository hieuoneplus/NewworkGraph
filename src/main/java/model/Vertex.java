package model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor
public class Vertex {

    @JsonProperty("label")
    public String label;

    @JsonProperty("function")
    public ArrayList<String> function;

    @JsonProperty("memory")
    public Double memory;

    @JsonProperty("cpu")
    public Double cpu;

    public Vertex(String label, ArrayList<String> function) {
        this.label = label;
        this.function = function;
    }

    public Vertex(String label, ArrayList<String> function, Double resource, Double cpu) {
        this.label = label;
        this.function = function;
        this.memory = resource;
        this.cpu = cpu;
    }

    public ArrayList<String> getFunction() {
        return function;
    }
    public void setFunction(ArrayList<String> function) {
        this.function = function;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public Double getMemory() {
        return memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public Vertex copy() {
        Vertex newVertex = new Vertex();
        newVertex.label = this.label;
        newVertex.function = new ArrayList<>(this.function); // Tạo một bản sao của ArrayList
        newVertex.memory = this.memory;
        newVertex.cpu = this.cpu;
        return newVertex;
    }
}
 