package com.workout.feed.domain;

import com.workout.member.domain.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // 연관관계 편의 메소드를 위해 추가
@Entity
@Table(name = "feed_like", uniqueConstraints = {
    // 한 사용자가 한 게시물에 좋아요를 한 번만 할 수 있도록 UNIQUE 제약조건 추가
    @UniqueConstraint(columnNames = {"feed_id", "member_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "feed_id", nullable = false, updatable = false)
  private Feed feed;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false, updatable = false)
  private Member member;
}
