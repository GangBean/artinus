package com.artinus.subscription.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.Member;
import com.artinus.subscription.api.entity.SubscriptionHistory;
import com.artinus.subscription.api.entity.SubscriptionState;
import com.artinus.subscription.api.exception.MemberNotExistsException;
import com.artinus.subscription.api.repository.ChannelRepository;
import com.artinus.subscription.api.repository.MemberRepository;
import com.artinus.subscription.api.repository.SubscriptionRequestRepository;
import com.artinus.subscription.api.response.CancleResponse;
import com.artinus.subscription.api.response.HistoryListResponse;
import com.artinus.subscription.api.response.HistoryResponse;
import com.artinus.subscription.api.response.SubscribeResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SubscriptionService {
        private final MemberRepository memberRepository;
        private final SubscriptionRequestRepository subscriptionRequestRepository;
        private final ChannelRepository channelRepository;

        @Transactional
        public SubscribeResponse subscribe(
                        CellPhoneNumber phoneNumber,
                        Long channelId,
                        SubscriptionState state,
                        LocalDateTime dateTime) {
                Channel channel = findChannelByIdOrThrowsException(channelId);
                channel.validateSubscription();

                Member member = findMemberByCellPhoneNumberOrThrowsException(phoneNumber);
                SubscriptionState beforeState = member.getSubscriptionState();
                member.subscribe(state);

                SubscriptionHistory history = SubscriptionHistory.builder()
                                .memberId(member.getId())
                                .channelId(channel.getId())
                                .beforeState(beforeState)
                                .afterState(member.getSubscriptionState())
                                .date(dateTime.toLocalDate())
                                .time(dateTime.toLocalTime())
                                .build();

                this.memberRepository.save(member);
                SubscriptionHistory saved = this.subscriptionRequestRepository.save(history);
                return SubscribeResponse.builder()
                                .memberId(member.getId())
                                .historyId(saved.getId())
                                .build();
        }

        @Transactional
        public CancleResponse cancle(
                        CellPhoneNumber phoneNumber,
                        Long channelId,
                        SubscriptionState state,
                        LocalDateTime dateTime) {
                Channel channel = findChannelByIdOrThrowsException(channelId);
                channel.validateCancle();

                Member member = findMemberByCellPhoneNumberOrThrowsException(phoneNumber);
                SubscriptionState beforeState = member.getSubscriptionState();
                member.cancle(state);

                SubscriptionHistory history = SubscriptionHistory.builder()
                                .memberId(member.getId())
                                .channelId(channel.getId())
                                .beforeState(beforeState)
                                .afterState(member.getSubscriptionState())
                                .date(dateTime.toLocalDate())
                                .time(dateTime.toLocalTime())
                                .build();

                this.memberRepository.save(member);
                SubscriptionHistory saved = this.subscriptionRequestRepository.save(history);
                return CancleResponse.builder()
                                .memberId(member.getId())
                                .historyId(saved.getId())
                                .build();
        }

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

        public HistoryListResponse getRequestsByDateAndChannel(LocalDate date, Long channelId) {
                List<SubscriptionHistory> histories = subscriptionRequestRepository
                                .findAllByDateAndChannelIdOrderByDateDescTimeDescMemberIdAsc(date, channelId);

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
                return channelRepository.findById(channelId)
                                .orElseThrow(() -> new RuntimeException("해당하는 ID의 채널이 존재하지 않습니다: " + channelId));
        }
}
