package com.artinus.subscription.api.entity;

import com.artinus.subscription.api.exception.SubscriptionStateCanNotChangeException;

public enum SubscriptionState {
    NONE("미가입"),
    NORMAL("일반"),
    PREMIUM("프리미엄");

    private final String kor;

    private SubscriptionState(String kor) {
        this.kor = kor;
    }

    public String toKor() {
        return this.kor;
    }

    public boolean ensureUpdatable(SubscriptionState state) {
        if (this == state) {
            throw new SubscriptionStateCanNotChangeException("구독 상태 변경이 불가합니다: " + this.toKor() + "->" + state.toKor());

        }
        return true;
    };

}
