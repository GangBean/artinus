package com.artinus.subscription.api.entity;

import com.artinus.subscription.api.exception.ChannelCanNotCancleException;
import com.artinus.subscription.api.exception.ChannelCanNotSubscribeException;

public enum ChannelType {
    WEB("웹") {

        @Override
        public void cancle() {
            return;
        }

        @Override
        public void subscribe() {
            return;
        }
        
    }, MOBILE("모바일") {

        @Override
        public void cancle() {
            throw new ChannelCanNotCancleException("해당 채널은 해지가 불가합니다: " + this.toKor());
        }

        @Override
        public void subscribe() {
            return;            
        }
        
    }, APP("앱") {

        @Override
        public void cancle() {
            return;            
        }

        @Override
        public void subscribe() {
            throw new ChannelCanNotSubscribeException("해당 채널은 구독이 불가합니다: " + this.toKor());           
        }
        
    };

    private final String kor;

    private ChannelType(String kor) {
        this.kor = kor;
    }

    public String toKor() {
        return this.kor;
    }

    public abstract void subscribe();
    public abstract void cancle();
}
