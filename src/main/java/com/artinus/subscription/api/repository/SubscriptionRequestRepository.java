package com.artinus.subscription.api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.SubscriptionRequest;

public interface SubscriptionRequestRepository extends JpaRepository<SubscriptionRequest, Long> {
    List<SubscriptionRequest> findAllByMemberIdOrderByDateDescTimeDesc(Long memberId);
    List<SubscriptionRequest> findAllByDateAndChannelOrderByDateDescTimeDescMemberIdAsc(LocalDate date, Channel channel);
}
