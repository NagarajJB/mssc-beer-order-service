package com.njb.msscbeerorderservice.sm.actions;

import java.util.UUID;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import com.njb.model.events.AllocateOrderRequest;
import com.njb.msscbeerorderservice.config.JmsConfig;
import com.njb.msscbeerorderservice.domain.BeerOrder;
import com.njb.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.njb.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.njb.msscbeerorderservice.repositories.BeerOrderRepository;
import com.njb.msscbeerorderservice.services.BeerOrderManagerImpl;
import com.njb.msscbeerorderservice.web.mappers.BeerOrderMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
	private final JmsTemplate jmsTemplate;
	private final BeerOrderRepository beerOrderRepository;
	private final BeerOrderMapper beerOrderMapper;

	@Override
	public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
		String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
		BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(beerOrderId));

		jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_QUEUE,
				AllocateOrderRequest.builder().beerOrder(beerOrderMapper.beerOrderToDto(beerOrder)).build());

		log.debug("Sent Allocation Request for order id: " + beerOrderId);

	}
}
