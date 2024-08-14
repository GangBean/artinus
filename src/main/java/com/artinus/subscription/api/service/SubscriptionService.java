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
import com.artinus.subscription.api.response.RequestListResponse;
import com.artinus.subscription.api.response.RequestResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SubscriptionService {
    private final MemberRepository memberRepository;
    private final SubscriptionRequestRepository subscriptionRequestRepository;

    @Transactional
    public void subscribe(CellPhoneNumber phoneNumber, Channel channel, SubscriptionState state,
            LocalDateTime dateTime) {
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
        this.subscriptionRequestRepository.save(request);
    }

    @Transactional
    public void cancle(CellPhoneNumber phoneNumber, Channel channel, LocalDateTime dateTime) {
        Member member = this.memberRepository.findByCellPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("해당 핸드폰 번호를 갖는 고객 정보가 존재하지 않습니다: " + phoneNumber.toString()));
        member.cancle();
        SubscriptionRequest request = SubscriptionRequest.builder()
                .memberId(member.getId())
                .channel(channel)
                .subscriptionState(member.getSubscriptionState())
                .date(dateTime.toLocalDate())
                .time(dateTime.toLocalTime())
                .build();

        this.memberRepository.save(member);
        this.subscriptionRequestRepository.save(request);
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
