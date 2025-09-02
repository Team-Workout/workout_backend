package com.workout.body.domain;

import com.workout.securityConverter.EncryptionConverter;
import com.workout.member.domain.Member;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "body_composition")
public class BodyComposition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "measurement_date", nullable = false)
  private LocalDate measurementDate;

  @Column(name = "weight_kg")
  @Convert(converter = EncryptionConverter.class)
  private BigDecimal weightKg;

  @Column(name = "fat_kg")
  @Convert(converter = EncryptionConverter.class)
  private BigDecimal fatKg;

  @Column(name = "muscle_mass_kg")
  @Convert(converter = EncryptionConverter.class)
  private BigDecimal muscleMassKg;
}