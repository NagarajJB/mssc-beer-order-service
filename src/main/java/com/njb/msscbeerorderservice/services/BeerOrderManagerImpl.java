package com.njb.msscbeerorderservice.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.njb.model.BeerOrderDto;
import com.njb.msscbeerorderservice.domain.BeerOrder;
import com.njb.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.njb.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.njb.msscbeerorderservice.repositories.BeerOrderRepository;
import com.njb.msscbeerorderservice.sm.BeerOrderStateChangeInterceptor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

	public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

	private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
	private final BeerOrderRepository beerOrderRepository;
	private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

	@Transactional
	@Override
	public BeerOrder newBeerOrder(BeerOrder beerOrder) {
		// house keeping if someone has already set id and status
		beerOrder.setId(null);
		beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
		BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
		sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
		return savedBeerOrder;
	}

	@Transactional
	@Override
	public void processValidationResult(UUID beerOrderId, Boolean isValid) {
		Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

		beerOrderOptional.ifPresent(beerOrder -> {
			if (isValid) {
				sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

				// get saved order again as sendBeerOrderEvent would save the object to db and
				// beerOrder will be a stale object
				BeerOrder validatedOrder = beerOrderRepository.findById(beerOrderId).get();
				sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
			} else
				sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
		});

	}

	@Override
	public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
		Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

		beerOrderOptional.ifPresent(beerOrder -> {
			sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
			updateAllocatedQty(beerOrderDto);
		});
	}

	@Override
	public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
		Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

		beerOrderOptional.ifPresent(beerOrder -> {
			sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);

			updateAllocatedQty(beerOrderDto);
		});

	}

	private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
		Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

		allocatedOrderOptional.ifPresent(allocatedOrder -> {
			allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
				beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
					if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
						beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
					}
				});
			});

			beerOrderRepository.saveAndFlush(allocatedOrder);
		});
	}

	@Override
	public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
		Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

		beerOrderOptional.ifPresent(beerOrder -> {
			sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
		});
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
			stateMachineAccess.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
			stateMachineAccess.resetStateMachine(
					new DefaultStateMachineContext<BeerOrderStatusEnum, BeerOrderEventEnum>(beerOrder.getOrderStatus(),
							null, null, null));
		});

		sm.start();
		return sm;

	}

	@Override
	public void beerOrderPickedUp(UUID id) {
		Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(id);
		beerOrderOptional.ifPresent(beerOrder -> {

			sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICKED_UP);

		});

	}

	@Override
	public void cancelOrder(UUID id) {
		beerOrderRepository.findById(id).ifPresent(beerOrder -> {
			sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CANCEL_ORDER);
		});
	}

}
