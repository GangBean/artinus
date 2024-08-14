package com.artinus.subscription.api.controller;

import java.net.URI;
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
import com.artinus.subscription.api.exception.SubscriptionControllerException;
import com.artinus.subscription.api.request.CancleRequest;
import com.artinus.subscription.api.request.SubscriptionRequest;
import com.artinus.subscription.api.response.CancleResponse;
import com.artinus.subscription.api.response.HistoryListResponse;
import com.artinus.subscription.api.response.SubscribeResponse;
import com.artinus.subscription.api.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscriptions")
    public ResponseEntity<SubscribeResponse> requestSubscription(
            @RequestBody SubscriptionRequest request) {
        LocalDateTime now = LocalDateTime.now();

        SubscribeResponse response = this.subscriptionService.subscribe(
                CellPhoneNumber.from(request.getCellPhoneNumber()),
                request.getChannelId(),
                request.getSubscriptionState(), now);

        return ResponseEntity.created(URI.create("/api/histories/" + response.getHistoryId()))
                .body(response);
    }

    @PostMapping("/cancle")
    public ResponseEntity<CancleResponse> cancleSubscription(
            @RequestBody CancleRequest request) {
        LocalDateTime now = LocalDateTime.now();
        CancleResponse response = this.subscriptionService.cancle(CellPhoneNumber.from(request.getCellPhoneNumber()),
                request.getChannelId(),
                request.getSubscriptionState(), now);

        return ResponseEntity.created(URI.create("/api/histories/" + response.getHistoryId()))
                .body(response);
    }

    @GetMapping("/histories")
    public ResponseEntity<HistoryListResponse> getRequestHistories(
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "channel", required = false) Long channelId) {
        if (phoneNumber == null && (date == null || channelId == null)) {
            throw new SubscriptionControllerException("휴대전화번호 혹은 날짜&채널 은 필수 입력입니다.");
        }
        HistoryListResponse response;
        if (phoneNumber != null) {
            response = subscriptionService.getRequestsByPhoneNumber(CellPhoneNumber.from(phoneNumber));
        } else {
            response = subscriptionService.getRequestsByDateAndChannel(date, channelId);
        }
        return ResponseEntity.ok().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body("처리 도중 오류가 발생했습니다. 담당자에게 문의하세요." + e.getMessage());
    }
}
