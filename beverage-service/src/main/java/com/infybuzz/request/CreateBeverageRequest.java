package com.infybuzz.request;

import com.infybuzz.entity.BeverageType;
import lombok.Data;

@Data
public class CreateBeverageRequest {
	private String beverageName;
	private Double beverageCost;
	private BeverageType beverageType;
	private Integer availability;
}
