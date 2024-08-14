package com.artinus.subscription.api.request;

import com.artinus.subscription.api.entity.SubscriptionState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @AllArgsConstructor
@Getter @NoArgsConstructor
public class SubscriptionRequest {
    private String cellPhoneNumber;
    private String channel;
    private SubscriptionState subscriptionState;
}
