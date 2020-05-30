package com.njb.msscbeerorderservice.services;

import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import com.njb.model.BeerOrderDto;
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
				.setHeader(BeerOrderManagerImpl.ORDER_ID_HEADER, beerOrder.getId().toString()).build();

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

	@Override
	public void processValidationResult(UUID beerOrderId, Boolean isValid) {
		BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);
		if (isValid) {
			sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

			// get saved order again as sendBeerOrderEvent would save the object to db and
			// beerOrder will be a stale object
			BeerOrder validatedOrder = beerOrderRepository.findOneById(beerOrderId);
			sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
		} else
			sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
	}

	@Override
	public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
		BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
		sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
		updateAllocatedQty(beerOrderDto, beerOrder);
	}

	private void updateAllocatedQty(BeerOrderDto beerOrderDto, BeerOrder beerOrder) {
		BeerOrder allocatedOrder = beerOrderRepository.getOne(beerOrderDto.getId());

		allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
			beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
				if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
					beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
				}
			});
		});

		beerOrderRepository.saveAndFlush(beerOrder);
	}

	@Override
	public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
		BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
		sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);

		updateAllocatedQty(beerOrderDto, beerOrder);
	}

	@Override
	public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
		BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
		sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
	}

}
