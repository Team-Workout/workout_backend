package com.workout.feed.dto;

import com.workout.feed.domain.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
    @NotNull(message = "대상 ID는 필수입니다.")
    Long targetId,

    @NotNull(message = "대상 타입은 필수입니다.")
    CommentType targetType, // FEED 또는 COMMENT

    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    @Size(max = 1000, message = "댓글은 최대 1000자까지 입력 가능합니다.")
    String content
) {
}