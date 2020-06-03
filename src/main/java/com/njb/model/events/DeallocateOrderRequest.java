package com.njb.model.events;

import com.njb.model.BeerOrderDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeallocateOrderRequest {
	 private BeerOrderDto beerOrderDto;
}
