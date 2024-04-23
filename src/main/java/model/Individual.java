package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class Individual {
    public double Fx;
    public double Lb;
    public double ratioAccepted;
    public Map<Request, Integer> option;
    public Map<Request, String> view;
    public Map<Request, Boolean> accepted;
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

    @JsonIgnore
    public double getFx() {
        return Fx;
    }

    public void setFx(double fx) {
        Fx = fx;
    }
    @JsonIgnore
    public double getLb() {
        return Lb;
    }

    public void setLb(double lb) {
        Lb = lb;
    }

    public double getRatioAccepted() {
        return ratioAccepted;
    }

    public void setRatioAccepted(double ratioAccepted) {
        this.ratioAccepted = ratioAccepted;
    }

    public Map<Request, String> getView() {
        return view;
    }

    public void setView(Map<Request, String> view) {
        this.view = view;
    }

    @JsonIgnore
    public Map<Request, Integer> getOption() {
        return option;
    }

    public void setOption(Map<Request, Integer> option) {
        this.option = option;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }


    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    @JsonIgnore
    public Map<Request, Boolean> getAccepted() {
        return accepted;
    }

    public void setAccepted(Map<Request, Boolean> accepted) {
        this.accepted = accepted;
    }
}