package com.example.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveApplication.class, args);
	}

	// 블로킹 리포지토리 사용
//	@Bean
//	CommandLineRunner initialize(BlockingItemRepository repository) {
//		return args -> {
//			repository.save(new Item("LG air fryer", 29.99));
//			repository.save(new Item("Samsung Galaxy tab", 850.11));
//		};
//	}

//	@Bean
//	CommandLineRunner initialize(MongoOperations mongo) {
//		return args -> {
//			mongo.findAllAndRemove();
//			mongo.save(new Item("LG air fryer", "에어프라이어", 29.99));
//			mongo.save(new Item("Samsung Galaxy tab", "갤럭시 탭", 850.11));
//		};
//	}

}
