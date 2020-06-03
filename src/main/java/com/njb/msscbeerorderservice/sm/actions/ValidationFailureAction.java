package com.njb.msscbeerorderservice.sm.actions;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import com.njb.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.njb.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.njb.msscbeerorderservice.services.BeerOrderManagerImpl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Data
@RequiredArgsConstructor
@Slf4j
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

	// this may be complex in real world app, like notify accounting, notify cust
	// service, create ticket etc might be using web hook call back

	@Override
	public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

		String orderId = (String) context.getMessageHeader(BeerOrderManagerImpl.ORDER_ID_HEADER);
		log.debug("Compensating Transaction...Validation failed:" + orderId);

	}

}
