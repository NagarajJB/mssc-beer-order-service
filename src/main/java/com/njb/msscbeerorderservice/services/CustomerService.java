package com.njb.msscbeerorderservice.services;

import org.springframework.data.domain.Pageable;

import com.njb.model.CustomerPagedList;

public interface CustomerService {
	 CustomerPagedList listCustomers(Pageable pageable);
}
