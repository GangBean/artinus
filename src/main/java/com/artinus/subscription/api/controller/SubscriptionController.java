package com.artinus.subscription.api.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.ChannelType;
import com.artinus.subscription.api.request.SubscriptionRequest;
import com.artinus.subscription.api.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscriptions")
    public ResponseEntity<Void> requestSubscription(
            @RequestBody SubscriptionRequest request) {
        LocalDateTime now = LocalDateTime.now();

        this.subscriptionService.subscribe(CellPhoneNumber.from(request.getCellPhoneNumber()),
                Channel.builder().channelType(ChannelType.valueOf(request.getChannel())).build(),
                request.getSubscriptionState(), now);

        return ResponseEntity.created(null)
                .body(null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
