package com.artinus.subscription.api.entity;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.artinus.subscription.api.exception.SubscriptionStateCanNotChangeException;

public class SubscriptionStateTest {
    @ParameterizedTest
    @MethodSource("generateChangeWhenPossible")
    void change_when_possible(SubscriptionState from, SubscriptionState to) {
        Assertions.assertThatNoException().isThrownBy(() -> from.ensureUpdatable(to));
    }

    private static Stream<Arguments> generateChangeWhenPossible() {
        return Stream.of(
                Arguments.of(SubscriptionState.NONE, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.NONE, SubscriptionState.PREMIUM),
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.NONE),
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.PREMIUM),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.NONE),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.NORMAL));
    }

    @ParameterizedTest
    @MethodSource("generateChangeThrowsExceptionWithMessageWhenSameState")
    void change_throws_exception_with_message_when_same_state(SubscriptionState from, SubscriptionState to) {
        Assertions.assertThatThrownBy(() -> from.ensureUpdatable(to))
                .hasMessage("구독 상태 변경이 불가합니다: " + from.toKor() + "->" + to.toKor())
                .isInstanceOf(SubscriptionStateCanNotChangeException.class);
    }

    private static Stream<Arguments> generateChangeThrowsExceptionWithMessageWhenSameState() {
        return Stream.of(
                Arguments.of(SubscriptionState.NONE, SubscriptionState.NONE),
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.PREMIUM));
    }
}
