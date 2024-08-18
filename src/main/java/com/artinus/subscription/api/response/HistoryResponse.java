package com.artinus.subscription.api.response;

import com.artinus.subscription.api.entity.SubscriptionHistory;
import com.artinus.subscription.api.entity.SubscriptionState;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder @Getter
@EqualsAndHashCode
@Schema(description = "구독 이력 정보를 담은 클래스")
public class HistoryResponse {
    @Schema(description = "구독 이력 ID", example = "1")
    private final Long id;
    @Schema(description = "구독 이력 멤버 ID", example = "1")
    private final Long memberId;
    @Schema(description = "구독 이력 요청 채널 ID", example = "1")
    private final Long channelId;
    @Schema(description = "요청 시 구독 상태", example = "NORMAL")
    private final SubscriptionState beforeState;
    @Schema(description = "요청 후 구독 상태", example = "PREMIUM")
    private final SubscriptionState afterState;
    @Schema(description = "구독 이력 생성 일자", example = "2024-08-18")
    private final String date;
    @Schema(description = "구독 이력 생성 시간", example = "12:24:30.123094534")
    private final String time;

    public static HistoryResponse from(SubscriptionHistory history) {
        return HistoryResponse.builder()
                .id(history.getId())
                .memberId(history.getMemberId())
                .channelId(history.getChannelId())
                .beforeState(history.getBeforeState())
                .afterState(history.getAfterState())
                .date(history.getDate())
                .time(history.getTime())
                .build();
    }
}
