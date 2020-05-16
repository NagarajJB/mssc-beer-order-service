package com.njb.msscbeerorderservice.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.njb.msscbeerorderservice.domain.BeerOrder;
import com.njb.msscbeerorderservice.web.model.BeerOrderDto;

@Mapper(uses = { DateMapper.class, BeerOrderLineMapper.class })
public interface BeerOrderMapper {

	@Mapping(target = "customerId", source = "customer.id")
	BeerOrderDto beerOrderToDto(BeerOrder beerOrder);

	BeerOrder dtoToBeerOrder(BeerOrderDto dto);
}