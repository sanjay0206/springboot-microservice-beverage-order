package com.infybuzz.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BeverageRequest {
    private Long beverageId;
    private Integer quantity;
}
