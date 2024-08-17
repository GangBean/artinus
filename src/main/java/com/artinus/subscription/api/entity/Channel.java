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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Channel other = (Channel) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
