package com.workout.utils.domain;

import com.workout.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor
@Builder
@Table(name = "files")
public class UserFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  private String fileName;
  private String filePath;
  private Long fileSize;
  private String fileType;

  // MultipartFile → UserFile 변환
  public static UserFile from(Member member, String fileName, String filePath, Long fileSize, String fileType) {
    return UserFile.builder()
            .member(member)
            .fileName(fileName)
            .filePath(filePath)
            .fileSize(fileSize)
            .fileType(fileType)
            .build();
  }

}
