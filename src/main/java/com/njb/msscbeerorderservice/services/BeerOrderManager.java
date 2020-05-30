package com.njb.msscbeerorderservice.services;

import java.util.UUID;

import com.njb.msscbeerorderservice.domain.BeerOrder;

public interface BeerOrderManager {

	BeerOrder newBeerOrder(BeerOrder beerOrder);

	void processValidationResult(UUID orderId, Boolean isValid);
}
