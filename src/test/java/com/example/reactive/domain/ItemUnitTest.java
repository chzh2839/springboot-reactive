package com.example.reactive.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

/** 도메인 객체 테스트 */

public class ItemUnitTest {
    @Test
    void itemBasicShouldWork() {
        System.out.println("========================== itemBasicShouldWork test start ==========================");
        Item sampleItem = new Item("TV tray", "티비 트레이", 20.10);

        // AssertJ를 사용한 값 일치 테스트
        assertThat(sampleItem.getName(), is(equalTo("TV tray")));
        assertThat(sampleItem.getDescription(), is(equalTo("티비 트레이")));
        assertThat(sampleItem.getPrice(), is(equalTo(20.10)));

        System.out.println("========================== itemBasicShouldWork test end ==========================");
    }
}
