package com.njb.msscbeerorderservice.web.mappers;

import org.mapstruct.Mapper;

import com.njb.msscbeerorderservice.domain.BeerOrderLine;
import com.njb.msscbeerorderservice.web.model.BeerOrderLineDto;

@Mapper(uses = { DateMapper.class })
public interface BeerOrderLineMapper {
	BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line);

	BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto);
}
