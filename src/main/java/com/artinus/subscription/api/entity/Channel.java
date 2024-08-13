package com.artinus.subscription.api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder @AllArgsConstructor @Getter
public class Channel {
    private ChannelType channelType;

    public void subscribe() {
        this.channelType.subscribe();
    }

    public void cancle() {
        this.channelType.cancle();
    }
}
