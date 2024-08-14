package com.artinus.subscription.api.response;

import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder @Getter
@EqualsAndHashCode
public class RequestListResponse {
    private final List<HistoryResponse> histories;
}
