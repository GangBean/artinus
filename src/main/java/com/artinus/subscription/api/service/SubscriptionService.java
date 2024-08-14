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
import com.artinus.subscription.api.repository.ChannelRepository;
import com.artinus.subscription.api.repository.MemberRepository;
import com.artinus.subscription.api.repository.SubscriptionRequestRepository;
import com.artinus.subscription.api.response.CancleResponse;
import com.artinus.subscription.api.response.RequestListResponse;
import com.artinus.subscription.api.response.RequestResponse;
import com.artinus.subscription.api.response.SubscribeResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SubscriptionService {
    private final MemberRepository memberRepository;
    private final SubscriptionRequestRepository subscriptionRequestRepository;
    private final ChannelRepository channelRepository;

    @Transactional
    public SubscribeResponse subscribe(CellPhoneNumber phoneNumber, Long channelId, SubscriptionState state,
            LocalDateTime dateTime) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("해당하는 ID의 채널이 존재하지 않습니다: " + channelId));
        channel.validateSubscription();

        Member member = this.memberRepository.findByCellPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("해당 핸드폰 번호를 갖는 고객 정보가 존재하지 않습니다: " + phoneNumber.toString()));
        member.subscribe(state);

        SubscriptionHistory request = SubscriptionHistory.builder()
                .memberId(member.getId())
                .channelId(channel.getId())
                .subscriptionState(state)
                .date(dateTime.toLocalDate())
                .time(dateTime.toLocalTime())
                .build();

        this.memberRepository.save(member);
        SubscriptionHistory saved = this.subscriptionRequestRepository.save(request);
        return SubscribeResponse.builder().memberId(member.getId()).historyId(saved.getId()).build();
    }

    @Transactional
    public CancleResponse cancle(CellPhoneNumber phoneNumber, Long channelId, SubscriptionState state, LocalDateTime dateTime) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("해당하는 ID의 채널이 존재하지 않습니다: " + channelId));
        channel.validateCancle();
        Member member = this.memberRepository.findByCellPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("해당 핸드폰 번호를 갖는 고객 정보가 존재하지 않습니다: " + phoneNumber.toString()));
        member.cancle(state);
        SubscriptionHistory request = SubscriptionHistory.builder()
                .memberId(member.getId())
                .channelId(channel.getId())
                .subscriptionState(member.getSubscriptionState())
                .date(dateTime.toLocalDate())
                .time(dateTime.toLocalTime())
                .build();

        this.memberRepository.save(member);
        SubscriptionHistory saved = this.subscriptionRequestRepository.save(request);
        return CancleResponse.builder().memberId(member.getId()).historyId(saved.getId()).build();
    }

    public RequestListResponse getRequestsByPhoneNumber(CellPhoneNumber phoneNumber) {
        Member member = this.memberRepository.findByCellPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("해당 핸드폰 번호를 갖는 고객 정보가 존재하지 않습니다: " + phoneNumber.toString()));
        List<SubscriptionHistory> requests = subscriptionRequestRepository
                .findAllByMemberIdOrderByDateDescTimeDesc(member.getId());
        return RequestListResponse.builder()
                .requests(requests.stream()
                        .map(RequestResponse::from)
                        .toList())
                .build();
    }

    public RequestListResponse getRequestsByDateAndChannel(LocalDate date, Long channelId) {
        List<SubscriptionHistory> requests = subscriptionRequestRepository
                .findAllByDateAndChannelIdOrderByDateDescTimeDescMemberIdAsc(date, channelId);
        return RequestListResponse.builder()
                .requests(requests.stream()
                        .map(RequestResponse::from)
                        .toList())
                .build();
    }
}
