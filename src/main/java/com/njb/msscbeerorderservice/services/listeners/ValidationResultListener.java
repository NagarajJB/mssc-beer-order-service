package com.njb.msscbeerorderservice.services.listeners;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.njb.model.events.ValidateOrderResult;
import com.njb.msscbeerorderservice.config.JmsConfig;
import com.njb.msscbeerorderservice.services.BeerOrderManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationResultListener {

	private final BeerOrderManager beerOrderManager;
	
	@JmsListener(destination = JmsConfig.VALIDATE_ORDER__RESPONSE_QUEUE)
	public void listen(ValidateOrderResult validateOrderResult) {
		
		log.debug("Validation res for "+ validateOrderResult.getOrderId().toString());
		
		beerOrderManager.processValidationResult(validateOrderResult.getOrderId(), validateOrderResult.getIsValid());
	}
	
}
