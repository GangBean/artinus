package com.artinus.subscription.api.controller;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.exception.ApplicationException;
import com.artinus.subscription.api.exception.SubscriptionControllerException;
import com.artinus.subscription.api.request.CancelRequest;
import com.artinus.subscription.api.request.SubscriptionRequest;
import com.artinus.subscription.api.response.CancelResponse;
import com.artinus.subscription.api.response.HistoryListResponse;
import com.artinus.subscription.api.response.SubscribeResponse;
import com.artinus.subscription.api.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class SubscriptionController {
    private static final Pattern DATE_WITH_DASH = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern DATE_WITHOUT_DASH = Pattern.compile("\\d{8}");
    private final SubscriptionService subscriptionService;

    @Operation(summary = "구독 요청", description = "사용자의 구독 요청을 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "구독 요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류: 담당자 확인 요청"),
    })
    @PostMapping("/subscriptions")
    public ResponseEntity<SubscribeResponse> requestSubscription(
            @RequestBody final SubscriptionRequest request) {
        LocalDateTime now = LocalDateTime.now();

        SubscribeResponse response = this.subscriptionService.subscribe(
                CellPhoneNumber.from(request.getCellPhoneNumber()),
                request.getChannelId(),
                request.getSubscriptionState(), now);

        return ResponseEntity.created(URI.create("/api/histories/" + response.getHistoryId()))
                .body(response);
    }

    @Operation(summary = "구독 취소 요청", description = "사용자의 구독 취소 요청을 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "구독 취소 요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류: 담당자 확인 요청"),
    })
    @PostMapping("/cancellations")
    public ResponseEntity<CancelResponse> cancelSubscription(
            @RequestBody final CancelRequest request) {
        LocalDateTime now = LocalDateTime.now();
        CancelResponse response = this.subscriptionService.cancel(CellPhoneNumber.from(request.getCellPhoneNumber()),
                request.getChannelId(),
                request.getSubscriptionState(), now);

        return ResponseEntity.created(URI.create("/api/histories/" + response.getHistoryId()))
                .body(response);
    }

    @Operation(summary = "구독 요청 이력 조회", description = "휴대전화 번호, 날짜 또는 채널 ID에 따라 구독 요청 이력을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이력 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류: 담당자 확인 요청"),
    })
    @GetMapping("/histories")
    public ResponseEntity<HistoryListResponse> getRequestHistories(
            @RequestParam(value = "phoneNumber", required = false) final String phoneNumber,
            @RequestParam(value = "date", required = false) final String dateString,
            @RequestParam(value = "channel", required = false) final Long channelId) {
        if ((phoneNumber == null || phoneNumber.isBlank())
                && ((dateString == null || dateString.isBlank()) || channelId == null)) {
            throw new SubscriptionControllerException("휴대전화번호 혹은 날짜&채널 은 필수 입력입니다.");
        }
        HistoryListResponse response;
        if (phoneNumber != null) {
            response = subscriptionService.getRequestsByPhoneNumber(CellPhoneNumber.from(phoneNumber));
        } else {
            LocalDate date = parsed(dateString);
            response = subscriptionService.getRequestsByDateAndChannel(date, channelId);
        }
        return ResponseEntity.ok().body(response);
    }

    private LocalDate parsed(final String dateString) {
        if (DATE_WITH_DASH.matcher(dateString).matches()) {
            return LocalDate.parse(dateString);
        }
        if (DATE_WITHOUT_DASH.matcher(dateString).matches()) {
            return LocalDate.parse(new StringJoiner("-")
                    .add(dateString.substring(0, 4))
                    .add(dateString.substring(4, 6))
                    .add(dateString.substring(6, 8))
                    .toString());
        }
        throw new ApplicationException("날짜 형식을 확인해주세요(yyyy-mm-dd): " + dateString);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body("처리 도중 오류가 발생했습니다. 담당자에게 문의하세요.");
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<String> handleApplicationException(ApplicationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
