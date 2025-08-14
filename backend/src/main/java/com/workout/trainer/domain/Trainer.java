package com.workout.trainer.domain;

import com.workout.global.Gender;
import com.workout.global.Role;
import com.workout.gym.domain.Gym;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 (빌더가 사용)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)// JPA가 사용하는 기본 생성자성자 (빌더가 사용)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Trainer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "gym_id", nullable = false)
  private Gym gym;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  private String phoneNumber;
  private String introduction;

  @Column(nullable = false)
  private Gender gender;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // UTC 기준의 절대 시간을 저장

  private Set<Award> awards = new HashSet<>();
  private Set<Certification> certifications = new HashSet<>();
  private Set<Education> educations = new HashSet<>();
  private Set<Workexperiences> workexperiences = new HashSet<>();
  private Set<Specialty> specialties = new HashSet<>();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;

    Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
    Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
    if (thisClass != thatClass) return false;

    Trainer that = (Trainer) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
