package com.njb.msscbeerorderservice.sm.actions;

import java.util.UUID;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import com.njb.model.events.AllocationFailureEvent;
import com.njb.msscbeerorderservice.config.JmsConfig;
import com.njb.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.njb.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.njb.msscbeerorderservice.services.BeerOrderManagerImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
	private final JmsTemplate jmsTemplate;

	@Override
	public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
		String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);

		jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_FAILURE_QUEUE,
				AllocationFailureEvent.builder().orderId(UUID.fromString(beerOrderId)).build());

		log.debug("Sent Allocation Failure Message to queue for order id " + beerOrderId);
	}

}
