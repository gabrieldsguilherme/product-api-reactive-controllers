package com.wiredbraincoffee.productapiannotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.wiredbraincoffee.productapiannotation.controller.ProductController;
import com.wiredbraincoffee.productapiannotation.model.Product;
import com.wiredbraincoffee.productapiannotation.model.ProductEvent;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ProductController.class)
public class JUnit5WebFluxTestAnnotationTest {

	@Autowired
	private WebTestClient client;
	
	private List<Product> expectedList;
	
	@MockBean
	private ProductRepository productRepository;
	
	@MockBean
	private CommandLineRunner commandLineRunner;
	
	@BeforeEach
	public void beforeEach() {
		this.expectedList = List.of(new Product("1", "Big Latte", 2.99));
	}
	
	@Test
	public void testGetAllProducts() {
		when(productRepository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));
		client.get()
				.uri("/products")
				.exchange()
				.expectStatus()
				.isOk()
				.expectBodyList(Product.class)
				.isEqualTo(expectedList);
	}
	
	@Test
	public void testGetProductByInvalidId() {
		String id = "aaa";
		when(productRepository.findById(id)).thenReturn(Mono.empty());
		client.get()
				.uri("/products/" + id)
				.exchange()
				.expectStatus()
				.isNotFound();
	}
	
	@Test
	public void testGetProductById() {
		Product expectedProduct = expectedList.get(0);
		when(productRepository.findById(expectedProduct.getId())).thenReturn(Mono.just(expectedProduct));
		client.get()
				.uri("/products/" + expectedProduct.getId())
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
				.uri("/products/events")
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
