package com.workout.utils.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class FileResponse {
    // 원본 파일명
    private String originalFileName;
    // 저장된 파일명 (UUID 포함)
    private String storedFileName;

}
