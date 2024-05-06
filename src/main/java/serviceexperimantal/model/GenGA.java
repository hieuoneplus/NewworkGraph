package serviceexperimantal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GenGA {
    @JsonProperty("total")
    public double total;
    @JsonProperty("accepted")
    public double accepted;
    @JsonProperty("utilization")
    public double utilization;

    @JsonIgnore
    public String SFCs;

    @JsonIgnore
    public String time;
}
