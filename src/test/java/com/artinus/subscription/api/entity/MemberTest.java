package com.artinus.subscription.api.entity;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.artinus.subscription.api.exception.SubscriptionStateCanNotChangeException;

public class MemberTest {
    @ParameterizedTest
    @MethodSource("generateSubscribeStateWhenPossible")
    void subscribe_state_when_possible(SubscriptionState from, SubscriptionState to) {
        Member member = Member.builder().subscriptionState(from).build();
        member.subscribe(to);

        Assertions.assertThat(member.getSubscriptionState()).isEqualTo(to);
    }

    private static Stream<Arguments> generateSubscribeStateWhenPossible() {
        return Stream.of(
                Arguments.of(SubscriptionState.NONE, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.NONE, SubscriptionState.PREMIUM));
    }

    @ParameterizedTest
    @MethodSource("generateSubscribeThrowsExceptionWithMessageWhenInitialStateIsNotNone")
    void subscribe_throws_exception_with_message_when_initial_state_is_not_none(SubscriptionState from,
            SubscriptionState to) {
        Member member = Member.builder().subscriptionState(from).build();

        Assertions.assertThatThrownBy(() -> member.subscribe(to))
                .hasMessage("이미 구독중인 서비스가 존재합니다: " + from.toKor())
                .isInstanceOf(SubscriptionStateCanNotChangeException.class);
    }

    private static Stream<Arguments> generateSubscribeThrowsExceptionWithMessageWhenInitialStateIsNotNone() {
        return Stream.of(
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.PREMIUM),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.PREMIUM));
    }

    @ParameterizedTest
    @MethodSource("generateUpgradeStateWhenPossible")
    void upgrade_state_when_possible(SubscriptionState from, SubscriptionState to) {
        Member member = Member.builder().subscriptionState(from).build();
        member.upgrade(to);

        Assertions.assertThat(member.getSubscriptionState()).isEqualTo(to);
    }

    private static Stream<Arguments> generateUpgradeStateWhenPossible() {
        return Stream.of(
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.PREMIUM));
    }

    @ParameterizedTest
    @MethodSource("generateUpgradeThrowsExceptionWithMessageWhenInitialStateIsNotNone")
    void upgrade_throws_exception_with_message_when_initial_state_is_not_none(SubscriptionState from,
            SubscriptionState to) {
        Member member = Member.builder().subscriptionState(from).build();

        Assertions.assertThatThrownBy(() -> member.upgrade(to))
                .hasMessage("구독 상태 갱신이 불가합니다.")
                .isInstanceOf(SubscriptionStateCanNotChangeException.class);
    }

    private static Stream<Arguments> generateUpgradeThrowsExceptionWithMessageWhenInitialStateIsNotNone() {
        return Stream.of(
                Arguments.of(SubscriptionState.NONE, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.NONE, SubscriptionState.PREMIUM),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.PREMIUM));
    }

    @ParameterizedTest
    @MethodSource("generateCancleStateWhenPossible")
    void cancle_state_when_possible(SubscriptionState from) {
        Member member = Member.builder().subscriptionState(from).build();
        member.cancle();

        Assertions.assertThat(member.getSubscriptionState()).isEqualTo(SubscriptionState.NONE);
    }

    private static Stream<Arguments> generateCancleStateWhenPossible() {
        return Stream.of(
                Arguments.of(SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.PREMIUM));
    }

    @Test
    void cancle_throws_exception_with_message_when_state_is_none() {
        Member member = Member.builder().subscriptionState(SubscriptionState.NONE).build();

        Assertions.assertThatThrownBy(() -> member.cancle())
                .hasMessage("현재 구독중인 상태가 아닙니다.")
                .isInstanceOf(SubscriptionStateCanNotChangeException.class);
    }
}
