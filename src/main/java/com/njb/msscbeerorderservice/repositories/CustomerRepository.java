package com.njb.msscbeerorderservice.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.njb.msscbeerorderservice.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
	List<Customer> findAllByCustomerNameLike(String customerName);
}
