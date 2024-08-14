package com.artinus.subscription.api.entity;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class ChannelAuthSet {
    private Set<ChannelAuth> auths;

    public boolean isSubscribePossible() {
        return auths.contains(ChannelAuth.SUBSCRIBE);
    }

    public boolean isCanclePossible() {
        return auths.contains(ChannelAuth.CANCLE);
    }

    public static ChannelAuthSet of(ChannelAuth... auths) {
        return ChannelAuthSet.builder()
                .auths(Set.of(auths))
                .build();
    }
}
