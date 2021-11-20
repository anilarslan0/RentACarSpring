package com.etiya.rentACarSpring.businnes.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateCarRequest {

	private int brandId;
	private int colorId;
	private int dailyPrice;

	private String description;

}
