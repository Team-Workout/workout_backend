package com.workout.utils.dto;

import com.workout.utils.domain.UserFile;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class FileResponse {
    private Long fileId;
    private String fileUrl;
    private String originalFileName;
    private LocalDate recordDate;

    public static FileResponse from(UserFile userFile) {
        return new FileResponse(
            userFile.getId(),
            "/images/" + userFile.getStoredFileName(),
            userFile.getOriginalFileName(),
            userFile.getRecordDate()
        );
    }
}
