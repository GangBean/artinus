package com.artinus.subscription.api.response;

import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder @Getter
@EqualsAndHashCode
public class HistoryListResponse {
    private final List<HistoryResponse> histories;
}
