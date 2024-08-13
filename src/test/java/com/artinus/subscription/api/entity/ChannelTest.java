package com.artinus.subscription.api.entity;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ChannelTest {
    @ParameterizedTest
    @MethodSource("generateSubscribeWhenPossible")
    void subscribe_when_possible(ChannelType channelType) {
        Channel channel = Channel.builder()
                .channelType(channelType)
                .build();

        Assertions.assertThatNoException().isThrownBy(() -> channel.subscribe());
    }

    private static Stream<Arguments> generateSubscribeWhenPossible() {
        return Stream.of(
                Arguments.of(ChannelType.WEB),
                Arguments.of(ChannelType.MOBILE));
    }

    @ParameterizedTest
    @MethodSource("generateSubscribeThrowExceptionAndMessageWhenImpossible")
    void subscribe_throw_Exception_and_messgage_when_impossible(ChannelType channelType) {
        Channel channel = Channel.builder().channelType(channelType).build();

        Assertions.assertThatThrownBy(() -> channel.subscribe())
                .hasMessage("해당 채널은 구독이 불가합니다: " + channelType.toKor())
                .isInstanceOf(RuntimeException.class);
    }

    private static Stream<Arguments> generateSubscribeThrowExceptionAndMessageWhenImpossible() {
        return Stream.of(
            Arguments.of(ChannelType.APP)
        );
    }

    @ParameterizedTest
    @MethodSource("generateCancleWhenPossible")
    void cancle_when_possible(ChannelType channelType) {
        Channel channel = Channel.builder()
                .channelType(channelType)
                .build();

        Assertions.assertThatNoException().isThrownBy(() -> channel.cancle());
    }

    private static Stream<Arguments> generateCancleWhenPossible() {
        return Stream.of(
                Arguments.of(ChannelType.WEB),
                Arguments.of(ChannelType.APP));
    }

    @ParameterizedTest
    @MethodSource("generateCanclehrowExceptionAndMessageWhenImpossible")
    void cancle_throw_Exception_and_messgage_when_impossible(ChannelType channelType) {
        Channel channel = Channel.builder().channelType(channelType).build();

        Assertions.assertThatThrownBy(() -> channel.cancle())
                .hasMessage("해당 채널은 해지가 불가합니다: " + channelType.toKor())
                .isInstanceOf(RuntimeException.class);
    }

    private static Stream<Arguments> generateCanclehrowExceptionAndMessageWhenImpossible() {
        return Stream.of(
            Arguments.of(ChannelType.MOBILE)
        );
    }

    // private static Stream<Arguments> generateSubscribeWhenPossible() {
    // return Stream.of(
    // Arguments.of(SubscriptionState.NONE, SubscriptionState.NORMAL),
    // Arguments.of(SubscriptionState.NONE, SubscriptionState.PREMIUM),
    // Arguments.of(SubscriptionState.NORMAL, SubscriptionState.PREMIUM));
    // }
}
