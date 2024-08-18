package com.artinus.subscription.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder @AllArgsConstructor @Getter
@Schema(description = "구독 요청 결과를 담은 클래스")
public class SubscribeResponse {
    @Schema(description = "구독 취소 요청 처리된 멤버의 ID", example = "1")
    private final Long memberId;
    @Schema(description = "구독 취소 요청 이력의 ID", example = "1")
    private final Long historyId;
}
