package com.artinus.subscription.api.entity;

public enum ChannelAuth {
    SUBSCRIBE("구독"), CANCLE("취소");

    private final String kor;

    private ChannelAuth(String kor) {
        this.kor = kor;
    }

    public String toKor() {
        return this.kor;
    }
}
