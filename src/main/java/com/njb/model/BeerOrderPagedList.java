package com.njb.model;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class BeerOrderPagedList extends PageImpl<BeerOrderDto> {
	private static final long serialVersionUID = -5621976224038668742L;

	public BeerOrderPagedList(List<BeerOrderDto> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	public BeerOrderPagedList(List<BeerOrderDto> content) {
		super(content);
	}
}