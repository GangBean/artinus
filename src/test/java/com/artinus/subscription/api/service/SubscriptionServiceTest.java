package com.artinus.subscription.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.artinus.subscription.api.config.RedisConfig;
import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.ChannelAuth;
import com.artinus.subscription.api.entity.ChannelAuthSet;
import com.artinus.subscription.api.entity.Member;
import com.artinus.subscription.api.entity.SubscriptionHistory;
import com.artinus.subscription.api.entity.SubscriptionState;
import com.artinus.subscription.api.exception.ApplicationException;
import com.artinus.subscription.api.repository.ChannelRepository;
import com.artinus.subscription.api.repository.MemberRepository;
import com.artinus.subscription.api.repository.SubscriptionHistoryRepository;
import com.artinus.subscription.api.response.HistoryResponse;

@DataJpaTest
@ActiveProfiles("test")
@Import({ RedisConfig.class, SubscriptionService.class, RedisTestContainers.class })
public class SubscriptionServiceTest {

        @Autowired
        private MemberRepository memberRepository;
        @Autowired
        private SubscriptionHistoryRepository historyRepository;
        @Autowired
        private ChannelRepository channelRepository;
        @Autowired
        private PlatformTransactionManager transactionManager;
        @Autowired
        private SubscriptionService service;

        @BeforeEach
        void setup() {
                clean();
        }

        void clean() {
                System.out.println("clean data");
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus status = transactionManager.getTransaction(def);

                memberRepository.deleteAll();
                channelRepository.deleteAll();
                historyRepository.deleteAll();

                transactionManager.commit(status);
        }

        @ParameterizedTest
        @MethodSource("generateSubscribeUpdateMemberStateAndMakeRequestWhenChannelHasSubscribeAuth")
        void subscribe_update_member_state_and_make_request_when_channel_has_subscribe_auth(String name,
                        ChannelAuthSet auths) {
                // given
                SubscriptionState beforeState = SubscriptionState.NONE;
                Member member = memberRepository.save(Member.builder()
                                .cellPhoneNumber(CellPhoneNumber.from("010-1234-5678"))
                                .subscriptionState(beforeState)
                                .build());
                Channel channel = channelRepository.save(Channel.builder()
                                .name(name)
                                .auths(auths)
                                .build());

                SubscriptionState state = SubscriptionState.NORMAL;
                LocalDateTime now = LocalDateTime.of(2024, 8, 31, 12, 0, 0);

                // when
                service.subscribe(member.getCellPhoneNumber(), channel.getId(), state, now);

                Member foundMember = memberRepository.findById(member.getId()).orElseThrow();
                SubscriptionHistory history = historyRepository.findAll().get(0);

                // then
                Assertions.assertThat(foundMember.getSubscriptionState()).isEqualTo(state);
                Assertions.assertThat(history.getMemberId()).isEqualTo(foundMember.getId());
                Assertions.assertThat(history.getChannelId()).isEqualTo(channel.getId());
                Assertions.assertThat(history.getDate()).isEqualTo(now.toLocalDate().toString());
                Assertions.assertThat(history.getTime()).isEqualTo(now.toLocalTime().toString());
                Assertions.assertThat(history.getBeforeState()).isEqualTo(beforeState);
                Assertions.assertThat(history.getAfterState()).isEqualTo(member.getSubscriptionState());
        }

        private static Stream<Arguments> generateSubscribeUpdateMemberStateAndMakeRequestWhenChannelHasSubscribeAuth() {
                return Stream.of(
                                Arguments.of("웹", ChannelAuthSet.of(ChannelAuth.SUBSCRIBE)),
                                Arguments.of("웹", ChannelAuthSet.of(ChannelAuth.SUBSCRIBE, ChannelAuth.CANCLE)));
        }

        @ParameterizedTest
        @MethodSource("generateCancleUpdateMemberStateToNoneAndMakeRequestWhenChannelHasCancleAuth")
        void cancle_update_member_state_to_none_and_make_request_when_channel_has_cancle_auth(SubscriptionState state,
                        String name, ChannelAuthSet auths) {
                // given
                Member member = memberRepository.save(Member.builder()
                                .cellPhoneNumber(CellPhoneNumber.from("010-1234-5678"))
                                .subscriptionState(state)
                                .build());

                Channel channel = channelRepository.save(Channel.builder()
                                .name(name)
                                .auths(auths)
                                .build());

                LocalDateTime now = LocalDateTime.of(2024, 8, 31, 12, 0, 0);

                // when
                service.cancel(member.getCellPhoneNumber(), channel.getId(), SubscriptionState.NONE, now);

                Member foundMember = memberRepository.findById(member.getId()).orElseThrow();
                SubscriptionHistory history = historyRepository.findAll().get(0);

                // then
                Assertions.assertThat(foundMember.getSubscriptionState()).isEqualTo(SubscriptionState.NONE);
                Assertions.assertThat(history.getMemberId()).isEqualTo(foundMember.getId());
                Assertions.assertThat(history.getChannelId()).isEqualTo(channel.getId());
                Assertions.assertThat(history.getDate()).isEqualTo(now.toLocalDate().toString());
                Assertions.assertThat(history.getTime()).isEqualTo(now.toLocalTime().toString());
                Assertions.assertThat(history.getBeforeState()).isEqualTo(state);
                Assertions.assertThat(history.getAfterState()).isEqualTo(member.getSubscriptionState());
        }

        private static Stream<Arguments> generateCancleUpdateMemberStateToNoneAndMakeRequestWhenChannelHasCancleAuth() {
                return Stream.of(
                                Arguments.of(SubscriptionState.NORMAL, "웹", ChannelAuthSet.of(ChannelAuth.CANCLE)),
                                Arguments.of(SubscriptionState.NORMAL, "웹",
                                                ChannelAuthSet.of(ChannelAuth.CANCLE, ChannelAuth.SUBSCRIBE)));
        }

        @Test
        void get_requests_by_phone_number_return_list_of_phone_number_owners_entire_subscription_histories() {
                // given
                Member member = Member.builder()
                                .cellPhoneNumber(CellPhoneNumber.from("010-1234-5678"))
                                .subscriptionState(SubscriptionState.NONE)
                                .build();
                memberRepository.save(member);

                Channel channel = channelRepository.save(Channel.builder()
                                .name("웹")
                                .auths(ChannelAuthSet.of(ChannelAuth.SUBSCRIBE, ChannelAuth.CANCLE))
                                .build());

                List<LocalDateTime> dateTimes = new ArrayList<>();
                LocalDateTime now = LocalDateTime.now();
                service.subscribe(member.getCellPhoneNumber(), channel.getId(), SubscriptionState.NORMAL, now);
                dateTimes.add(now);

                now = now.plusDays(1);
                service.cancel(member.getCellPhoneNumber(), channel.getId(), SubscriptionState.NONE, now);
                dateTimes.add(now);

                now = now.plusDays(1);
                service.subscribe(member.getCellPhoneNumber(), channel.getId(), SubscriptionState.PREMIUM, now);
                dateTimes.add(now);

                // when
                List<HistoryResponse> histories = service.getRequestsByPhoneNumber(member.getCellPhoneNumber())
                                .getHistories();

                Assertions.assertThat(histories.size()).isEqualTo(3);
                Assertions.assertThat(histories.get(0).getMemberId()).isEqualTo(member.getId());
                Assertions.assertThat(histories.get(0).getBeforeState()).isEqualTo(SubscriptionState.NONE);
                Assertions.assertThat(histories.get(0).getAfterState()).isEqualTo(SubscriptionState.PREMIUM);
                Assertions.assertThat(histories.get(0).getChannelId()).isEqualTo(channel.getId());
                Assertions.assertThat(histories.get(0).getDate()).isEqualTo(dateTimes.get(2).toLocalDate().toString());
                Assertions.assertThat(histories.get(0).getTime()).isEqualTo(dateTimes.get(2).toLocalTime().toString());
                Assertions.assertThat(histories.get(1).getMemberId()).isEqualTo(member.getId());
                Assertions.assertThat(histories.get(1).getBeforeState()).isEqualTo(SubscriptionState.NORMAL);
                Assertions.assertThat(histories.get(1).getAfterState()).isEqualTo(SubscriptionState.NONE);
                Assertions.assertThat(histories.get(1).getChannelId()).isEqualTo(channel.getId());
                Assertions.assertThat(histories.get(1).getDate()).isEqualTo(dateTimes.get(1).toLocalDate().toString());
                Assertions.assertThat(histories.get(1).getTime()).isEqualTo(dateTimes.get(1).toLocalTime().toString());
                Assertions.assertThat(histories.get(2).getMemberId()).isEqualTo(member.getId());
                Assertions.assertThat(histories.get(2).getBeforeState()).isEqualTo(SubscriptionState.NONE);
                Assertions.assertThat(histories.get(2).getAfterState()).isEqualTo(SubscriptionState.NORMAL);
                Assertions.assertThat(histories.get(2).getChannelId()).isEqualTo(channel.getId());
                Assertions.assertThat(histories.get(2).getDate()).isEqualTo(dateTimes.get(0).toLocalDate().toString());
                Assertions.assertThat(histories.get(2).getTime()).isEqualTo(dateTimes.get(0).toLocalTime().toString());
        }

        @Test
        void get_requests_by_date_and_channel_return_list_of_corresponding_subscription_histories() {
                // given
                Member member = Member.builder()
                                .cellPhoneNumber(CellPhoneNumber.from("010-1234-5678"))
                                .subscriptionState(SubscriptionState.NONE)
                                .build();
                Member member2 = Member.builder()
                                .cellPhoneNumber(CellPhoneNumber.from("010-1234-1234"))
                                .subscriptionState(SubscriptionState.NONE)
                                .build();
                memberRepository.saveAll(List.of(member, member2));

                Channel channel = channelRepository.save(Channel.builder()
                                .name("웹")
                                .auths(ChannelAuthSet.of(ChannelAuth.SUBSCRIBE, ChannelAuth.CANCLE))
                                .build());

                Channel channel2 = channelRepository.save(Channel.builder()
                                .name("모바일")
                                .auths(ChannelAuthSet.of(ChannelAuth.SUBSCRIBE, ChannelAuth.CANCLE))
                                .build());

                List<LocalDateTime> dateTimes = new ArrayList<>();
                LocalDateTime now = LocalDateTime.now();
                service.subscribe(member.getCellPhoneNumber(), channel.getId(), SubscriptionState.NORMAL, now);
                service.subscribe(member2.getCellPhoneNumber(), channel.getId(), SubscriptionState.NORMAL, now);
                service.cancel(member.getCellPhoneNumber(), channel.getId(), SubscriptionState.NONE, now.plusHours(2));
                service.cancel(member2.getCellPhoneNumber(), channel2.getId(), SubscriptionState.NONE,
                                now.plusHours(2));
                dateTimes.add(now);

                now = now.plusDays(1);
                service.subscribe(member.getCellPhoneNumber(), channel.getId(), SubscriptionState.PREMIUM, now);
                service.subscribe(member2.getCellPhoneNumber(), channel.getId(), SubscriptionState.PREMIUM, now);
                service.cancel(member.getCellPhoneNumber(), channel2.getId(), SubscriptionState.NONE, now);
                service.cancel(member2.getCellPhoneNumber(), channel2.getId(), SubscriptionState.NONE, now);
                dateTimes.add(now);

                now = now.plusDays(1);
                service.subscribe(member.getCellPhoneNumber(), channel2.getId(), SubscriptionState.PREMIUM, now);
                service.subscribe(member2.getCellPhoneNumber(), channel2.getId(), SubscriptionState.PREMIUM, now);
                service.cancel(member.getCellPhoneNumber(), channel.getId(), SubscriptionState.NONE, now);
                service.cancel(member2.getCellPhoneNumber(), channel.getId(), SubscriptionState.NONE, now);
                dateTimes.add(now);

                // when
                List<HistoryResponse> requests = service
                                .getRequestsByDateAndChannel(dateTimes.get(0).toLocalDate(), channel.getId())
                                .getHistories();

                Assertions.assertThat(requests.size()).isEqualTo(3);
                Assertions.assertThat(requests.get(0).getMemberId()).isEqualTo(member.getId());
                Assertions.assertThat(requests.get(0).getBeforeState()).isEqualTo(SubscriptionState.NORMAL);
                Assertions.assertThat(requests.get(0).getAfterState()).isEqualTo(SubscriptionState.NONE);
                Assertions.assertThat(requests.get(0).getChannelId()).isEqualTo(channel.getId());
                Assertions.assertThat(requests.get(0).getDate()).isEqualTo(dateTimes.get(0).toLocalDate().toString());
                Assertions.assertThat(requests.get(0).getTime())
                                .isEqualTo(dateTimes.get(0).toLocalTime().plusHours(2).toString());

                Assertions.assertThat(requests.get(1).getMemberId()).isEqualTo(member.getId());
                Assertions.assertThat(requests.get(1).getBeforeState()).isEqualTo(SubscriptionState.NONE);
                Assertions.assertThat(requests.get(1).getAfterState()).isEqualTo(SubscriptionState.NORMAL);
                Assertions.assertThat(requests.get(1).getChannelId()).isEqualTo(channel.getId());
                Assertions.assertThat(requests.get(1).getDate()).isEqualTo(dateTimes.get(0).toLocalDate().toString());
                Assertions.assertThat(requests.get(1).getTime()).isEqualTo(dateTimes.get(0).toLocalTime().toString());

                Assertions.assertThat(requests.get(2).getMemberId()).isEqualTo(member2.getId());
                Assertions.assertThat(requests.get(2).getBeforeState()).isEqualTo(SubscriptionState.NONE);
                Assertions.assertThat(requests.get(2).getAfterState()).isEqualTo(SubscriptionState.NORMAL);
                Assertions.assertThat(requests.get(2).getChannelId()).isEqualTo(channel.getId());
                Assertions.assertThat(requests.get(2).getDate()).isEqualTo(dateTimes.get(0).toLocalDate().toString());
                Assertions.assertThat(requests.get(2).getTime()).isEqualTo(dateTimes.get(0).toLocalTime().toString());
        }

        @Test
        void multi_threads_insert_consecutive_histories() throws InterruptedException, ExecutionException {
                // given
                ExecutorService executor = Executors.newFixedThreadPool(10);
                CellPhoneNumber phoneNumber = CellPhoneNumber.from("010-1234-5678");
                Callable<List<Long>> insertTask = () -> {
                        Channel channel = channelRepository.save(Channel.builder()
                                        .name("WEB")
                                        .auths(ChannelAuthSet.of(ChannelAuth.SUBSCRIBE,
                                                        ChannelAuth.CANCLE))
                                        .build());
                        Member member = memberRepository.save(Member.builder()
                                        .cellPhoneNumber(phoneNumber)
                                        .subscriptionState(SubscriptionState.NONE)
                                        .build());
                        return List.of(member.getId(), channel.getId());
                };
                List<Long> idxs = executor.submit(insertTask).get();
                Long memberId = idxs.get(0);
                Long channelId = idxs.get(1);

                int threadCount = 1_000;
                int executeCount = 1;
                IntStream.range(0, threadCount)
                                .mapToObj(i -> task(i, executeCount, channelId))
                                .map(executor::submit)
                                .forEach(future -> {
                                        try {
                                                future.get();
                                        } catch (Exception e) {
                                                e.printStackTrace();
                                        }
                                });
                executor.shutdown();

                List<SubscriptionHistory> histories = this.historyRepository
                                .findAllByMemberIdOrderByDateDescTimeDesc(memberId);
                SubscriptionState beforeState = this.memberRepository.findById(memberId).get()
                                .getSubscriptionState();

                // then
                Assertions.assertThat(histories.size()).isBetween(1, threadCount * executeCount);
                for (SubscriptionHistory history : histories) {
                        Assertions.assertThat(history.getAfterState()).isEqualTo(beforeState);
                        beforeState = history.getBeforeState();
                }
                Assertions.assertThat(beforeState).isEqualTo(SubscriptionState.NONE);
        }

        private Runnable task(int i, int executeCount, Long channelId) {
                return () -> {
                        Member member = memberRepository.findAll().get(0);

                        for (int j = 0; j < executeCount; j++) {
                                TransactionStatus threadStatus = transactionManager
                                                .getTransaction(new DefaultTransactionDefinition());
                                try {
                                        int idx = (int) (Math.random() * 3);
                                        SubscriptionState state = SubscriptionState.values()[idx];
                                        if (i % 2 == 0) {
                                                this.service.subscribe(
                                                                member.getCellPhoneNumber(),
                                                                channelId,
                                                                state,
                                                                LocalDateTime.now());
                                        } else {
                                                this.service.cancel(
                                                                member.getCellPhoneNumber(),
                                                                channelId,
                                                                state,
                                                                LocalDateTime.now());
                                        }
                                        transactionManager.commit(threadStatus);
                                } catch (ApplicationException e) {
                                        transactionManager.rollback(threadStatus);
                                        continue;
                                }
                        }
                };
        }
}
