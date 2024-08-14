package com.artinus.subscription.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder @AllArgsConstructor @Getter
public class CancleResponse {
    private final Long memberId;
    private final Long historyId;
}
