package com.infybuzz.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeverageResponse {
    private Long beverageId;
    private String beverageName;
    private Double beverageCost;
    private String beverageType;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer availability;
}
