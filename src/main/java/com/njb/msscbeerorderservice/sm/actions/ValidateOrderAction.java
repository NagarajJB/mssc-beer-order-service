package com.njb.msscbeerorderservice.sm.actions;

import java.util.UUID;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import com.njb.model.events.ValidateOrderRequest;
import com.njb.msscbeerorderservice.config.JmsConfig;
import com.njb.msscbeerorderservice.domain.BeerOrder;
import com.njb.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.njb.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.njb.msscbeerorderservice.repositories.BeerOrderRepository;
import com.njb.msscbeerorderservice.services.BeerOrderManagerImpl;
import com.njb.msscbeerorderservice.web.mappers.BeerOrderMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Data
@RequiredArgsConstructor
@Slf4j
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

	private final JmsTemplate jmsTemplate;
	private final BeerOrderRepository beerOrderRepository;
	private final BeerOrderMapper beerOrderMapper;

	@Override
	public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

		String orderId = (String) context.getMessageHeader(BeerOrderManagerImpl.ORDER_ID_HEADER);
		BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(orderId));

		jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE,
				ValidateOrderRequest.builder().beerOrder(beerOrderMapper.beerOrderToDto(beerOrder)).build());

		log.debug("Sent validation request for beer order " + orderId);

	}

}
