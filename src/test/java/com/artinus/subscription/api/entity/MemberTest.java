package com.artinus.subscription.api.entity;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.artinus.subscription.api.exception.SubscriptionStateCanNotChangeException;

public class MemberTest {
    @ParameterizedTest
    @MethodSource("generateSubscribeStateWhenPossible")
    void subscribe_state_when_current_state_is_lower(SubscriptionState from, SubscriptionState to) {
        Member member = Member.builder().subscriptionState(from).build();
        member.subscribe(to);

        Assertions.assertThat(member.getSubscriptionState()).isEqualTo(to);
    }

    private static Stream<Arguments> generateSubscribeStateWhenPossible() {
        return Stream.of(
                Arguments.of(SubscriptionState.NONE, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.NONE, SubscriptionState.PREMIUM),
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.PREMIUM));
    }

    @ParameterizedTest
    @MethodSource("generateSubscribeThrowsExceptionWithMessageWhenInitialStateIsHigher")
    void subscribe_throws_exception_with_message_when_initial_state_is_higher(SubscriptionState from,
            SubscriptionState to) {
        Member member = Member.builder().subscriptionState(from).build();

        Assertions.assertThatThrownBy(() -> member.subscribe(to))
                .hasMessage("낮거나 같은 등급으로 변경이 불가합니다: " + from.toKor() + " -> " + to.toKor())
                .isInstanceOf(SubscriptionStateCanNotChangeException.class);
    }

    private static Stream<Arguments> generateSubscribeThrowsExceptionWithMessageWhenInitialStateIsHigher() {
        return Stream.of(
                Arguments.of(SubscriptionState.NONE, SubscriptionState.NONE),
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.NONE),
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.PREMIUM));
    }

    @ParameterizedTest
    @MethodSource("generateCancleStateWhenCurrentStateIsHigher")
    void cancle_state_when_current_state_is_higher(SubscriptionState from, SubscriptionState to) {
        Member member = Member.builder().subscriptionState(from).build();
        member.cancle(to);

        Assertions.assertThat(member.getSubscriptionState()).isEqualTo(to);
    }

    private static Stream<Arguments> generateCancleStateWhenCurrentStateIsHigher() {
        return Stream.of(
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.NONE),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.NONE),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.NORMAL));
    }

    @ParameterizedTest
    @MethodSource("generateCancleThrowsExceptionWithMessageWhenCurrentStateIsLower")
    void cancle_throws_exception_with_message_when_current_state_is_lower(
            SubscriptionState from,
            SubscriptionState to) {
        Member member = Member.builder().subscriptionState(from).build();

        Assertions.assertThatThrownBy(() -> member.cancle(to))
                .hasMessage("높거나 같은 등급으로 변경이 불가합니다: " + from.toKor() + " -> " + to.toKor())
                .isInstanceOf(SubscriptionStateCanNotChangeException.class);
    }

    private static Stream<Arguments> generateCancleThrowsExceptionWithMessageWhenCurrentStateIsLower() {
        return Stream.of(
                Arguments.of(SubscriptionState.NONE, SubscriptionState.NONE),
                Arguments.of(SubscriptionState.NONE, SubscriptionState.NORMAL),
                Arguments.of(SubscriptionState.NONE, SubscriptionState.PREMIUM),
                Arguments.of(SubscriptionState.NORMAL, SubscriptionState.PREMIUM),
                Arguments.of(SubscriptionState.PREMIUM, SubscriptionState.PREMIUM));
    }
}
