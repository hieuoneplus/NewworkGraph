package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
public class Individual {
    public double Fx;
    public double Lb;
    public double ratioAccepted;
    public Map<Request, Integer> option;
    public int rank;
    public double crowdingDistance;
    public Individual() {
        Fx = 0.0;
        Lb = 0.0;
        ratioAccepted = 0.0;
        option = new HashMap<>();
        rank = 0;
        crowdingDistance = 0.0;
    }
}