package com.workout.trainer.domain;

import com.workout.trainer.dto.ProfileCreateDto.CertificationDto;
import com.workout.workout.domain.log.WorkoutLog;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
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
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }

    Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
    Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
    if (thisClass != thatClass) {
      return false;
    }

    Certification that = (Certification) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
