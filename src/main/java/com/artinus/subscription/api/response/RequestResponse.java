package com.artinus.subscription.api.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.SubscriptionRequest;
import com.artinus.subscription.api.entity.SubscriptionState;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder @Getter
@EqualsAndHashCode
public class RequestResponse {
    private final Long id;
    private final Long memberId;
    private final Channel channel;
    private final SubscriptionState subscriptionState;
    private final LocalDate date;
    private final LocalTime time;

    public static RequestResponse from(SubscriptionRequest request) {
        return RequestResponse.builder()
                .id(request.getId())
                .memberId(request.getMemberId())
                .channel(request.getChannel())
                .subscriptionState(request.getSubscriptionState())
                .date(request.getDate())
                .time(request.getTime())
                .build();
    }
}
