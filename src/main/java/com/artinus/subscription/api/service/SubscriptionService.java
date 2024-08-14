package com.artinus.subscription.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.Member;
import com.artinus.subscription.api.entity.SubscriptionRequest;
import com.artinus.subscription.api.entity.SubscriptionState;
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

    @Transactional
    public SubscribeResponse subscribe(CellPhoneNumber phoneNumber, Channel channel, SubscriptionState state,
            LocalDateTime dateTime) {
        channel.validateSubscription();
        Member member = this.memberRepository.findByCellPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("해당 핸드폰 번호를 갖는 고객 정보가 존재하지 않습니다: " + phoneNumber.toString()));
        member.subscribe(state);
        SubscriptionRequest request = SubscriptionRequest.builder()
                .memberId(member.getId())
                .channel(channel)
                .subscriptionState(state)
                .date(dateTime.toLocalDate())
                .time(dateTime.toLocalTime())
                .build();

        this.memberRepository.save(member);
        SubscriptionRequest saved = this.subscriptionRequestRepository.save(request);
        return SubscribeResponse.builder().memberId(member.getId()).historyId(saved.getId()).build();
    }

    @Transactional
    public CancleResponse cancle(CellPhoneNumber phoneNumber, Channel channel, SubscriptionState state, LocalDateTime dateTime) {
        channel.validateCancle();
        Member member = this.memberRepository.findByCellPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("해당 핸드폰 번호를 갖는 고객 정보가 존재하지 않습니다: " + phoneNumber.toString()));
        member.cancle(state);
        SubscriptionRequest request = SubscriptionRequest.builder()
                .memberId(member.getId())
                .channel(channel)
                .subscriptionState(member.getSubscriptionState())
                .date(dateTime.toLocalDate())
                .time(dateTime.toLocalTime())
                .build();

        this.memberRepository.save(member);
        SubscriptionRequest saved = this.subscriptionRequestRepository.save(request);
        return CancleResponse.builder().memberId(member.getId()).historyId(saved.getId()).build();
    }

    public RequestListResponse getRequestsByPhoneNumber(CellPhoneNumber phoneNumber) {
        Member member = this.memberRepository.findByCellPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("해당 핸드폰 번호를 갖는 고객 정보가 존재하지 않습니다: " + phoneNumber.toString()));
        List<SubscriptionRequest> requests = subscriptionRequestRepository
                .findAllByMemberIdOrderByDateDescTimeDesc(member.getId());
        return RequestListResponse.builder()
                .requests(requests.stream()
                        .map(RequestResponse::from)
                        .toList())
                .build();
    }

    public RequestListResponse getRequestsByDateAndChannel(LocalDate date, Channel channel) {
        List<SubscriptionRequest> requests = subscriptionRequestRepository
                .findAllByDateAndChannelOrderByDateDescTimeDescMemberIdAsc(date, channel);
        return RequestListResponse.builder()
                .requests(requests.stream()
                        .map(RequestResponse::from)
                        .toList())
                .build();
    }
}
