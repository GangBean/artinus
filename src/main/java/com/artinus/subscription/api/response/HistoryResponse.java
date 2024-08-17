package com.artinus.subscription.api.response;

import com.artinus.subscription.api.entity.SubscriptionHistory;
import com.artinus.subscription.api.entity.SubscriptionState;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder @Getter
@EqualsAndHashCode
public class HistoryResponse {
    private final Long id;
    private final Long memberId;
    private final Long channelId;
    private final SubscriptionState beforeState;
    private final SubscriptionState afterState;
    private final String date;
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
