package com.njb.msscbeerorderservice.web.mappers;

import org.mapstruct.Mapper;

import com.njb.model.CustomerDto;
import com.njb.msscbeerorderservice.domain.Customer;

@Mapper(uses = DateMapper.class)
public interface CustomerMapper {

	CustomerDto customerToDto(Customer customer);

	Customer dtoToCustomer(CustomerDto customerDto);
}
