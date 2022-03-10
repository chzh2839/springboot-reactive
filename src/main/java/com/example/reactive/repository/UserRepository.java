package com.example.reactive.repository;

import com.example.reactive.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    Mono<User> findByName(String name);
}
