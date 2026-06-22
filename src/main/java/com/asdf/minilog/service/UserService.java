package com.asdf.minilog.service;

import com.asdf.minilog.dto.UserRequestDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.entity.main.Role;
import com.asdf.minilog.entity.main.User;
import com.asdf.minilog.exception.NotAuthorizedException;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.repository.main.UserRepository;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.util.EntityDtoMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자(User) 도메인의 비즈니스 로직을 담당하는 서비스.
 *
 * <p>사용자 생성/수정/삭제/조회와 권한(Role) 부여·제거를 처리한다. 신규 사용자에게는 기본적으로 {@code ROLE_AUTHOR} 권한이 부여된다.
 */
@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * 전체 사용자 목록을 조회한다.
   *
   * @return 사용자 목록
   */
  @Transactional(readOnly = true)
  public List<UserResponseDto> getUsers() {
    return userRepository.findAll().stream()
        .map(EntityDtoMapper::toDto)
        .collect(Collectors.toList());
  }

  /**
   * 특정 권한(Role)을 가진 사용자 목록을 조회한다.
   *
   * @param role 조회 기준이 되는 권한
   * @return 해당 권한을 가진 사용자 목록
   */
  @Transactional(readOnly = true)
  public List<UserResponseDto> getUsersByRole(Role role) {
    return userRepository.findAllByRole(role).stream()
        .map(EntityDtoMapper::toDto)
        .collect(Collectors.toList());
  }

  /**
   * ID로 사용자를 조회한다.
   *
   * @param id 사용자 ID
   * @return 사용자 정보(없으면 빈 Optional)
   */
  @Transactional(readOnly = true)
  public Optional<UserResponseDto> getUserById(Long id) {
    return userRepository.findById(id).map(EntityDtoMapper::toDto);
  }

  /**
   * 신규 사용자를 생성한다. 기본적으로 {@code ROLE_AUTHOR} 권한이 부여된다.
   *
   * @param userRequestDto 사용자명·비밀번호 등 생성 정보
   * @return 생성된 사용자 정보
   * @throws IllegalArgumentException 동일한 사용자명이 이미 존재하는 경우
   */
  public UserResponseDto createUser(UserRequestDto userRequestDto) {
    if (userRepository.findByUserName(userRequestDto.getUsername()).isPresent()) {
      throw new IllegalArgumentException("User already exists");
    }

    // grant ROLE_AUTHOR permission when creating a new user
    HashSet<Role> roles = new HashSet<>();
    roles.add(Role.ROLE_AUTHOR);

    // NOTE: Do not THIS Real Project. it is just example for simplicity.
    // grant ROLE_ADMIN permission when creating a new user who has a name 'admin'
    if (userRequestDto.getUsername().equals("admin")) {
      roles.add(Role.ROLE_ADMIN);
    }

    User user =
        User.builder()
            .userName(userRequestDto.getUsername())
            .password(userRequestDto.getPassword())
            .roles(roles)
            .build();

    User savedUser = userRepository.save(user);
    return EntityDtoMapper.toDto(savedUser);
  }

  /**
   * 기본 {@code ROLE_AUTHOR}에 더해 지정한 권한을 함께 부여하여 신규 사용자를 생성한다.
   *
   * @param userRequestDto 사용자 생성 정보
   * @param role 추가로 부여할 권한
   * @return 생성된 사용자 정보
   * @throws IllegalArgumentException 동일한 사용자명이 이미 존재하는 경우
   */
  public UserResponseDto createUserWithRole(UserRequestDto userRequestDto, Role role) {
    if (userRepository.findByUserName(userRequestDto.getUsername()).isPresent()) {
      throw new IllegalArgumentException("User already exists");
    }

    HashSet<Role> roles = new HashSet<>();
    roles.add(Role.ROLE_AUTHOR);
    roles.add(role);

    User user =
        User.builder()
            .userName(userRequestDto.getUsername())
            .password(userRequestDto.getPassword())
            .roles(roles)
            .build();

    return EntityDtoMapper.toDto(userRepository.save(user));
  }

  /**
   * 기존 사용자에게 권한을 추가한다.
   *
   * @param userId 사용자 ID
   * @param role 추가할 권한
   * @return 권한이 갱신된 사용자 정보
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   */
  public UserResponseDto addRole(Long userId, Role role) {
    User user = findUserOrThrow(userId);
    HashSet<Role> roles = new HashSet<>();
    if (user.getRoles() != null) {
      roles.addAll(user.getRoles());
    }
    roles.add(role);
    user.setRoles(roles);
    return EntityDtoMapper.toDto(userRepository.save(user));
  }

  /**
   * 지정한 권한을 보유한 사용자의 사용자명·비밀번호를 수정한다.
   *
   * @param userId 사용자 ID
   * @param userRequestDto 변경할 정보
   * @param role 사용자가 반드시 보유하고 있어야 하는 권한
   * @return 수정된 사용자 정보
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   * @throws IllegalArgumentException 사용자가 해당 권한을 보유하지 않은 경우
   */
  public UserResponseDto updateUserWithRole(Long userId, UserRequestDto userRequestDto, Role role) {
    User user = findUserOrThrow(userId);
    if (user.getRoles() == null || !user.getRoles().contains(role)) {
      throw new IllegalArgumentException("User does not have required role");
    }

    user.setUserName(userRequestDto.getUsername());
    user.setPassword(userRequestDto.getPassword());
    return EntityDtoMapper.toDto(userRepository.save(user));
  }

  /**
   * 사용자가 보유한 권한 중 지정한 권한을 제거한다.
   *
   * @param userId 사용자 ID
   * @param role 제거할 권한
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   * @throws IllegalArgumentException 사용자가 해당 권한을 보유하지 않은 경우
   */
  public void removeRole(Long userId, Role role) {
    User user = findUserOrThrow(userId);
    if (user.getRoles() == null || !user.getRoles().contains(role)) {
      throw new IllegalArgumentException("User does not have required role");
    }
    HashSet<Role> roles = new HashSet<>(user.getRoles());
    roles.remove(role);
    user.setRoles(roles);
    userRepository.save(user);
  }

  /**
   * 사용자의 사용자명·비밀번호를 수정한다. 관리자(ROLE_ADMIN)이거나 본인인 경우에만 수정할 수 있다.
   *
   * @param userDetails 현재 인증된 사용자 정보
   * @param userId 수정 대상 사용자 ID
   * @param userRequestDto 변경할 정보
   * @return 수정된 사용자 정보
   * @throws NotAuthorizedException 관리자도 본인도 아닌 경우
   * @throws UserNotFoundException 대상 사용자를 찾을 수 없는 경우
   */
  public UserResponseDto updateUser(
      MinilogUserDetails userDetails, Long userId, UserRequestDto userRequestDto) {
    // 관리자 권한 보유 여부 확인
    var isUserMatchedAdmin =
        userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(Role.ROLE_ADMIN.name()));
    // 관리자가 아니면서 본인도 아닌 경우 수정 불가
    if (!isUserMatchedAdmin && !userDetails.getId().equals(userId)) {
      throw new NotAuthorizedException("You are not authorized to update this user");
    }

    User user = findUserOrThrow(userId);
    user.setUserName(userRequestDto.getUsername());
    user.setPassword(userRequestDto.getPassword());

    var updatedUser = userRepository.save(user);
    return EntityDtoMapper.toDto(updatedUser);
  }

  /**
   * 사용자명으로 사용자를 조회한다.
   *
   * @param username 사용자명
   * @return 사용자 정보
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   */
  public UserResponseDto getUserByUsername(String username) {
    return userRepository
        .findByUserName(username)
        .map(EntityDtoMapper::toDto)
        .orElseThrow(
            () -> {
              String message = String.format("User with username %s not found", username);
              return new UserNotFoundException(message);
            });
  }

  /**
   * 사용자를 삭제한다.
   *
   * @param userId 삭제할 사용자 ID
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   */
  public void deleteUser(Long userId) {
    User user = findUserOrThrow(userId);
    userRepository.deleteById(user.getId());
  }

  /**
   * 사용자를 조회하고, 없으면 예외를 던지는 내부 헬퍼.
   *
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   */
  private User findUserOrThrow(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              String message = String.format("User with id %d not found", userId);
              return new UserNotFoundException(message);
            });
  }
}
