package com.asdf.minilog.service;

import com.asdf.minilog.dto.FollowResponseDto;
import com.asdf.minilog.entity.Follow;
import com.asdf.minilog.entity.User;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.repository.FollowRepository;
import com.asdf.minilog.repository.UserRepository;
import com.asdf.minilog.util.EntityDtoMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 팔로우(Follow) 관계의 비즈니스 로직을 담당하는 서비스.
 *
 * <p>사용자 간 팔로우/언팔로우 처리와 특정 사용자의 팔로우 목록 조회를 제공한다. 자기 자신을 팔로우하는 것은 허용하지 않는다.
 */
@Service
@Transactional
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;

  @Autowired
  public FollowService(FollowRepository followRepository, UserRepository userRepository) {
    this.followRepository = followRepository;
    this.userRepository = userRepository;
  }

  /**
   * 한 사용자가 다른 사용자를 팔로우한다.
   *
   * @param followerId 팔로우를 하는 사용자 ID
   * @param followeeId 팔로우 대상 사용자 ID
   * @return 생성된 팔로우 관계 정보
   * @throws IllegalArgumentException 자기 자신을 팔로우하려는 경우
   * @throws UserNotFoundException 팔로워 또는 팔로위 사용자를 찾을 수 없는 경우
   */
  public FollowResponseDto follow(Long followerId, Long followeeId) {
    // 자기 자신은 팔로우할 수 없음
    if (followerId.equals(followeeId)) {
      throw new IllegalArgumentException("You cannot follow yourself");
    }

    User follower =
        userRepository
            .findById(followerId)
            .orElseThrow(
                () -> {
                  String message = String.format("Follower with id %d not found", followerId);
                  return new UserNotFoundException(message);
                });
    User followee =
        userRepository
            .findById(followeeId)
            .orElseThrow(
                () -> {
                  String message = String.format("Followee with id %d not found", followeeId);
                  return new UserNotFoundException(message);
                });

    Follow follow =
        followRepository.save(EntityDtoMapper.toEntity(follower.getId(), followee.getId()));
    return EntityDtoMapper.toDto(follow);
  }

  /**
   * 팔로우 관계를 해제한다.
   *
   * @param followerId 언팔로우를 하는 사용자 ID
   * @param followeeId 언팔로우 대상 사용자 ID
   * @throws UserNotFoundException 해당 팔로우 관계가 존재하지 않는 경우
   */
  public void unfollow(Long followerId, Long followeeId) {
    Optional<Follow> follow =
        Optional.ofNullable(
            followRepository
                .findByFollowerIdAndFolloweeId(followerId, followeeId)
                .orElseThrow(
                    () -> {
                      String message =
                          String.format(
                              "Follower with id %d, Followee with id %d not found",
                              followerId, followeeId);
                      return new UserNotFoundException(message);
                    }));

    followRepository.delete(follow.get());
  }

  /**
   * 특정 사용자가 팔로우하고 있는 대상 목록을 조회한다.
   *
   * @param userId 팔로워 사용자 ID
   * @return 팔로우 관계 목록
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public List<FollowResponseDto> getFollowList(Long userId) {
    if (userRepository.findById(userId).isEmpty()) {
      String message = String.format("User with id %d not found", userId);
      throw new UserNotFoundException(message);
    }

    return followRepository.findByFollowerId(userId).stream().map(EntityDtoMapper::toDto).toList();
  }
}
