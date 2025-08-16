package com.workout.trainer.domain;

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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class TrainerSpecialty {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "specialty_id")
  private Specialty specialty;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trainer_id")
  private Trainer trainer;

  @Builder
  public TrainerSpecialty(Specialty specialty, Trainer trainer) {
    this.specialty = specialty;
    this.trainer = trainer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;

    Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
    Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
    if (thisClass != thatClass) return false;

    TrainerSpecialty that = (TrainerSpecialty) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
