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

  public FollowResponseDto follow(Long followerId, Long followeeId) {
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

  @Transactional(readOnly = true)
  public List<FollowResponseDto> getFollowList(Long userId) {
    if (userRepository.findById(userId).isEmpty()) {
      String message = String.format("User with id %d not found", userId);
      throw new UserNotFoundException(message);
    }

    return followRepository.findByFollowerId(userId).stream().map(EntityDtoMapper::toDto).toList();
  }
}
