package serviceexperimantal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GenNSGA {
    @JsonProperty("Lb")
    public double Lb;
    @JsonProperty("ratioAccepted")
    public double ratioAccepted;
    @JsonProperty("Fx")
    public double Fx;
    @JsonProperty("rank")
    public double rank;
    @JsonProperty("crowdingDistance")
    public double crowdingDistance;
    @JsonIgnore
    public String view;
}
