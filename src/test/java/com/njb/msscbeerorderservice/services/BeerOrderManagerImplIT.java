package com.njb.msscbeerorderservice.services;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.njb.model.BeerDto;
import com.njb.msscbeerorderservice.domain.BeerOrder;
import com.njb.msscbeerorderservice.domain.BeerOrderLine;
import com.njb.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.njb.msscbeerorderservice.domain.Customer;
import com.njb.msscbeerorderservice.repositories.BeerOrderRepository;
import com.njb.msscbeerorderservice.repositories.CustomerRepository;
import com.njb.msscbeerorderservice.services.beer.BeerServiceImpl;

@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class BeerOrderManagerImplIT {

	@Autowired
	BeerOrderManager beerOrderManager;

	@Autowired
	BeerOrderRepository beerOrderRepository;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	WireMockServer wireMockServer;

	// as we have hibernate in the classpath, spring is going to create an in memory
	// h2 and work with it
	Customer testCustomer;

	UUID beerId = UUID.randomUUID();

	@Autowired
	ObjectMapper objectMapper;

	@TestConfiguration
	static class RestTemplateBuilderProvider {
		@Bean(destroyMethod = "stop")
		public WireMockServer wireMockServer() {
			WireMockServer server = with(wireMockConfig().port(7073));
			server.start();
			return server;
		}

	}

	@BeforeEach
	void setUp() {
		testCustomer = customerRepository.save(Customer.builder().customerName("Test Customer").build());
	}

	public BeerOrder createBeerOrder() {
		BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();

		Set<BeerOrderLine> beerOrderLines = new HashSet<BeerOrderLine>();

		beerOrderLines
				.add(BeerOrderLine.builder().beerId(beerId).upc("12345").orderQuantity(2).beerOrder(beerOrder).build());

		beerOrder.setBeerOrderLines(beerOrderLines);

		return beerOrder;
	}

	// for testing using artemis-jms-server embedded jms broker and mimic listener,
	// it will require a few seconds to get response, we used sleep, can use
	// awaitility which makes it more elegant
	// In testing we try to make the application self contained (jms broker, test
	// listener, mock api using wiremockserver etc)

	@Test
	void testNewToAllocated() throws JsonProcessingException, InterruptedException {

		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		// beerOrder gets updated with id on save
		beerOrderManager.newBeerOrder(beerOrder);

		// Awaitility lets program wait until the asynchronous operation completes:
		Awaitility.await().untilAsserted(() -> {
			BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
		});

		BeerOrder savedBeerOrder = beerOrderRepository.findById(beerOrder.getId()).get();

		assertNotNull(savedBeerOrder);
		savedBeerOrder.getBeerOrderLines().forEach(line -> {
			assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
		});

	}

	@Test
	void testFailedValidation() throws JsonProcessingException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		beerOrder.setCustomerRef("fail-validation");
		// beerOrder gets updated with id on save
		beerOrderManager.newBeerOrder(beerOrder);

		Awaitility.await().untilAsserted(() -> {
			BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
		});
	}

	@Test
	void testNewToPickedUp() throws JsonProcessingException, InterruptedException {

		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		// beerOrder gets updated with id on save
		beerOrderManager.newBeerOrder(beerOrder);

		// Awaitility lets program wait until the asynchronous operation completes:
		Awaitility.await().untilAsserted(() -> {
			BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
		});

		beerOrderManager.beerOrderPickedUp(beerOrder.getId());

		Awaitility.await().untilAsserted(() -> {
			BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus());
		});

		BeerOrder savedBeerOrder = beerOrderRepository.findById(beerOrder.getId()).get();
		assertEquals(BeerOrderStatusEnum.PICKED_UP, savedBeerOrder.getOrderStatus());

	}

	@Test
	void testAllocationFailure() throws JsonProcessingException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		beerOrder.setCustomerRef("fail-allocation");
		
		beerOrderManager.newBeerOrder(beerOrder);

		Awaitility.await().untilAsserted(() -> {
			BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
		});
	}

	@Test
	void testPartialAllocation() throws JsonProcessingException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		beerOrder.setCustomerRef("partial-allocation");

		beerOrderManager.newBeerOrder(beerOrder);
		
		Awaitility.await().untilAsserted(() -> {
			BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.PENDING_INVENTORY, foundOrder.getOrderStatus());
		});
	}

}
