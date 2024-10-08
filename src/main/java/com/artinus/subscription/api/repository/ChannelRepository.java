package com.artinus.subscription.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artinus.subscription.api.entity.Channel;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    
}
