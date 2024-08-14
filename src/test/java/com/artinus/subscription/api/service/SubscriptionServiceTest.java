package com.artinus.subscription.api.service;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.ChannelType;
import com.artinus.subscription.api.entity.Member;
import com.artinus.subscription.api.entity.SubscriptionRequest;
import com.artinus.subscription.api.entity.SubscriptionState;
import com.artinus.subscription.api.repository.MemberRepository;
import com.artinus.subscription.api.repository.SubscriptionRequestRepository;

@DataJpaTest
@ActiveProfiles("test")
public class SubscriptionServiceTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private SubscriptionRequestRepository requestRepository;
    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(memberRepository, requestRepository);
    }

    @ParameterizedTest
    @MethodSource("generateSubscribeUpdateMemberStateAndMakeRequestWhenChannelIsNotApp")
    void subscribe_update_member_state_and_make_request_when_channel_is_not_app(ChannelType channelType) {
        // given
        Member member = memberRepository.save(Member.builder()
                .cellPhoneNumber(CellPhoneNumber.builder().front("010").middle("1234").rear("5678").build())
                .subscriptionState(SubscriptionState.NONE)
                .build());

        Channel channel = Channel.builder().channelType(channelType).build();

        SubscriptionState state = SubscriptionState.NORMAL;
        LocalDateTime now = LocalDateTime.of(2024, 8, 31, 12, 0, 0);

        // when
        service.subscribe(member.getCellPhoneNumber(), channel, state, now);

        Member foundMember = memberRepository.findById(member.getId()).orElseThrow();
        SubscriptionRequest request = requestRepository.findAll().get(0);

        // then
        Assertions.assertThat(foundMember.getSubscriptionState()).isEqualTo(state);
        Assertions.assertThat(request.getMemberId()).isEqualTo(foundMember.getId());
        Assertions.assertThat(request.getChannel()).isEqualTo(channel);
        Assertions.assertThat(request.getDate()).isEqualTo(now.toLocalDate());
        Assertions.assertThat(request.getTime()).isEqualTo(now.toLocalTime());
    }

    private static Stream<Arguments> generateSubscribeUpdateMemberStateAndMakeRequestWhenChannelIsNotApp() {
        return Stream.of(
            Arguments.of(ChannelType.WEB),
            Arguments.of(ChannelType.MOBILE)
        );
    }

    @ParameterizedTest
    @MethodSource("generateCancleUpdateMemberStateToNoneAndMakeRequestWhenChannelIsNotMobile")
    void cancle_update_member_state_to_none_and_make_request_when_channel_is_not_mobile(SubscriptionState state, ChannelType channelType) {
        // given
        Member member = memberRepository.save(Member.builder()
                .cellPhoneNumber(CellPhoneNumber.builder().front("010").middle("1234").rear("5678").build())
                .subscriptionState(state)
                .build());

        Channel channel = Channel.builder().channelType(channelType).build();

        LocalDateTime now = LocalDateTime.of(2024, 8, 31, 12, 0, 0);

        // when
        service.cancle(member.getCellPhoneNumber(), channel, now);

        Member foundMember = memberRepository.findById(member.getId()).orElseThrow();
        SubscriptionRequest request = requestRepository.findAll().get(0);

        // then
        Assertions.assertThat(foundMember.getSubscriptionState()).isEqualTo(SubscriptionState.NONE);
        Assertions.assertThat(request.getMemberId()).isEqualTo(foundMember.getId());
        Assertions.assertThat(request.getChannel()).isEqualTo(channel);
        Assertions.assertThat(request.getDate()).isEqualTo(now.toLocalDate());
        Assertions.assertThat(request.getTime()).isEqualTo(now.toLocalTime());
    }

    private static Stream<Arguments> generateCancleUpdateMemberStateToNoneAndMakeRequestWhenChannelIsNotMobile() {
        return Stream.of(
            Arguments.of(SubscriptionState.NORMAL, ChannelType.WEB),
            Arguments.of(SubscriptionState.NORMAL, ChannelType.APP),
            Arguments.of(SubscriptionState.PREMIUM, ChannelType.WEB),
            Arguments.of(SubscriptionState.PREMIUM, ChannelType.APP)
        );
    }
}
