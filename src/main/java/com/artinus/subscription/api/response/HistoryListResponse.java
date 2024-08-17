package com.artinus.subscription.api.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder @Getter
@EqualsAndHashCode
@Schema(description = "구독 이력 조회 결과를 담은 클래스")
public class HistoryListResponse {
    @Schema(description = "구독 이력 목록", example = "[{...}, {...}]")
    private final List<HistoryResponse> histories;
}
