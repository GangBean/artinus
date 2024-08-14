package com.artinus.subscription.api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artinus.subscription.api.entity.SubscriptionHistory;

public interface SubscriptionRequestRepository extends JpaRepository<SubscriptionHistory, Long> {
    List<SubscriptionHistory> findAllByMemberIdOrderByDateDescTimeDesc(Long memberId);
    List<SubscriptionHistory> findAllByDateAndChannelIdOrderByDateDescTimeDescMemberIdAsc(LocalDate date, Long channelId);
}
