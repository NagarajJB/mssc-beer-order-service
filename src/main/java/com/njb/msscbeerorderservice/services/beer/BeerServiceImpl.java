package com.njb.msscbeerorderservice.services.beer;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.njb.model.BeerDto;

@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Service
public class BeerServiceImpl implements BeerService {

	public final String BEER_PATH_V1 = "/api/v1/beer/";
	public final String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/";
	private final RestTemplate restTemplate;

	private String beerServiceHost;

	public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	@Override
	public Optional<BeerDto> getBeerById(UUID beerId) {
		return Optional
				.of(restTemplate.getForObject(beerServiceHost + BEER_PATH_V1 + beerId.toString(), BeerDto.class));
	}

	@Override
	public Optional<BeerDto> getBeerByUpc(String upc) {
		return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_UPC_PATH_V1 + upc, BeerDto.class));

	}

	public void setBeerServiceHost(String beerServiceHost) {
		this.beerServiceHost = beerServiceHost;
	}

}
