package com.asdf.minilog.repository.main;

import com.asdf.minilog.entity.main.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 팔로우(Follow) 관계 엔티티를 관리하는 리포지토리. */
public interface FollowRepository extends JpaRepository<Follow, Long> {
  /**
   * 특정 사용자가 팔로우하는 모든 관계를 조회한다.
   *
   * @param followeeId 팔로워(현재 사용자) ID
   * @return 팔로우 관계 목록
   */
  List<Follow> findByFollowerId(Long followeeId);

  /**
   * 특정 팔로워-팔로위 사이의 팔로우 관계를 조회한다. (중복 팔로우 확인 등에 사용)
   *
   * @param followerId 팔로우를 하는 사용자 ID
   * @param followeeId 팔로우를 받는 사용자 ID
   * @return 존재하면 팔로우 관계, 없으면 빈 Optional
   */
  Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
}
