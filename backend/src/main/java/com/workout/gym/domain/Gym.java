package com.workout.gym.domain;

import com.workout.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "gym")
public class Gym extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String address;

  @Column(name = "phone_number")
  private String phoneNumber;


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

    Gym gym = (Gym) o;
    return id != null && Objects.equals(id, gym.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}