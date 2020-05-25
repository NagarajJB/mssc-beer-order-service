package com.njb.msscbeerorderservice.services;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.njb.model.BeerOrderDto;
import com.njb.model.BeerOrderPagedList;

public interface BeerOrderService {
	BeerOrderPagedList listOrders(UUID customerId, Pageable pageable);

	BeerOrderDto placeOrder(UUID customerId, BeerOrderDto beerOrderDto);

	BeerOrderDto getOrderById(UUID customerId, UUID orderId);

	void pickupOrder(UUID customerId, UUID orderId);
}
