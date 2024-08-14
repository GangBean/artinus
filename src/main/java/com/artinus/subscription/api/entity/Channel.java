package com.artinus.subscription.api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder @AllArgsConstructor @Getter
@EqualsAndHashCode
public class Channel {
    private ChannelType channelType;

    public void validateSubscription() {
        this.channelType.subscribe();
    }

    public void validateCancle() {
        this.channelType.cancle();
    }
}
