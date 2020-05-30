package com.njb.msscbeerorderservice.services.listeners;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.njb.model.events.AllocateOrderResult;
import com.njb.msscbeerorderservice.config.JmsConfig;
import com.njb.msscbeerorderservice.services.BeerOrderManager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class BeerOrderAllocationResultListener {

	private final BeerOrderManager beerOrderManager;

	@JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
	public void listen(AllocateOrderResult result) {
		if (!result.getAllocationError() && !result.getPendingInventory()) {
			// allocated normally
			beerOrderManager.beerOrderAllocationPassed(result.getBeerOrderDto());
		} else if (!result.getAllocationError() && result.getPendingInventory()) {
			// pending inventory
			beerOrderManager.beerOrderAllocationPendingInventory(result.getBeerOrderDto());
		} else if (result.getAllocationError()) {
			// allocation error
			beerOrderManager.beerOrderAllocationFailed(result.getBeerOrderDto());
		}
	}
}
