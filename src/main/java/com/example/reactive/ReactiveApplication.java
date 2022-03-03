package com.example.reactive;

import io.netty.channel.Channel;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.TemplateEngine;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class ReactiveApplication {

	public static void main(String[] args) {
		//BlockHound.install(); // 블록하운드 등록

		// TemplateEngine.process(), Unsafe.park 부분은 허용
		// 저수준의 메서드를 허용하는 것보다 구체적인 일부 지점만 허용하는 것이 안전
		BlockHound.builder()
				.allowBlockingCallsInside(
						TemplateEngine.class.getCanonicalName(), "process"
				).allowBlockingCallsInside(Channel.Unsafe.class.getCanonicalName(), "park").install();

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

	// JSON 기반 메시지 직렬화 설정
	// @Configurartion이 붙어있는 아무 클래스에서나 추가 가능 (@SpringBootApplication도 @Configurartion를 포함하고 있음)
	@Bean
	Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

}
