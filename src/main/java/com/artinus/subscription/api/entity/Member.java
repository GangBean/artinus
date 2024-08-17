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
        if (this.subscriptionState.ordinal() >= state.ordinal()) {
            throw new SubscriptionStateCanNotChangeException("낮거나 같은 등급으로 변경이 불가합니다: " + this.subscriptionState.toKor() + " -> " + state.toKor());
        }
        subscriptionState.ensureUpdatable(state);
        this.subscriptionState = state;
    }

    public void cancle(SubscriptionState state) {
        if (this.subscriptionState.ordinal() <= state.ordinal()) {
            throw new SubscriptionStateCanNotChangeException("높거나 같은 등급으로 변경이 불가합니다: " + this.subscriptionState.toKor() + " -> " + state.toKor());
        }
        subscriptionState.ensureUpdatable(state);
        this.subscriptionState = state;
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
        Member other = (Member) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    
}
