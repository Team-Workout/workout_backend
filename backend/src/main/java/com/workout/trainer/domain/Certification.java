package com.workout.trainer.domain;

import com.workout.trainer.dto.ProfileCreateDto.CertificationDto;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trainer_id", nullable = false)
  private Trainer trainer;

  private String certificationName;
  private String issuingOrganization;
  private LocalDate  acquisitionDate;

  @Builder
  private Certification(Trainer trainer, String certificationName, String issuingOrganization, LocalDate acquisitionDate) {
    this.trainer = trainer;
    this.certificationName = certificationName;
    this.issuingOrganization = issuingOrganization;
    this.acquisitionDate = acquisitionDate;
  }
  public static Certification of(CertificationDto certDto, Trainer trainer) {
    return Certification.builder()
        .trainer(trainer)
        .acquisitionDate(certDto.acquisitionDate())
        .certificationName(certDto.certificationName())
        .issuingOrganization(certDto.issuingOrganization())
        .build();
  }
}
