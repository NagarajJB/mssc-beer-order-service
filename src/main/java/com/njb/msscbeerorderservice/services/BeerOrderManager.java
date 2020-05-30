package com.njb.msscbeerorderservice.services;

import java.util.UUID;

import com.njb.model.BeerOrderDto;
import com.njb.msscbeerorderservice.domain.BeerOrder;

public interface BeerOrderManager {

	BeerOrder newBeerOrder(BeerOrder beerOrder);

	void processValidationResult(UUID orderId, Boolean isValid);

	void beerOrderAllocationPassed(BeerOrderDto beerOrder);

	void beerOrderAllocationPendingInventory(BeerOrderDto beerOrder);

	void beerOrderAllocationFailed(BeerOrderDto beerOrder);
}
