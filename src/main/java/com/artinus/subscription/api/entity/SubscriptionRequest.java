package com.artinus.subscription.api.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.artinus.subscription.api.config.ChannelConverter;

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
public class SubscriptionRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private Long memberId;
    
    @Convert(converter = ChannelConverter.class)
    private Channel channel;

    @Enumerated(value = EnumType.STRING)
    private SubscriptionState subscriptionState;
}
