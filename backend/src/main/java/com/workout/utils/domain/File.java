package com.workout.utils.domain;

import com.workout.global.BaseEntity;
import com.workout.member.domain.Member;
import com.workout.trainer.domain.Trainer;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class File extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String filePath;

    private String category;
    private String fileType;

}
