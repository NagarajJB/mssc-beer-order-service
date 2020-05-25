package com.njb.msscbeerorderservice.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.njb.model.BeerOrderDto;
import com.njb.msscbeerorderservice.domain.BeerOrder;

@Mapper(uses = { DateMapper.class, BeerOrderLineMapper.class })
public interface BeerOrderMapper {

	@Mapping(target = "customerId", source = "customer.id")
	BeerOrderDto beerOrderToDto(BeerOrder beerOrder);

	BeerOrder dtoToBeerOrder(BeerOrderDto dto);
}