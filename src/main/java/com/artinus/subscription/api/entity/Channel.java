package com.artinus.subscription.api.entity;

import com.artinus.subscription.api.config.ChannelAuthSetConverter;
import com.artinus.subscription.api.exception.ChannelCanNotCancleException;
import com.artinus.subscription.api.exception.ChannelCanNotSubscribeException;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @AllArgsConstructor @Getter
@Entity @NoArgsConstructor
public class Channel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Convert(converter = ChannelAuthSetConverter.class)
    private ChannelAuthSet auths;

    public void validateSubscription() {
        if (!auths.isSubscribePossible()) {
            throw new ChannelCanNotSubscribeException("해당 채널은 구독이 불가합니다: " + this.name);
        }
    }

    public void validateCancle() {
        if (!auths.isCanclePossible()) {
            throw new ChannelCanNotCancleException("해당 채널은 해지가 불가합니다: " + this.name);
        }
    }
}
