package com.artinus.subscription.api.entity;

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
            throw new RuntimeException("해당 채널은 해지가 불가합니다: " + this.toKor());
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
            throw new RuntimeException("해당 채널은 구독이 불가합니다: " + this.toKor());           
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
