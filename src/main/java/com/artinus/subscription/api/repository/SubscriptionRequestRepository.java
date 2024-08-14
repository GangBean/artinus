package com.artinus.subscription.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artinus.subscription.api.entity.SubscriptionRequest;

public interface SubscriptionRequestRepository extends JpaRepository<SubscriptionRequest, Long> {
    List<SubscriptionRequest> findAllByMemberId(Long memberId);
}
