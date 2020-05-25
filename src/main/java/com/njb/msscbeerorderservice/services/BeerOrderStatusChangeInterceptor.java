package com.njb.msscbeerorderservice.services;

import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import com.njb.msscbeerorderservice.domain.BeerOrder;
import com.njb.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.njb.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.njb.msscbeerorderservice.repositories.BeerOrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderStatusChangeInterceptor
		extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

	private final BeerOrderRepository beerOrderRepository;

	@Transactional
	@Override
	public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
			Message<BeerOrderEventEnum> message, Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
			StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {

		Optional.ofNullable(message)
				.flatMap(msg -> Optional
						.ofNullable((String) msg.getHeaders().getOrDefault(BeerOrderManagerImpl.ORDER_ID_HEADER, " ")))
				.ifPresent(orderId -> {
					log.debug("Saving state for order id: " + orderId + " Status: " + state.getId());
					BeerOrder beerOrder = beerOrderRepository.getOne(UUID.fromString(orderId));
					beerOrder.setOrderStatus(state.getId());
					beerOrderRepository.saveAndFlush(beerOrder);
				});

	}

}
