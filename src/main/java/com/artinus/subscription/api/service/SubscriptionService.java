package com.artinus.subscription.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.Member;
import com.artinus.subscription.api.entity.SubscriptionHistory;
import com.artinus.subscription.api.entity.SubscriptionState;
import com.artinus.subscription.api.exception.ApplicationException;
import com.artinus.subscription.api.exception.ChannelNotExistsException;
import com.artinus.subscription.api.exception.MemberNotExistsException;
import com.artinus.subscription.api.repository.ChannelRepository;
import com.artinus.subscription.api.repository.MemberRepository;
import com.artinus.subscription.api.repository.SubscriptionHistoryRepository;
import com.artinus.subscription.api.response.CancleResponse;
import com.artinus.subscription.api.response.HistoryListResponse;
import com.artinus.subscription.api.response.HistoryResponse;
import com.artinus.subscription.api.response.SubscribeResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SubscriptionService {
        private final MemberRepository memberRepository;
        private final SubscriptionHistoryRepository subscriptionRequestRepository;
        private final ChannelRepository channelRepository;
        private final RedissonClient redissonClient;

        // @Value("${spring.redis.wait_time}")
        private int waitTime = 15;

        // @Value("${spring.redis.lease_time}")
        private int leaseTime = 10;

        public SubscribeResponse subscribe(
                        CellPhoneNumber phoneNumber,
                        Long channelId,
                        SubscriptionState state,
                        LocalDateTime dateTime) {
                Channel channel = findChannelByIdOrThrowsException(channelId);
                channel.validateSubscription();

                RLock rLock = redissonClient.getLock(phoneNumber.toString());
                try {
                        if (!rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                                throw new ApplicationException("Lock 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");
                        }
                        return subscribe(phoneNumber, state, channel, dateTime);
                } catch (InterruptedException e) {
                        throw new ApplicationException();
                } finally {
                        rLock.unlock();
                }
        }

        public CancleResponse cancle(
                        CellPhoneNumber phoneNumber,
                        Long channelId,
                        SubscriptionState state,
                        LocalDateTime dateTime) {
                Channel channel = findChannelByIdOrThrowsException(channelId);
                channel.validateCancle();

                RLock rLock = redissonClient.getLock(phoneNumber.toString());
                try {
                        if (!rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                                throw new ApplicationException("Lock 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");
                        }
                        return cancle(phoneNumber, state, channel, dateTime);
                } catch (InterruptedException e) {
                        throw new ApplicationException();
                } finally {
                        rLock.unlock();
                }
        }

        @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
        private SubscribeResponse subscribe(CellPhoneNumber phoneNumber, SubscriptionState state, Channel channel,
                        LocalDateTime dateTime) {

                Member member = findMemberByCellPhoneNumberOrThrowsException(phoneNumber);
                SubscriptionState beforeState = member.getSubscriptionState();
                member.subscribe(state);

                SubscriptionHistory history = SubscriptionHistory.builder()
                                .memberId(member.getId())
                                .channelId(channel.getId())
                                .beforeState(beforeState)
                                .afterState(member.getSubscriptionState())
                                .date(dateTime.toLocalDate().toString())
                                .time(dateTime.toLocalTime().toString())
                                .build();

                System.out.println(history.getTime());

                this.memberRepository.save(member);
                SubscriptionHistory saved = this.subscriptionRequestRepository.save(history);
                return SubscribeResponse.builder()
                                .memberId(member.getId())
                                .historyId(saved.getId())
                                .build();
        }

        @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
        private CancleResponse cancle(CellPhoneNumber phoneNumber, SubscriptionState state, Channel channel,
                        LocalDateTime dateTime) {
                Member member = findMemberByCellPhoneNumberOrThrowsException(phoneNumber);
                SubscriptionState beforeState = member.getSubscriptionState();
                member.cancle(state);

                SubscriptionHistory history = SubscriptionHistory.builder()
                                .memberId(member.getId())
                                .channelId(channel.getId())
                                .beforeState(beforeState)
                                .afterState(member.getSubscriptionState())
                                .date(dateTime.toLocalDate().toString())
                                .time(dateTime.toLocalTime().toString())
                                .build();

                System.out.println(history.getTime());

                this.memberRepository.save(member);
                SubscriptionHistory saved = this.subscriptionRequestRepository.save(history);
                return CancleResponse.builder()
                                .memberId(member.getId())
                                .historyId(saved.getId())
                                .build();
        }

        @Transactional(readOnly = true)
        public HistoryListResponse getRequestsByPhoneNumber(CellPhoneNumber phoneNumber) {
                Member member = findMemberByCellPhoneNumberOrThrowsException(phoneNumber);

                List<SubscriptionHistory> histories = subscriptionRequestRepository
                                .findAllByMemberIdOrderByDateDescTimeDesc(member.getId());
                return HistoryListResponse.builder()
                                .histories(histories.stream()
                                                .map(HistoryResponse::from)
                                                .toList())
                                .build();
        }

        @Transactional(readOnly = true)
        public HistoryListResponse getRequestsByDateAndChannel(LocalDate date, Long channelId) {
                List<SubscriptionHistory> histories = subscriptionRequestRepository
                                .findAllByDateAndChannelIdOrderByDateDescTimeDescMemberIdAsc(date.toString(), channelId);

                return HistoryListResponse.builder()
                                .histories(histories.stream()
                                                .map(HistoryResponse::from)
                                                .toList())
                                .build();
        }

        private Member findMemberByCellPhoneNumberOrThrowsException(CellPhoneNumber phoneNumber) {
                return this.memberRepository.findByCellPhoneNumber(phoneNumber)
                                .orElseThrow(() -> new MemberNotExistsException(
                                                "해당 핸드폰 번호를 갖는 고객 정보가 존재하지 않습니다: " + phoneNumber.toString()));
        }

        private Channel findChannelByIdOrThrowsException(Long channelId) {
                return this.channelRepository.findById(channelId)
                                .orElseThrow(() -> new ChannelNotExistsException(
                                                "해당하는 ID의 채널이 존재하지 않습니다: " + channelId));
        }
}
