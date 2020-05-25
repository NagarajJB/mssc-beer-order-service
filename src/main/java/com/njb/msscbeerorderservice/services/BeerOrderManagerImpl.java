package com.njb.msscbeerorderservice.services;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import com.njb.msscbeerorderservice.domain.BeerOrder;
import com.njb.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.njb.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.njb.msscbeerorderservice.repositories.BeerOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

	public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

	private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
	private final BeerOrderRepository beerOrderRepository;

	private final BeerOrderStatusChangeInterceptor beerOrderStatusChangeInterceptor;

	@Override
	public BeerOrder newBeerOrder(BeerOrder beerOrder) {

		// house keeping if someone has already set id and status
		beerOrder.setId(null);
		beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

		BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

		sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

		return savedBeerOrder;
	}

	private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {

		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = buildStateMachine(beerOrder);

		Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(eventEnum)
				.setHeader(BeerOrderManagerImpl.ORDER_ID_HEADER, beerOrder.getId()).build();

		sm.sendEvent(message);

	}

	// hydrate sm from db
	private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> buildStateMachine(BeerOrder beerOrder) {

		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory
				.getStateMachine(beerOrder.getId());

		sm.stop();

		sm.getStateMachineAccessor().doWithAllRegions(stateMachineAccess -> {
			stateMachineAccess.addStateMachineInterceptor(beerOrderStatusChangeInterceptor);
			stateMachineAccess.resetStateMachine(
					new DefaultStateMachineContext<BeerOrderStatusEnum, BeerOrderEventEnum>(beerOrder.getOrderStatus(),
							null, null, null));
		});

		sm.start();
		return sm;

	}

}
