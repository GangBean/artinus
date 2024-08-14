package com.artinus.subscription.api.entity;

import com.artinus.subscription.api.config.CellPhoneNumberConverter;
import com.artinus.subscription.api.exception.SubscriptionStateCanNotChangeException;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @AllArgsConstructor @Getter
@Entity @NoArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Convert(converter = CellPhoneNumberConverter.class)
    private CellPhoneNumber cellPhoneNumber;
    
    @Enumerated(value = EnumType.STRING)
    private SubscriptionState subscriptionState;

    public void subscribe(SubscriptionState state) {
        if (this.subscriptionState != SubscriptionState.NONE) {
            throw new SubscriptionStateCanNotChangeException("이미 구독중인 서비스가 존재합니다: " + this.subscriptionState.toKor());
        }
        subscriptionState.ensureUpdatable(state);
        this.subscriptionState = state;
    }

    public void upgrade(SubscriptionState state) {
        if (this.subscriptionState != SubscriptionState.NORMAL) {
            throw new SubscriptionStateCanNotChangeException("구독 상태 갱신이 불가합니다.");
        }
        subscriptionState.ensureUpdatable(state);
        this.subscriptionState = state;
    }

    public void cancle() {
        if (this.subscriptionState == SubscriptionState.NONE) {
            throw new SubscriptionStateCanNotChangeException("현재 구독중인 상태가 아닙니다.");
        }
        subscriptionState.ensureUpdatable(SubscriptionState.NONE);
        this.subscriptionState = SubscriptionState.NONE;
    }
}
