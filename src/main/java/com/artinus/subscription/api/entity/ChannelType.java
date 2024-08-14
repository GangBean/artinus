package com.artinus.subscription.api.entity;

public enum ChannelType {
    WEB("웹"), MOBILE("모바일"), APP("앱");

    private final String kor;

    private ChannelType(String kor) {
        this.kor = kor;
    }

    public String toKor() {
        return this.kor;
    }
}
