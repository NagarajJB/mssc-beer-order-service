package com.njb.model;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class CustomerPagedList extends PageImpl<CustomerDto> {

	private static final long serialVersionUID = -5151793658579451871L;

	public CustomerPagedList(List<CustomerDto> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

}
