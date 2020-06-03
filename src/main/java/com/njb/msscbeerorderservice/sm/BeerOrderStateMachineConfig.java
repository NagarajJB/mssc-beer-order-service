package com.njb.msscbeerorderservice.sm;

import java.util.EnumSet;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import com.njb.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.njb.msscbeerorderservice.domain.BeerOrderStatusEnum;

import lombok.RequiredArgsConstructor;

/*
 * When an event is triggered, 
	1. It will execute the action configured (might be consuming api, sending message to JMS etc.)
	2. If there is a state change for that event, Code in Interceptor will get executed.
	3. If the service is expecting any messages from other services, listener can be configured and based on the response required events can be triggered.
	4. It will then follow the same sequence.
 */

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class BeerOrderStateMachineConfig
		extends StateMachineConfigurerAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

	private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> validateOrderAction;
	private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> allocateOrderAction;
	private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> validationFailureAction;
    private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> allocationFailureAction;


	@Override
	public void configure(StateMachineStateConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> states)
			throws Exception {

		states.withStates().initial(BeerOrderStatusEnum.NEW).states(EnumSet.allOf(BeerOrderStatusEnum.class))
				.end(BeerOrderStatusEnum.PICKED_UP).end(BeerOrderStatusEnum.DELIVERED)
				.end(BeerOrderStatusEnum.CANCELLED).end(BeerOrderStatusEnum.DELIVERY_EXCEPTION)
				.end(BeerOrderStatusEnum.VALIDATION_EXCEPTION).end(BeerOrderStatusEnum.ALLOCATION_EXCEPTION);

	}

	@Override
	public void configure(StateMachineTransitionConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> transitions)
			throws Exception {
		transitions.withExternal()
			.source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.VALIDATION_PENDING)
			.event(BeerOrderEventEnum.VALIDATE_ORDER).action(validateOrderAction)
		.and().withExternal()
			.source(BeerOrderStatusEnum.VALIDATION_PENDING).target(BeerOrderStatusEnum.VALIDATED)
			.event(BeerOrderEventEnum.VALIDATION_PASSED)
		.and().withExternal()
			.source(BeerOrderStatusEnum.VALIDATION_PENDING).target(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
			.event(BeerOrderEventEnum.VALIDATION_FAILED).action(validationFailureAction)
		.and().withExternal()
			.source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.ALLOCATION_PENDING)
			.event(BeerOrderEventEnum.ALLOCATE_ORDER).action(allocateOrderAction)
		.and().withExternal()
			.source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.ALLOCATED)
			.event(BeerOrderEventEnum.ALLOCATION_SUCCESS)
		.and().withExternal()
			.source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.ALLOCATION_EXCEPTION)
			.event(BeerOrderEventEnum.ALLOCATION_FAILED).action(allocationFailureAction)
		.and().withExternal()
			.source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.PENDING_INVENTORY)
			.event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)
		.and().withExternal()
			.source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.PICKED_UP)
			.event(BeerOrderEventEnum.BEERORDER_PICKED_UP);
	}

}
