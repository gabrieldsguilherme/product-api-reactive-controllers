package com.wiredbraincoffee.productapiannotation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

import com.wiredbraincoffee.productapiannotation.model.Product;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class ProductApiAnnotationApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ProductApiAnnotationApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init(ReactiveMongoOperations operations, ProductRepository productRepository) {
		return args -> {
			Flux<Product> productFlux = Flux.just(new Product(null, "Big Latte", 2.99),
					new Product(null, "Big Decaf", 2.49),
					new Product(null, "Green Tea", 1.99))
			.flatMap(productRepository::save);
			
			productFlux.thenMany(productRepository.findAll())
					.subscribe(System.out::println);
		};
	}

}
