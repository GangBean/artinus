package com.artinus.subscription.api.request;

import com.artinus.subscription.api.entity.SubscriptionState;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @AllArgsConstructor
@Getter @NoArgsConstructor
@Schema(description = "구독 요청 정보를 담은 클래스")
public class SubscriptionRequest {
    @Schema(description = "사용자의 휴대폰 번호", example = "010-1111-1111")
    private String cellPhoneNumber;
    @Schema(description = "요청 채널 ID", example = "1")
    private Long channelId;
    @Schema(description = "요청 구독 상태", example = "NORMAL")
    private SubscriptionState subscriptionState;
}
