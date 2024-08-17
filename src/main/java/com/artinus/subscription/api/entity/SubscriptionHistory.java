package com.artinus.subscription.api.entity;

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
public class SubscriptionHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String date;
    private String time;
    private Long memberId;
    private Long channelId;

    @Enumerated(value = EnumType.STRING)
    private SubscriptionState beforeState;

    @Enumerated(value = EnumType.STRING)
    private SubscriptionState afterState;
}
