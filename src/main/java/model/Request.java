package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jdk.dynalink.linker.LinkerServices;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

@NoArgsConstructor
public class Request {
    @JsonProperty("id")
    private String id;
    @JsonProperty("start")
    private String start;
    @JsonProperty("end")
    private String end;
    @JsonProperty("VNF")
    private Queue<String> VNF;
    @JsonProperty("cpu")
    private Double cpu;
    @JsonProperty("memory")
    private Double memory;
    @JsonProperty("bandwidth")
    private Double bandwidth;

    public Request(String id, String start, String end, Queue<String> VNF, double cpu, double memory, double bandwidth) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.VNF = VNF;
        this.cpu = cpu;
        this.memory = memory;
        this.bandwidth = bandwidth;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Queue<String> getVNF() {
        return VNF;
    }

    public void setVNF(Queue<String> VNF) {
        this.VNF = VNF;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public Double getMemory() {
        return memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

    public Double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Double bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
