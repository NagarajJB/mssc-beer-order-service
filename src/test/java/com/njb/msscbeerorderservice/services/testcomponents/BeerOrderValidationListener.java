package com.njb.msscbeerorderservice.services.testcomponents;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.njb.model.events.ValidateOrderRequest;
import com.njb.model.events.ValidateOrderResult;
import com.njb.msscbeerorderservice.config.JmsConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

	private final JmsTemplate jmsTemplate;

	@JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
	public void listen(Message message) {

		log.debug("VALIDATE_ORDER_QUEUE mimic listener.............");

		Boolean isValid = true;
		boolean sendResponse = true;

		ValidateOrderRequest request = (ValidateOrderRequest) message.getPayload();

		// condition to fail validation and pending validation
		if (request.getBeerOrder().getCustomerRef() != null) {
			if (request.getBeerOrder().getCustomerRef().equals("fail-validation")) {
				isValid = false;
			} else if (request.getBeerOrder().getCustomerRef().equals("dont-validate")) {
				sendResponse = false;
			}
		}

		if (sendResponse) {
			jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
					ValidateOrderResult.builder().isValid(isValid).orderId(request.getBeerOrder().getId()).build());
		}

	}
}
