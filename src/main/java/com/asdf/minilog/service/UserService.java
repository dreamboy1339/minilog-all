package com.asdf.minilog.service;

import com.asdf.minilog.dto.UserRequestDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.entity.Role;
import com.asdf.minilog.entity.User;
import com.asdf.minilog.exception.NotAuthorizedException;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.repository.UserRepository;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.util.EntityDtoMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<UserResponseDto> getUsers() {
    return userRepository.findAll().stream()
        .map(EntityDtoMapper::toDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Optional<UserResponseDto> getUserById(Long id) {
    return userRepository.findById(id).map(EntityDtoMapper::toDto);
  }

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

  public UserResponseDto updateUser(
      MinilogUserDetails userDetails, Long userId, UserRequestDto userRequestDto) {
    var isUserMatchedAdmin =
        userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(Role.ROLE_ADMIN.name()));
    if (!isUserMatchedAdmin && !userDetails.getId().equals(userId)) {
      throw new NotAuthorizedException("You are not authorized to update this user");
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  String message = String.format("User with id %d not found", userId);
                  return new UserNotFoundException(message);
                });
    user.setUserName(userRequestDto.getUsername());
    user.setPassword(userRequestDto.getPassword());

    var updatedUser = userRepository.save(user);
    return EntityDtoMapper.toDto(updatedUser);
  }

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

  public void deleteUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  String message = String.format("User with id %d not found", userId);
                  return new UserNotFoundException(message);
                });
    userRepository.deleteById(user.getId());
  }
}
