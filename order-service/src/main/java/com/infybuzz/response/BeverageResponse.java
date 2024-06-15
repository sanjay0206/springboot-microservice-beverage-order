package com.infybuzz.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BeverageResponse {
    private String beverageId;
    private String beverageName;
    private Double beverageCost;
    private String beverageType;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer availability;
}
