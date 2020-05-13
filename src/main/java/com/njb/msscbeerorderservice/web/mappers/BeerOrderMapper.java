package com.njb.msscbeerorderservice.web.mappers;

import org.mapstruct.Mapper;

import com.njb.msscbeerorderservice.domain.BeerOrder;
import com.njb.msscbeerorderservice.web.model.BeerOrderDto;

@Mapper(uses = { DateMapper.class, BeerOrderLineMapper.class })
public interface BeerOrderMapper {

	BeerOrderDto beerOrderToDto(BeerOrder beerOrder);

	BeerOrder dtoToBeerOrder(BeerOrderDto dto);
}
