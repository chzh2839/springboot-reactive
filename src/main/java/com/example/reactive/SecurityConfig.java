package com.example.reactive;

import com.example.reactive.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {
    public static final String USER = "USER";
    public static final String INVENTORY = "INVENTORY";

    public static String role(String auth) {
        return "ROLE_" + auth;
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository){
        return username -> userRepository.findByName(username)
                .map(user -> User.withDefaultPasswordEncoder()
                            .username(user.getName())
                            .password(user.getPassword())
                            .authorities(user.getRoles().get(0))
                        .build()); // userDetails 객체 생성
    }

    @Bean
    SecurityWebFilterChain myCustomSecurityPolicy(ServerHttpSecurity http) { // <1>
        return http //
                .authorizeExchange(exchanges -> exchanges //
//                        .pathMatchers(HttpMethod.POST, "/item").hasRole(INVENTORY) // itemController 아이템추가
//                        .pathMatchers(HttpMethod.POST, "/saveItem").hasRole(INVENTORY) // homeController 아이템추가
//                        .pathMatchers(HttpMethod.DELETE, "/**").hasRole(INVENTORY)
                        .anyExchange().authenticated() // <3>
                        .and() //
                        .httpBasic() // <4> HTTP BASIC 인증 허용
                        .and() //
                        .formLogin()) // <5> 로그인 정보를 HTTP FORM으로 전송하는 것을 허용
                .csrf().disable() //
                .build();
    }

    @Bean
    CommandLineRunner userLoader(MongoOperations operations) {
        return args -> {
//            operations.save(new com.example.reactive.domain.User(
//                    "user1", "password1", Arrays.asList(role("USER"))));
//            operations.save(new com.example.reactive.domain.User(
//                    "user2", "password2", Arrays.asList(role("USER"), role("INVENTORY"))));
//            operations.save(new com.example.reactive.domain.User(
//                    "user3", "password3", Arrays.asList(role("INVENTORY"))));
        };
    }
}
