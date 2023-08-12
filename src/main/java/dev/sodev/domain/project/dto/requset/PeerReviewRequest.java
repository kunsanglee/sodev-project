package dev.sodev.domain.project.dto.requset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Peer Review Request")
public record PeerReviewRequest(
        @NotNull
        @Schema(description = "동료 평가", example = "프로젝트 진행중에 참여도도 높고 실력도 좋으셔서 나중에도 같이 프로젝트 하고싶어요~")
        String review
) {
}
