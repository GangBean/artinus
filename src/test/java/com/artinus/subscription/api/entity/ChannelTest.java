package com.artinus.subscription.api.entity;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.artinus.subscription.api.exception.ChannelCanNotCancleException;
import com.artinus.subscription.api.exception.ChannelCanNotSubscribeException;

public class ChannelTest {
    @ParameterizedTest
    @MethodSource("generateSubscribeWhenPossible")
    void subscribe_when_have_subscribe_auth(String name, ChannelAuthSet auths) {
        Channel channel = Channel.builder()
                .name(name)
                .auths(auths)
                .build();

        Assertions.assertThatNoException().isThrownBy(() -> channel.validateSubscription());
    }

    private static Stream<Arguments> generateSubscribeWhenPossible() {
        return Stream.of(
                Arguments.of("웹", ChannelAuthSet.of(ChannelAuth.SUBSCRIBE, ChannelAuth.CANCLE)),
                Arguments.of("모바일", ChannelAuthSet.of(ChannelAuth.SUBSCRIBE)));
    }

    @ParameterizedTest
    @MethodSource("generateSubscribeThrowExceptionAndMessageWhenImpossible")
    void subscribe_throw_Exception_and_messgage_when_dont_have_subscribe_auth(String name, ChannelAuthSet auths) {
        Channel channel = Channel.builder()
                .name(name)
                .auths(auths)
                .build();

        Assertions.assertThatThrownBy(() -> channel.validateSubscription())
                .hasMessage("해당 채널은 구독이 불가합니다: " + channel.getName())
                .isInstanceOf(ChannelCanNotSubscribeException.class);
    }

    private static Stream<Arguments> generateSubscribeThrowExceptionAndMessageWhenImpossible() {
        return Stream.of(
                Arguments.of("웹", ChannelAuthSet.of()),
                Arguments.of("모바일", ChannelAuthSet.of(ChannelAuth.CANCLE)));
    }

    @ParameterizedTest
    @MethodSource("generateCancleWhenPossible")
    void cancle_when_have_cancle_auth(String name, ChannelAuthSet auths) {
        Channel channel = Channel.builder()
                .name(name)
                .auths(auths)
                .build();

        Assertions.assertThatNoException().isThrownBy(() -> channel.validateCancle());
    }

    private static Stream<Arguments> generateCancleWhenPossible() {
        return Stream.of(
                Arguments.of("웹", ChannelAuthSet.of(ChannelAuth.CANCLE)),
                Arguments.of("모바일", ChannelAuthSet.of(ChannelAuth.CANCLE, ChannelAuth.SUBSCRIBE)));
    }

    @ParameterizedTest
    @MethodSource("generateCancleThrowExceptionAndMessageWhenImpossible")
    void cancle_throw_Exception_and_messgage_when_dont_have_cancle_auth(String name, ChannelAuthSet auths) {
        Channel channel = Channel.builder()
                .name(name)
                .auths(auths)
                .build();

        Assertions.assertThatThrownBy(() -> channel.validateCancle())
                .hasMessage("해당 채널은 해지가 불가합니다: " + channel.getName())
                .isInstanceOf(ChannelCanNotCancleException.class);
    }

    private static Stream<Arguments> generateCancleThrowExceptionAndMessageWhenImpossible() {
        return Stream.of(
                Arguments.of("웹", ChannelAuthSet.of()),
                Arguments.of("웹", ChannelAuthSet.of(ChannelAuth.SUBSCRIBE)));
    }

    // private static Stream<Arguments> generateSubscribeWhenPossible() {
    // return Stream.of(
    // Arguments.of(SubscriptionState.NONE, SubscriptionState.NORMAL),
    // Arguments.of(SubscriptionState.NONE, SubscriptionState.PREMIUM),
    // Arguments.of(SubscriptionState.NORMAL, SubscriptionState.PREMIUM));
    // }
}
