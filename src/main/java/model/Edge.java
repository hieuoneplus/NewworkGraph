package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Edge {
    @JsonProperty("bandwidth")
    public double bandwidth;

    @JsonProperty("v1")
    public String v1;
    @JsonProperty("v2")
    public String v2;
    public Edge(String v1, String v2, double bandwidth) {
        this.bandwidth = bandwidth;
        this.v1 = v1;
        this.v2 = v2;
    }
    public double getBandwidth() {
        return bandwidth;
    }
    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }
    public String getV1() {
        return v1;
    }
    public void setV1(String v1) {
        this.v1 = v1;
    }
    public String getV2() {
        return v2;
    }
    public void setV2(String v2) {
        this.v2 = v2;
    }

    public Edge copy() {
        String newV1 = this.v1;
        String newV2 = this.v2;
        return new Edge(newV1, newV2, this.bandwidth);
    }
}
 