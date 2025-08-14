package com.workout.user.domain;

import com.workout.global.Gender;
import com.workout.global.Role;
import com.workout.gym.domain.Gym;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Getter
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 (빌더가 사용)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)// JPA가 사용하는 기본 생성자성자 (빌더가 사용)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User {
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


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    // 스키마에 goal 컬럼이 있으므로 추가합니다. (text -> String)
    @Column(columnDefinition = "TEXT")
    private String goal;

    // Enum 타입이므로 @Enumerated를 사용하고, DB 컬럼명에 맞게 name 속성을 지정합니다.
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // 스키마에 created_at 컬럼이 있으므로 추가하고, 생성 시에만 값이 들어가도록 updatable=false로 설정합니다.
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // UTC 기준의 절대 시간을 저장

    private String phoneNumber;

    @Builder
    public User(Long id, Gym gym, String name, String email, String password, Gender gender, String goal, AccountStatus accountStatus, Role role) {
        this.id = id;
        this.gym = gym;
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.goal = goal;
        this.accountStatus = accountStatus;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
        Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
        if (thisClass != thatClass) return false;

        User that = (User) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}