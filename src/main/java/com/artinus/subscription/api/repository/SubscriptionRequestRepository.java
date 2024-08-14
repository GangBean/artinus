package com.artinus.subscription.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artinus.subscription.api.entity.SubscriptionRequest;

public interface SubscriptionRequestRepository extends JpaRepository<SubscriptionRequest, Long> {
    
}
