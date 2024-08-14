package com.artinus.subscription.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import com.artinus.subscription.api.response.RequestResponse;

@DataJpaTest
@ActiveProfiles("test")
public class SubscriptionServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private SubscriptionRequestRepository requestRepository;
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
                Arguments.of(ChannelType.MOBILE));
    }

    @ParameterizedTest
    @MethodSource("generateCancleUpdateMemberStateToNoneAndMakeRequestWhenChannelIsNotMobile")
    void cancle_update_member_state_to_none_and_make_request_when_channel_is_not_mobile(SubscriptionState state,
            ChannelType channelType) {
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
                Arguments.of(SubscriptionState.PREMIUM, ChannelType.APP));
    }

    @Test
    void get_requests_by_phone_number_return_list_of_phone_number_owners_entire_subscription_histories() {
        // given
        Member member = Member.builder()
                .cellPhoneNumber(CellPhoneNumber.builder()
                        .front("010").middle("1234").rear("4567")
                        .build())
                .subscriptionState(SubscriptionState.NONE)
                .build();
        memberRepository.save(member);

        Channel channel = Channel.builder().channelType(ChannelType.WEB).build();

        List<LocalDateTime> dateTimes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        service.subscribe(member.getCellPhoneNumber(), channel, SubscriptionState.NORMAL, now);
        dateTimes.add(now);

        now = now.plusDays(1);
        service.cancle(member.getCellPhoneNumber(), channel, now);
        dateTimes.add(now);

        now = now.plusDays(1);
        service.subscribe(member.getCellPhoneNumber(), channel, SubscriptionState.PREMIUM, now);
        dateTimes.add(now);

        // when
        List<RequestResponse> requests = service.getRequestsByPhoneNumber(member.getCellPhoneNumber()).getRequests();

        Assertions.assertThat(requests.size()).isEqualTo(3);
        Assertions.assertThat(requests.get(0).getMemberId()).isEqualTo(member.getId());
        Assertions.assertThat(requests.get(0).getSubscriptionState()).isEqualTo(SubscriptionState.PREMIUM);
        Assertions.assertThat(requests.get(0).getChannel()).isEqualTo(channel);
        Assertions.assertThat(requests.get(0).getDate()).isEqualTo(dateTimes.get(2).toLocalDate());
        Assertions.assertThat(requests.get(0).getTime()).isEqualTo(dateTimes.get(2).toLocalTime());
        Assertions.assertThat(requests.get(1).getMemberId()).isEqualTo(member.getId());
        Assertions.assertThat(requests.get(1).getSubscriptionState()).isEqualTo(SubscriptionState.NONE);
        Assertions.assertThat(requests.get(1).getChannel()).isEqualTo(channel);
        Assertions.assertThat(requests.get(1).getDate()).isEqualTo(dateTimes.get(1).toLocalDate());
        Assertions.assertThat(requests.get(1).getTime()).isEqualTo(dateTimes.get(1).toLocalTime());
        Assertions.assertThat(requests.get(2).getMemberId()).isEqualTo(member.getId());
        Assertions.assertThat(requests.get(2).getSubscriptionState()).isEqualTo(SubscriptionState.NORMAL);
        Assertions.assertThat(requests.get(2).getChannel()).isEqualTo(channel);
        Assertions.assertThat(requests.get(2).getDate()).isEqualTo(dateTimes.get(0).toLocalDate());
        Assertions.assertThat(requests.get(2).getTime()).isEqualTo(dateTimes.get(0).toLocalTime());
    }

    @Test
    void get_requests_by_date_and_channel_return_list_of_corresponding_subscription_histories() {
        // given
        Member member = Member.builder()
                .cellPhoneNumber(CellPhoneNumber.builder()
                        .front("010").middle("1234").rear("4567")
                        .build())
                .subscriptionState(SubscriptionState.NONE)
                .build();
        Member member2 = Member.builder()
                .cellPhoneNumber(CellPhoneNumber.builder()
                        .front("010").middle("3321").rear("9876")
                        .build())
                .subscriptionState(SubscriptionState.NONE)
                .build();
        memberRepository.saveAll(List.of(member, member2));

        List<Channel> channels = List.of(
                Channel.builder().channelType(ChannelType.WEB).build(),
                Channel.builder().channelType(ChannelType.MOBILE).build(),
                Channel.builder().channelType(ChannelType.APP).build());

        List<LocalDateTime> dateTimes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        service.subscribe(member.getCellPhoneNumber(), channels.get(0), SubscriptionState.NORMAL, now);
        service.subscribe(member2.getCellPhoneNumber(), channels.get(0), SubscriptionState.NORMAL, now);
        service.cancle(member.getCellPhoneNumber(), channels.get(0), now.plusHours(2));
        service.cancle(member2.getCellPhoneNumber(), channels.get(1), now.plusHours(2));
        dateTimes.add(now);

        now = now.plusDays(1);
        service.subscribe(member.getCellPhoneNumber(), channels.get(2), SubscriptionState.PREMIUM, now);
        service.subscribe(member2.getCellPhoneNumber(), channels.get(2), SubscriptionState.PREMIUM, now);
        service.cancle(member.getCellPhoneNumber(), channels.get(2), now);
        service.cancle(member2.getCellPhoneNumber(), channels.get(2), now);
        dateTimes.add(now);
        
        now = now.plusDays(1);
        service.subscribe(member.getCellPhoneNumber(), channels.get(2), SubscriptionState.PREMIUM, now);
        service.subscribe(member2.getCellPhoneNumber(), channels.get(2), SubscriptionState.PREMIUM, now);
        service.cancle(member.getCellPhoneNumber(), channels.get(1), now);
        service.cancle(member2.getCellPhoneNumber(), channels.get(1), now);
        dateTimes.add(now);

        // when
        List<RequestResponse> requests = service
                .getRequestsByDateAndChannel(dateTimes.get(0).toLocalDate(), channels.get(0))
                .getRequests();

        Assertions.assertThat(requests.size()).isEqualTo(3);
        Assertions.assertThat(requests.get(0).getMemberId()).isEqualTo(member.getId());
        Assertions.assertThat(requests.get(0).getSubscriptionState()).isEqualTo(SubscriptionState.NONE);
        Assertions.assertThat(requests.get(0).getChannel()).isEqualTo(channels.get(0));
        Assertions.assertThat(requests.get(0).getDate()).isEqualTo(dateTimes.get(0).toLocalDate());
        Assertions.assertThat(requests.get(0).getTime()).isEqualTo(dateTimes.get(0).toLocalTime().plusHours(2));

        Assertions.assertThat(requests.get(1).getMemberId()).isEqualTo(member.getId());
        Assertions.assertThat(requests.get(1).getSubscriptionState()).isEqualTo(SubscriptionState.NORMAL);
        Assertions.assertThat(requests.get(1).getChannel()).isEqualTo(channels.get(0));
        Assertions.assertThat(requests.get(1).getDate()).isEqualTo(dateTimes.get(0).toLocalDate());
        Assertions.assertThat(requests.get(1).getTime()).isEqualTo(dateTimes.get(0).toLocalTime());

        Assertions.assertThat(requests.get(2).getMemberId()).isEqualTo(member2.getId());
        Assertions.assertThat(requests.get(2).getSubscriptionState()).isEqualTo(SubscriptionState.NORMAL);
        Assertions.assertThat(requests.get(2).getChannel()).isEqualTo(channels.get(0));
        Assertions.assertThat(requests.get(2).getDate()).isEqualTo(dateTimes.get(0).toLocalDate());
        Assertions.assertThat(requests.get(2).getTime()).isEqualTo(dateTimes.get(0).toLocalTime());

        // Assertions.assertThat(requests.get(3).getMemberId()).isEqualTo(member2.getId());
        // Assertions.assertThat(requests.get(3).getSubscriptionState()).isEqualTo(SubscriptionState.NORMAL);
        // Assertions.assertThat(requests.get(3).getChannel()).isEqualTo(channels.get(1));
        // Assertions.assertThat(requests.get(3).getDate()).isEqualTo(dateTimes.get(0).toLocalDate());
        // Assertions.assertThat(requests.get(3).getTime()).isEqualTo(dateTimes.get(0).toLocalTime().plusHours(2));
    }
}
