package com.workout.trainer.domain;

import com.workout.gym.domain.Gym;
import com.workout.user.domain.Gender;
import com.workout.user.domain.Role;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Gender gender;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Builder
  public Trainer(Gym gym, String name, String email, String password, String phoneNumber, String introduction, Gender gender, Role role) {
    this.gym = gym;
    this.name = name;
    this.email = email;
    this.password = password;
    this.phoneNumber = phoneNumber;
    this.introduction = introduction;
    this.gender = gender;
    this.role = role;
  }

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
