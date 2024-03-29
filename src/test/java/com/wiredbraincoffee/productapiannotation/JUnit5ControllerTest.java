package com.wiredbraincoffee.productapiannotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.wiredbraincoffee.productapiannotation.controller.ProductController;
import com.wiredbraincoffee.productapiannotation.model.Product;
import com.wiredbraincoffee.productapiannotation.model.ProductEvent;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;

import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JUnit5ControllerTest {
	
	private WebTestClient client;
	
	private List<Product> expectedList;
	
	@Autowired
	private ProductRepository productRepository;
	
	@BeforeEach
	public void beforeEach() {
		this.client = WebTestClient.bindToController(new ProductController(productRepository))
				.configureClient()
				.baseUrl("/products")
				.build();
		this.expectedList = productRepository.findAll().collectList().block();
	}
	
	@Test
	public void testGetAllProducts() {
		client.get()
				.uri("/")
				.exchange()
				.expectStatus()
				.isOk()
				.expectBodyList(Product.class)
				.isEqualTo(expectedList);
	}
	
	@Test
	public void testGetProductByInvalidId() {
		client.get()
				.uri("/aaa")
				.exchange()
				.expectStatus()
				.isNotFound();
	}
	
	@Test
	public void testGetProductById() {
		Product expectedProduct = expectedList.get(0);
		client.get()
				.uri("/" + expectedProduct.getId())
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody(Product.class)
				.isEqualTo(expectedProduct);
	}
	
	@Test
	public void testProductEvents() {
		ProductEvent expectedEvent = new ProductEvent(0L, "Product Event");
		
		FluxExchangeResult<ProductEvent> result = client.get()
				.uri("/events")
				.accept(MediaType.TEXT_EVENT_STREAM)
				.exchange()
				.expectStatus()
				.isOk()
				.returnResult(ProductEvent.class);
		
		StepVerifier.create(result.getResponseBody())
				.expectNext(expectedEvent)
				.expectNextCount(2)
				.consumeNextWith(event -> assertEquals(Long.valueOf(3), event.getEventId()))
				.thenCancel()
				.verify();
	}

}
