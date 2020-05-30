package com.njb.msscbeerorderservice.services.beer;

import java.util.Optional;
import java.util.UUID;

import com.njb.model.BeerDto;

public interface BeerService {

	Optional<BeerDto> getBeerById(UUID beerId);

	Optional<BeerDto> getBeerByUpc(String upc);

}
