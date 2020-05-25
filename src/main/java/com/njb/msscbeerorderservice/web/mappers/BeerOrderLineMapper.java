package com.njb.msscbeerorderservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import com.njb.model.BeerOrderLineDto;
import com.njb.msscbeerorderservice.domain.BeerOrderLine;

@Mapper(uses = { DateMapper.class })
@DecoratedWith(BeerOrderLineMapperDecorator.class)
public interface BeerOrderLineMapper {
	BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line);

	BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto);
}
