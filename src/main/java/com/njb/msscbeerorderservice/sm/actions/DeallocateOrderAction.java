package com.njb.msscbeerorderservice.sm.actions;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import com.njb.model.events.DeallocateOrderRequest;
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
public class DeallocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
	private final JmsTemplate jmsTemplate;
	private final BeerOrderRepository beerOrderRepository;
	private final BeerOrderMapper beerOrderMapper;

	@Override
	public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
		String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
		Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));

		beerOrderOptional.ifPresent(beerOrder -> {
			jmsTemplate.convertAndSend(JmsConfig.DEALLOCATE_ORDER_QUEUE,
					DeallocateOrderRequest.builder().beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder)).build());

			//inventory service will listen to it and add back allocated to inventory
			log.debug("Sent Deallocation Request for order id: " + beerOrderId);
		});
	}
}
