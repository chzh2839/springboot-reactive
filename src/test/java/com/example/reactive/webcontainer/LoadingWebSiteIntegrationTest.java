package com.example.reactive.webcontainer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

// WebEnvironment.RANDOM_PORT 는 테스트할 때 임의의 포트에 내장 컨테이너를 바인딩한다.
// @AutoConfigureWebTestClient는 app에 요청을 날리는 WebTestClient 인스턴스를 생성한다.
@Disabled("pom.xml에서 blockhound-junit-platform 의존 관계를 제거한 후에 실행해야 성공한다.")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class LoadingWebSiteIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void test() {
        webTestClient.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    assertThat(stringEntityExchangeResult.getResponseBody()).contains("<input type=\"submit\" value=\"Add");
                });
    }
}
