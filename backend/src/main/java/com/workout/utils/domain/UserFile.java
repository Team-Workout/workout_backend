package com.workout.utils.domain;

import com.workout.global.BaseEntity;
import com.workout.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor
@Builder
@Table(name = "user_file")
public class UserFile extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "record_date") // 사진 기록 날짜
  private LocalDate recordDate;

  @Column(name = "stored_file_name", nullable = false, unique = true)
  private String storedFileName ;

  @Column(name = "original_file_name")
  private String originalFileName;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "file_type")
  private String fileType;

  @Enumerated(EnumType.STRING)
  @Column(name = "purpose", nullable = false) // 이미지 용도 컬럼 추가
  private ImagePurpose purpose;

  public static UserFile from(Member member, String storedFileName, String originalFileName,
      Long fileSize, String fileType, ImagePurpose purpose, LocalDate recordDate) { // recordDate 파라미터 추가
    return UserFile.builder()
        .member(member)
        .storedFileName(storedFileName)
        .originalFileName(originalFileName)
        .fileSize(fileSize)
        .fileType(fileType)
        .purpose(purpose)
        .recordDate(recordDate) // 추가
        .build();
  }
}
