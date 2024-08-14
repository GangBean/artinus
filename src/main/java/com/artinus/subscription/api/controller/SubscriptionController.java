package com.artinus.subscription.api.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.ChannelType;
import com.artinus.subscription.api.request.CancleRequest;
import com.artinus.subscription.api.request.SubscriptionRequest;
import com.artinus.subscription.api.response.RequestListResponse;
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

    @PostMapping("/cancle")
    public ResponseEntity<Void> cancleSubscription(
            @RequestBody CancleRequest request) {
        LocalDateTime now = LocalDateTime.now();
        this.subscriptionService.cancle(CellPhoneNumber.from(request.getCellPhoneNumber()),
                Channel.builder().channelType(ChannelType.valueOf(request.getChannel())).build(),
                request.getSubscriptionState(), now);

        return ResponseEntity.created(null).body(null);
    }

    @GetMapping("/histories")
    public ResponseEntity<RequestListResponse> getRequestHistories(
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "channel", required = false) String channel) {
        if (phoneNumber == null && (date == null || channel == null)) {
            throw new RuntimeException("휴대전화번호 혹은 날짜&채널 은 필수 입력입니다.");
        }
        RequestListResponse response;
        if (phoneNumber != null) {
            response = subscriptionService.getRequestsByPhoneNumber(CellPhoneNumber.from(phoneNumber));
        } else {
            response = subscriptionService.getRequestsByDateAndChannel(date,
                    Channel.builder().channelType(ChannelType.valueOf(channel)).build());
        }
        return ResponseEntity.ok().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
