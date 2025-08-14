package com.workout.gym.domain;

import com.workout.global.AuditListener;
import com.workout.global.Auditable;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "gym")
@EntityListeners(AuditListener.class)
public class Gym implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
        Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
        if (thisClass != thatClass) return false;

        Gym gym = (Gym) o;
        return id != null && Objects.equals(id, gym.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}