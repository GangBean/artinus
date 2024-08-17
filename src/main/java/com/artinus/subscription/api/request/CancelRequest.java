package com.artinus.subscription.api.request;

import com.artinus.subscription.api.entity.SubscriptionState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class CancelRequest {
    private String cellPhoneNumber;
    private Long channelId;
    private SubscriptionState subscriptionState;
}
