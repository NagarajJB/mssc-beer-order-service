package com.njb.msscbeerorderservice.services.testcomponents;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.njb.model.events.AllocateOrderRequest;
import com.njb.model.events.AllocateOrderResult;
import com.njb.msscbeerorderservice.config.JmsConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

	private final JmsTemplate jmsTemplate;

	@JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
	public void listen(Message message) {

		log.debug("ALLOCATE_ORDER_QUEUE mimic listener.............");

		AllocateOrderRequest request = (AllocateOrderRequest) message.getPayload();
		boolean pendingInventory = false;
		boolean allocationError = false;
		boolean sendResponse = true;

		if (request.getBeerOrderDto().getCustomerRef() != null) {
			if (request.getBeerOrderDto().getCustomerRef().equals("fail-allocation")) {
				allocationError = true;
			} else if (request.getBeerOrderDto().getCustomerRef().equals("partial-allocation")) {
				pendingInventory = true;
			} else if (request.getBeerOrderDto().getCustomerRef().equals("dont-allocate")) {
				sendResponse = false;
			}
		}

		boolean finalPendingInventory = pendingInventory;

		request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
			if (finalPendingInventory) {
				// mimic partial allocation
				beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
			} else {
				// full allocation
				beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
			}
		});

		//to test cancel from pending allocation
		if (sendResponse) {
			jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
					AllocateOrderResult.builder().beerOrderDto(request.getBeerOrderDto())
							.pendingInventory(pendingInventory).allocationError(allocationError).build());
		}

	}
}
