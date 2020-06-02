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

		AllocateOrderRequest req = (AllocateOrderRequest) message.getPayload();

		//full alloc
		req.getBeerOrderDto().getBeerOrderLines().forEach(line->{
			line.setQuantityAllocated(line.getOrderQuantity());
		});
		
		jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, AllocateOrderResult.builder()
				.beerOrderDto(req.getBeerOrderDto()).pendingInventory(false).allocationError(false).build());

	}
}
